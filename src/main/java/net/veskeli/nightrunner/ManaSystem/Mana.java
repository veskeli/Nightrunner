package net.veskeli.nightrunner.ManaSystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veskeli.nightrunner.ModAttachments;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.networking.ManaData;
import net.veskeli.nightrunner.networking.ManaSyncPacket;
import org.jetbrains.annotations.UnknownNullability;

@EventBusSubscriber(modid = Nightrunner.MODID)
public class Mana implements IMana, INBTSerializable<CompoundTag> {

    // Like spell slots. (made from spells)
    private int spellAmount = 0;
    private int maxSpellAmount = 20; // Spell slots are like minecraft hearts. (20 = 10 visible)

    private int mana = 10;
    private int maxMana = 10;
    private int currentPenalty = 0;
    private final int maxPenalty = 30; // When casting a spell, mana regen is disabled
    private int regenCooldown = 0;
    private static final int regenCooldownMax = 30; // 20 ticks = 1 second

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = Math.min(mana, maxMana);
    }

    @Override
    public void setMaxMana(int maxMana) {
        this.maxMana = Math.max(maxMana, 0);
    }

    @Override
    public void addMana(int amount) {
        setMana(this.mana + amount);
    }

    @Override
    public void subtractMana(int amount) {
        setMana(this.mana - amount);

        currentPenalty = maxPenalty; // Set penalty to max
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public int getRegenCooldown() {
        return regenCooldown;
    }

    @Override
    public void setRegenCooldown(int regenCooldown) {
        this.regenCooldown = Math.max(0, regenCooldown);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mana", mana);
        tag.putInt("MaxMana", maxMana);
        tag.putInt("SpellAmount", spellAmount);
        tag.putInt("MaxSpellAmount", maxSpellAmount);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        mana = compoundTag.getInt("Mana");
        maxMana = compoundTag.getInt("MaxMana");
        spellAmount = compoundTag.getInt("SpellAmount");
        maxSpellAmount = compoundTag.getInt("MaxSpellAmount");
    }

    @Override
    public void setSpellLevel(int level) {
        this.spellAmount = Math.min(level, maxSpellAmount);
    }

    @Override
    public int getCurrentPenalty() {
        return currentPenalty;
    }

    @Override
    public void subtractPenalty(int amount) {
        currentPenalty = Math.max(0, currentPenalty - amount);
    }

    @Override
    public int getCurrentRecharge() {
        // Return penalty if active
        if(currentPenalty > 0) {
            return currentPenalty + regenCooldown;
        }
        // Else return regen cooldown
        return regenCooldown;
    }

    @Override
    public int getSpellAmount() {
        return spellAmount;
    }

    @Override
    public void regenSpellSlots(int amount) {
        spellAmount = Math.min(spellAmount + amount, maxSpellAmount);
    }

    @Override
    public void subtractSpellSlots(int amount) {
        spellAmount = Math.max(spellAmount - amount, 0);
    }

    @Override
    public int getMaxSpellAmount() {
        return maxSpellAmount;
    }

    public ManaSyncPacket getNewManaSyncPacket() {
        return new ManaSyncPacket(mana, maxMana, currentPenalty, spellAmount, maxSpellAmount);
    }

    @SubscribeEvent
    public static void onCustomPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Mana mana = player.getData(ModAttachments.PLAYER_MANA);

        // Return if mana is full
        if (mana.getMana() >= mana.getMaxMana()) {
            return;
        }

        if (event.getEntity().level().isClientSide)
        {
            // In the client we can tick down the cooldown.
            // As when we set the cooldown on the server we send it to client
            // and the client will tick it down. So player will see the cooldown
            // on the client side.
            mana.setRegenCooldown(mana.getRegenCooldown() - 1);
            player.setData(ModAttachments.PLAYER_MANA, mana);
            return;
        }

        // Tick regen cooldown if active
        if (mana.getRegenCooldown() > 0) {
            mana.setRegenCooldown(mana.getRegenCooldown() - 1);
            return; // No regen during penalty
        }

        // Check if player is in penalty
        if (mana.getCurrentPenalty() > 0) {
            mana.subtractPenalty(1);
            return; // No regen during penalty
        }

        if (mana.getRegenCooldown() <=0 ) {
            mana.addMana(1); // Regenerate 1 mana

            // If mana is not full, reset cooldown
            if (mana.getMana() < mana.getMaxMana()) {
                mana.setRegenCooldown(regenCooldownMax);
            }

            // Set mana back to player
            player.setData(ModAttachments.PLAYER_MANA, mana);

            if (player instanceof ServerPlayer serverPlayer) {
                // Send mana to client
                ManaSyncPacket pkt = mana.getNewManaSyncPacket();
                PacketDistributor.sendToPlayer(serverPlayer, pkt);
            }
            else {
                // Handle the case where player is not a ServerPlayer
                System.out.println("Player is not a ServerPlayer");
            }
        }
    }
}
