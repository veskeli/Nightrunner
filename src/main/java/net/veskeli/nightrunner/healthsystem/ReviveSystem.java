package net.veskeli.nightrunner.healthsystem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.item.ModItems;

import java.util.List;
import java.util.UUID;

public class ReviveSystem {

    public static void TryRevive(PlayerInteractEvent.EntityInteractSpecific event, GraveEntity grave, ServerPlayer interactor, ItemStack itemInHand) {
        // Check if the item is supported for revival
        RevivalItem revivalItem = RevivalItem.fromItem(itemInHand);
        if (revivalItem == null)
        {
            if(!itemInHand.is(Items.AIR))
            {
                interactor.displayClientMessage(Component.literal("This item cannot revive the soul..."), true);
            }
            return;
        }

        UUID graveOwnerId = grave.getOwner();
        if (graveOwnerId == null)
        {
            System.out.println("Grave owner ID is null");
            return;
        }

        ServerLevel level = (ServerLevel) interactor.level();
        ServerPlayer ownerPlayer = level.getServer().getPlayerList().getPlayer(graveOwnerId);
        if (ownerPlayer == null) {
            interactor.displayClientMessage(Component.literal("Cannot find the soul..."), true);
            return;
        }

        // Execute item-specific revival logic
        revivalItem.revive(ownerPlayer, grave, interactor, level, itemInHand);

        // Effects at revival location
        level.playSound(null, ownerPlayer.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 1.0f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                ownerPlayer.getX(), ownerPlayer.getY() + 1, ownerPlayer.getZ(),
                20, 0.5, 0.5, 0.5, 0.1
        );

        // Notify both players
        interactor.displayClientMessage(Component.literal("You revived ").append(ownerPlayer.getDisplayName()).append("!"), true);
        ownerPlayer.displayClientMessage(Component.literal("You have been revived by ").append(interactor.getDisplayName()).append("!"), true);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    // Enum to handle different item types for revival
    public enum RevivalItem {
        SOULSTONE {
            @Override
            public void revive(ServerPlayer ownerPlayer, GraveEntity grave, ServerPlayer interactor, ServerLevel level, ItemStack itemInHand) {
                System.out.println("Reviving with Golden Apple");
                // Restore inventory and set player stats
                restoreInventory(ownerPlayer, grave);
                restoreStats(ownerPlayer, 16.0f); // Restore health with 8 hearts (16 health)
                handleReviveConvert(ownerPlayer, grave);

                // Consume the golden apple
                itemInHand.shrink(1);
            }
        };

        // Abstract method for each revival item to implement specific behavior
        public abstract void revive(ServerPlayer ownerPlayer, GraveEntity grave, ServerPlayer interactor, ServerLevel level, ItemStack itemInHand);

        // Helper method to restore inventory
        private static void restoreInventory(ServerPlayer ownerPlayer, GraveEntity grave) {
            List<ItemStack> stored = GraveDataStore.retrieveInventory(grave.getOwner());
            if (stored != null) {
                ownerPlayer.getInventory().clearContent();
                for (int i = 0; i < stored.size(); i++) {
                    ownerPlayer.getInventory().setItem(i, stored.get(i));
                }
            }
        }

        // Helper method to set the player's health and max health
        private static void restoreStats(ServerPlayer player, float healthValue) {
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(healthValue);
            player.setHealth(healthValue);
        }

        // Helper method to set the player's game mode
        private static void handleReviveConvert(ServerPlayer player, GraveEntity grave) {
            player.setGameMode(GameType.SURVIVAL);
            // Set the player's position to the grave location
            player.teleportTo(grave.getX(), grave.getY() + 1, grave.getZ());
            // Remove the grave entity
            grave.remove(Entity.RemovalReason.DISCARDED);
        }

        // Helper method to find the correct RevivalItem by checking the item in hand
        public static RevivalItem fromItem(ItemStack itemStack) {
            if (itemStack.is(ModItems.Soulstone)) {
                return SOULSTONE;
            }
            return null;  // No revival item found
        }
    }
}
