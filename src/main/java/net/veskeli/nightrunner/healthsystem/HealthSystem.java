package net.veskeli.nightrunner.healthsystem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.veskeli.nightrunner.ModAttachments;

import java.util.Objects;

public class HealthSystem {

    private static final float MINIMUM_REVIVE_HEALTH = 2.0f;

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

    public static float getReviveHealthForItem(Player player, float itemMaxReviveHealth, float itemDegradeStep) {
        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        float normalizedMaxReviveHealth = normalizeToWholeHearts(itemMaxReviveHealth);
        float normalizedDegradeStep = normalizeDegradeStep(itemDegradeStep);

        boolean needsItemRefresh = !nearlyEqual(healthStats.getReviveItemMaxHealth(), normalizedMaxReviveHealth)
                || !nearlyEqual(healthStats.getReviveHealthDegradeStep(), normalizedDegradeStep)
                || healthStats.getCurrentReviveHealth() <= 0.0f;

        if (needsItemRefresh) {
            healthStats.setReviveItemMaxHealth(normalizedMaxReviveHealth);
            healthStats.setReviveHealthDegradeStep(normalizedDegradeStep);
            healthStats.setCurrentReviveHealth(normalizedMaxReviveHealth);
            player.setData(ModAttachments.PLAYER_HEALTH_STATS, healthStats);
        }

        float clampedReviveHealth = Math.clamp(healthStats.getCurrentReviveHealth(), MINIMUM_REVIVE_HEALTH, normalizedMaxReviveHealth);
        float reviveHealth = normalizeToWholeHearts(clampedReviveHealth);
        healthStats.setCurrentReviveHealth(reviveHealth);
        player.setData(ModAttachments.PLAYER_HEALTH_STATS, healthStats);
        return reviveHealth;
    }

    public static void applyReviveDegradationAfterRevive(Player player) {
        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        float maxReviveHealth = healthStats.getReviveItemMaxHealth();
        float degradeStep = healthStats.getReviveHealthDegradeStep();

        if (maxReviveHealth <= 0.0f || degradeStep <= 0.0f) {
            return;
        }

        float currentReviveHealth = healthStats.getCurrentReviveHealth();
        if (currentReviveHealth <= 0.0f) {
            currentReviveHealth = maxReviveHealth;
        }

        float degradedHealth = Math.clamp(currentReviveHealth - degradeStep, MINIMUM_REVIVE_HEALTH, maxReviveHealth);
        degradedHealth = normalizeToWholeHearts(degradedHealth);
        healthStats.setCurrentReviveHealth(degradedHealth);
        player.setData(ModAttachments.PLAYER_HEALTH_STATS, healthStats);
    }

    public static void resetReviveDegradation(Player player) {
        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        float maxReviveHealth = healthStats.getReviveItemMaxHealth();
        if (maxReviveHealth <= 0.0f) {
            return;
        }

        healthStats.setCurrentReviveHealth(normalizeToWholeHearts(maxReviveHealth));
        player.setData(ModAttachments.PLAYER_HEALTH_STATS, healthStats);
    }

    private static float normalizeToWholeHearts(float healthValue) {
        float normalized = (float) (Math.floor(healthValue / 2.0f) * 2.0f);
        return Math.max(MINIMUM_REVIVE_HEALTH, normalized);
    }

    private static float normalizeDegradeStep(float degradeStep) {
        if (degradeStep <= 0.0f) {
            return 0.0f;
        }

        float normalized = (float) (Math.floor(degradeStep / 2.0f) * 2.0f);
        return Math.max(2.0f, normalized);
    }

    private static boolean nearlyEqual(float a, float b) {
        return Math.abs(a - b) < 0.0001f;
    }

    public static boolean tryUseHealthModifierItem(LivingEntityUseItemEvent.Finish event, Player player, ItemStack itemInHand) {
        // Check if the item is a health modifier item
        HealthModifierItem healthModifierItem = HealthModifierItem.fromItem(itemInHand);
        // If the item is not a health modifier item, return
        if (healthModifierItem == null) {
            System.out.println("Item is not a health modifier item");
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
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

            // Reset degradation only when this food actually increases permanent max health.
            if (success) {
                HealthSystem.resetReviveDegradation(player);
            }

            if (success) {
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
            return null;
        }
    }
}
