package net.veskeli.nightrunner.healthsystem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.veskeli.nightrunner.ModAttachments;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.item.ModItems;

import java.util.List;
import java.util.UUID;

public class ReviveSystem {

    private static final float SELF_REVIVE_HEALTH = 2.0f;

    public enum SelfReviveSource {
        ENCHANTED_GOLDEN_APPLE("enchanted_golden_apple") {
            @Override
            public boolean matches(ItemStack stack) {
                return stack.is(Items.ENCHANTED_GOLDEN_APPLE);
            }
        };

        private final String id;

        SelfReviveSource(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public abstract boolean matches(ItemStack stack);

        public static SelfReviveSource fromItem(ItemStack stack) {
            for (SelfReviveSource source : values()) {
                if (source.matches(stack)) {
                    return source;
                }
            }
            return null;
        }

        public static SelfReviveSource fromId(String id) {
            if (id == null || id.isEmpty()) {
                return null;
            }

            for (SelfReviveSource source : values()) {
                if (source.id.equals(id)) {
                    return source;
                }
            }
            return null;
        }
    }

    public static void grantSelfRevive(ServerPlayer player, SelfReviveSource source) {
        if (source == null) {
            return;
        }

        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        healthStats.setPendingSelfRevive(true);
        healthStats.setPendingSelfReviveSourceId(source.getId());
        player.setData(ModAttachments.PLAYER_HEALTH_STATS, healthStats);

        player.displayClientMessage(Component.literal("Your next death can be self-revived.").withStyle(ChatFormatting.AQUA), false);
    }

    public static boolean hasPendingSelfRevive(ServerPlayer player) {
        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        return healthStats.hasPendingSelfRevive();
    }

    public static void sendSelfReviveOffer(ServerPlayer player) {
        if (!hasPendingSelfRevive(player)) {
            return;
        }

        Component clickable = Component.literal("[Click here]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nr selfrevive"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Revive to respawn point with 1 heart")))
                        .withUnderlined(true));

        player.displayClientMessage(
                Component.literal("You can self revive to your last respawn point with one heart. ")
                        .withStyle(ChatFormatting.AQUA)
                        .append(clickable),
                false
        );
    }

    public static boolean tryUsePendingSelfRevive(ServerPlayer player) {
        HealthStats healthStats = player.getData(ModAttachments.PLAYER_HEALTH_STATS);
        if (!healthStats.hasPendingSelfRevive()) {
            return false;
        }

        if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            return false;
        }

        reviveToRespawnPoint(player);
        healthStats.setPendingSelfRevive(false);
        healthStats.setPendingSelfReviveSourceId("");
        player.setData(ModAttachments.PLAYER_HEALTH_STATS, healthStats);
        player.displayClientMessage(Component.literal("Self revive used."), false);
        return true;
    }

    private static void reviveToRespawnPoint(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel targetLevel = server.getLevel(player.getRespawnDimension());

        if (targetLevel == null) {
            targetLevel = server.getLevel(Level.OVERWORLD);
        }

        if (targetLevel == null) {
            targetLevel = player.serverLevel();
        }

        var respawnPos = player.getRespawnPosition();
        var targetPos = respawnPos != null ? respawnPos : targetLevel.getSharedSpawnPos();

        player.setGameMode(GameType.SURVIVAL);
        player.teleportTo(targetLevel, targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5, player.getYRot(), player.getXRot());
        HealthSystem.setPlayerMaxHealth(player, SELF_REVIVE_HEALTH);
    }

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

        UUID graveId = grave.getGraveId();
        if (graveId == null) {
            interactor.displayClientMessage(Component.literal("This grave is missing its soul binding."), true);
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
        SOULSTONE(12.0f, 2.0f) {
            @Override
            public void revive(ServerPlayer ownerPlayer, GraveEntity grave, ServerPlayer interactor, ServerLevel level, ItemStack itemInHand) {
                // Restore inventory and set player stats
                restoreInventory(ownerPlayer, grave);
                restoreStats(ownerPlayer, getMaxReviveHealth(), getDegradePerDeath());
                handleReviveConvert(ownerPlayer, grave);
                itemInHand.shrink(1);
            }
        };

        private final float maxReviveHealth;
        private final float degradePerDeath;

        RevivalItem(float maxReviveHealth, float degradePerDeath) {
            this.maxReviveHealth = maxReviveHealth;
            this.degradePerDeath = degradePerDeath;
        }

        public float getMaxReviveHealth() {
            return maxReviveHealth;
        }

        public float getDegradePerDeath() {
            return degradePerDeath;
        }

        public abstract void revive(ServerPlayer ownerPlayer, GraveEntity grave, ServerPlayer interactor, ServerLevel level, ItemStack itemInHand);

        private static void restoreInventory(ServerPlayer ownerPlayer, GraveEntity grave) {
            UUID ownerId = grave.getOwner();
            UUID graveId = grave.getGraveId();
            if (ownerId == null || graveId == null) {
                return;
            }

            List<ItemStack> stored = GraveDataStore.consumeInventory(ownerPlayer.serverLevel(), ownerId, graveId);
            if (stored != null) {
                ownerPlayer.getInventory().clearContent();
                for (int i = 0; i < stored.size(); i++) {
                    ownerPlayer.getInventory().setItem(i, stored.get(i));
                }
            }
        }

        private static void restoreStats(ServerPlayer player, float itemMaxReviveHealth, float itemDegradePerDeath) {
            float reviveHealth = HealthSystem.getReviveHealthForItem(player, itemMaxReviveHealth, itemDegradePerDeath);
            HealthSystem.setPlayerMaxHealth(player, reviveHealth);
            // Store the degraded value for the next revive attempt, not for the current one.
            HealthSystem.applyReviveDegradationAfterRevive(player);
        }

        private static void handleReviveConvert(ServerPlayer player, GraveEntity grave) {
            player.setGameMode(GameType.SURVIVAL);
            player.teleportTo(grave.getX(), grave.getY() + 1, grave.getZ());
            grave.remove(Entity.RemovalReason.DISCARDED);
        }

        public static RevivalItem fromItem(ItemStack itemStack) {
            if (itemStack.is(ModItems.Soulstone)) {
                return SOULSTONE;
            }
            return null;
        }
    }
}
