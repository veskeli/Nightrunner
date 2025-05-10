package net.veskeli.nightrunner.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.ModAttachments;

public class ManaOrbEntity extends Entity {

    private int manaAmount;

    public ManaOrbEntity(EntityType<? extends Entity> entityType, Level level, int manaAmount) {
        super(entityType, level);
        this.manaAmount = manaAmount;
    }

    public ManaOrbEntity(EntityType<ManaOrbEntity> manaOrbEntityEntityType, Level level) {
        super(manaOrbEntityEntityType, level);
        this.manaAmount = 1;
    }

    public void setManaAmount(int manaAmount) {
        this.manaAmount = Math.max(0, Math.min(manaAmount, 100)); // Clamp between 0 and 100
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    public void playerTouch(Player player) {
        // Return if client side
        if (player.level().isClientSide()) {
            return;
        }
        Mana mana = player.getData(ModAttachments.PLAYER_MANA);

        mana.regenSpellSlots(manaAmount);

        player.setData(ModAttachments.PLAYER_MANA, mana);

        // Send the mana data to the client
        Mana.replicateData(mana, (ServerPlayer) player);

        // Play sound effect
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, .3F);

        // Remove the entity from the world
        this.remove(RemovalReason.DISCARDED);
    }

    public int getIcon() {
        if (this.manaAmount >= 30) {
            return 10;
        } else if (this.manaAmount >= 20) {
            return 9;
        } else if (this.manaAmount >= 15) {
            return 8;
        } else if (this.manaAmount >= 10) {
            return 7;
        } else if (this.manaAmount >= 7) {
            return 6;
        } else if (this.manaAmount >= 5) {
            return 5;
        } else if (this.manaAmount >= 4) {
            return 4;
        } else if (this.manaAmount >= 3) {
            return 3;
        } else if (this.manaAmount >= 2) {
            return 2;
        } else {
            return this.manaAmount >= 3 ? 1 : 0;
        }
    }
}
