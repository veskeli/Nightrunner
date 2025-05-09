package net.veskeli.nightrunner.healthsystem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.veskeli.nightrunner.ModAttachments;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.item.ModItems;

import java.util.List;
import java.util.Objects;

public class HealthSystem {

    public static void addTemporaryHealth(Player player, float tempHealthAmount, float MaxAbsorptionValue) {
        // get the current absorption amount
        float currentAbsorption = player.getAbsorptionAmount();

        float newAbsorption = Math.clamp(currentAbsorption + tempHealthAmount, 0, MaxAbsorptionValue);

        // return if current absorption is greater than the new absorption
        if (currentAbsorption >= newAbsorption) {
            return;
        }

        // set the new absorption amount
        player.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(newAbsorption);
        player.setAbsorptionAmount(newAbsorption);
    }

    // Set the player's max health to a value greater than the current max health if possible
    public static boolean setPlayerMaxHealthMoreIfPossible(Player player, float healthValue) {
        // Get the player's health stats
        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        // Check if the new health value is greater than the current max health
        if (healthValue > healthStats.getHealth()) {
            // Set the new max health
            healthStats.setMaxHealth((int) healthValue);
            // Set the player's health stats
            player.setData(ModAttachments.PLAYER_HEALTH_STATS, healthStats);

            // Set the player's max health
            Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(healthValue);
            return true;
        }
        return false;
    }

    public static void loadPlayerMaxHealth(Player player) {
        // Get the player's health stats
        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        // Update the player's max health only if it's different from the current value
        if (!Objects.equals(player.getAttribute(Attributes.MAX_HEALTH).getBaseValue(), healthStats.getHealth())) {
            Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(healthStats.getHealth());
        }
    }

    public static void setPlayerMaxHealth(Player player, float healthValue) {
        // Get the player's health stats
        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        healthStats.setMaxHealth((int) healthValue);
        // Set the player's health stats
        player.setData(ModAttachments.PLAYER_HEALTH_STATS, healthStats);

        // Set the player's max health
        Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(healthValue);
        // Set the player's current health
        player.setHealth(healthValue);
    }

    public static boolean tryUseHealthModifierItem(LivingEntityUseItemEvent.Finish event, Player player, ItemStack itemInHand) {
        // Check if the item is a health modifier item
        HealthModifierItem healthModifierItem = HealthModifierItem.fromItem(itemInHand);
        // If the item is not a health modifier item, return
        if (healthModifierItem == null) {
            System.out.println("Item is not a health modifier item");
            return false;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if(serverPlayer == null) {
            System.out.println("ServerPlayer is null");
            return false;
        }

        // Use the item
        healthModifierItem.useHealthModifier(serverPlayer, itemInHand);
        return true;
    }

    // Enum to handle different item types for revival
    public enum HealthModifierItem {
        GOLDEN_APPLE {
            @Override
            public void useHealthModifier(ServerPlayer ownerPlayer, ItemStack itemInHand) {
                // Restore inventory and set player stats
                HealthModifierItem.setStats(ownerPlayer, 20.f);

                // Consume 1 of the item
                itemInHand.shrink(1);

                // Remove the golden apple absorption effect
                ownerPlayer.removeEffect(MobEffects.ABSORPTION);

                // Add 10 absorption hearts
                addTemporaryHealth(ownerPlayer, 10.f, 10.f);
            }
        },
        GOLDEN_CARROT {
            @Override
            public void useHealthModifier(ServerPlayer ownerPlayer, ItemStack itemInHand) {
                // Restore inventory and set player stats
                HealthModifierItem.setStats(ownerPlayer, 16.f);

                // Consume 1 of the item
                itemInHand.shrink(1);
            }
        },
        ENCHANTED_GOLDEN_APPLE {
            @Override
            public void useHealthModifier(ServerPlayer ownerPlayer, ItemStack itemInHand) {
                // Restore inventory and set player stats
                HealthModifierItem.setStats(ownerPlayer, 22.f);

                // Consume 1 of the item
                itemInHand.shrink(1);

                // Remove the golden apple absorption effect
                ownerPlayer.removeEffect(MobEffects.ABSORPTION);

                // Add 10 absorption hearts
                addTemporaryHealth(ownerPlayer, 16.f, 16.f);
            }
        };

        // Abstract method for each revival item to implement specific behavior
        public abstract void useHealthModifier(ServerPlayer ownerPlayer, ItemStack itemInHand);

        // Helper method to set the player's health and max health
        private static void setStats(ServerPlayer player, float healthValue) {

            // Set the player's max health stats (only if possible to increase)
            boolean success = HealthSystem.setPlayerMaxHealthMoreIfPossible(player, healthValue);

            if(success)
            {
                // Add confetti effect
                player.level().addParticle(
                        ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getY() + 1, player.getZ(),
                        0.5, 0.5, 0.5
                );
            }
        }

        // Helper method to find the correct RevivalItem by checking the item in hand
        public static HealthModifierItem fromItem(ItemStack itemStack) {
            if (itemStack.is(Items.GOLDEN_APPLE)) {
                return GOLDEN_APPLE;
            }
            if (itemStack.is(Items.GOLDEN_CARROT)) {
                return GOLDEN_CARROT;
            }
            if (itemStack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                return ENCHANTED_GOLDEN_APPLE;
            }
            return null;  // No revival item found
        }
    }
}
