package net.veskeli.nightrunner.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.ModAttachments;
import net.veskeli.nightrunner.SpellSystem.Attachment.SpellAttachment;
import net.veskeli.nightrunner.SpellSystem.Spell;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.projectile.WandProjectile;
import net.veskeli.nightrunner.item.properties.WandItemProperties;
import net.veskeli.nightrunner.networking.ManaData;
import net.veskeli.nightrunner.networking.ManaSyncPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WandItem extends Item{

    private float power = 4.0f;
    private float aoeRadius = 1.0f;

    public WandItem(WandItemProperties properties) {
        super(properties);
        this.power = properties.getPower();
        this.aoeRadius = properties.getAoeRadius();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        Mana mana = player.getData(ModAttachments.PLAYER_MANA);

        SpellAttachment spellAttachment = player.getData(ModAttachments.PLAYER_SPELLS);
        Spell spell = spellAttachment.getSelectedSpell();

        if(spell != null)
        {
            if(player.level().isClientSide()) return InteractionResultHolder.success(itemStack);

            int spellCost = spell.getCost();

            // Check if the player has enough spell slots
            int spellAmount = mana.getSpellAmount();
            if(spellAmount < 1) {
                // Send message to player
                player.sendSystemMessage(Component.literal("Sell slots depleted!"));
                // play sound effect
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_DRIP, SoundSource.PLAYERS, 0.5f, 1.0f);
                return InteractionResultHolder.fail(itemStack);
            }
            else if(spellAmount < spellCost) {
                // Send message to player
                player.sendSystemMessage(Component.literal("Not enough spell slots!"));
                // play sound effect
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_DRIP, SoundSource.PLAYERS, 0.5f, 1.0f);
                return InteractionResultHolder.fail(itemStack);
            }

            // Cast the spell
            boolean succeeded = spell.castSpell(level,player, hand);
            if(succeeded)
            {
                DefaultHurtItem(level, player, itemStack);

                // Use the spell slot
                mana.subtractSpellSlots(spellCost);

                // set mana back to player
                player.setData(ModAttachments.PLAYER_MANA, mana);

                ServerPlayer serverPlayer = (ServerPlayer) player;
                // Send mana to client
                ManaSyncPacket pkt = mana.getNewManaSyncPacket();
                PacketDistributor.sendToPlayer(serverPlayer, pkt);
            }

            return InteractionResultHolder.success(itemStack);
        }

        // Return if client side
        if (level.isClientSide) {
            return InteractionResultHolder.success(itemStack);
        }
        // If spell was not selected we use the wand spell
        if (useWandSpell(level, player, mana, itemStack))
        {
            return InteractionResultHolder.success(itemStack);
        }
        // play sound effect
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_DRIP, SoundSource.PLAYERS, 0.5f, 1.0f);
        // Send message to player
        System.out.println("Mana is empty!");
        return InteractionResultHolder.success(itemStack);
    }

    private void DefaultHurtItem(Level level, Player player, ItemStack itemStack) {
        // Play sound effect
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.5f, 1.0f);

        // Damage the item
        itemStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

        // Apply use time
        player.getCooldowns().addCooldown(this, 15);
    }

    protected @Nullable boolean useWandSpell(Level level, Player player, Mana mana, ItemStack itemStack) {
        if(mana.getMana() < 1) {
            // Send message to player
            player.sendSystemMessage(Component.literal("Mana is empty!"));
            return false;
        }

        // Summon the WandProjectile
        WandProjectile projectile = new WandProjectile(ModEntities.WAND_PROJECTILE.get(), level);
        projectile.setCustomProperties(player,power, aoeRadius);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        projectile.setOwner(player);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 2.0F); // same speed as arrow
        level.addFreshEntity(projectile);

        // Play sound effect
        DefaultHurtItem(level, player, itemStack);

        // Subtract mana
        mana.subtractMana(1);
        // set mana back to player
        player.setData(ModAttachments.PLAYER_MANA, mana);

        ServerPlayer serverPlayer = (ServerPlayer) player;
        // Send mana to client
        ManaSyncPacket pkt = mana.getNewManaSyncPacket();
        PacketDistributor.sendToPlayer(serverPlayer, pkt);
        return true;

    }
}
