package net.veskeli.nightrunner.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.healthsystem.GraveDataStore;
import net.veskeli.nightrunner.healthsystem.HealthSystem;
import net.veskeli.nightrunner.healthsystem.ReviveSystem;

import java.util.UUID;

public class ReviveCommand {
    public static int execute(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer) {
        ServerPlayer executor = context.getSource().getPlayer();
        if (executor == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player"));
            return 0;
        }

        ServerLevel level = context.getSource().getLevel();

        // Check if the player has any graves in data store
        UUID targetUUID = targetPlayer.getUUID();
        if (GraveDataStore.getGravesForOwner(level, targetUUID).isEmpty()) {
            context.getSource().sendFailure(Component.literal("Player " + targetPlayer.getName().getString() + " has no graves to revive from"));
            return 0;
        }

        // Revive the player
        revivePlayer(targetPlayer, level);

        context.getSource().sendSuccess(
            () -> Component.literal("Revived ").withStyle(ChatFormatting.GREEN)
                .append(targetPlayer.getDisplayName().copy().withStyle(ChatFormatting.AQUA)),
            true
        );

        targetPlayer.displayClientMessage(
            Component.literal("You have been revived by ").append(executor.getDisplayName()),
            false
        );

        return 1;
    }

    private static void revivePlayer(ServerPlayer player, ServerLevel level) {
        // Get graves from data store
        UUID playerUUID = player.getUUID();
        var graves = GraveDataStore.getGravesForOwner(level, playerUUID);

        if (graves.isEmpty()) {
            // Fallback: just revive without grave interaction
            player.setGameMode(GameType.SURVIVAL);
            player.setHealth(player.getMaxHealth() * 0.5f);
            return;
        }

        // Get first grave ID
        UUID graveId = graves.iterator().next();

        // Find the grave entity to get its location
        GraveEntity targetGrave = null;
        for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
            if (entity instanceof GraveEntity grave && grave.getGraveId() != null && grave.getGraveId().equals(graveId)) {
                targetGrave = grave;
                break;
            }
        }

        // Restore inventory from grave data
        var stored = GraveDataStore.consumeInventory(level, playerUUID, graveId);
        if (stored != null) {
            player.getInventory().clearContent();
            for (int i = 0; i < stored.size(); i++) {
                player.getInventory().setItem(i, stored.get(i));
            }
        }

        // Restore stats
        float reviveHealth = HealthSystem.getReviveHealthForItem(player, 12.0f, 2.0f);
        HealthSystem.setPlayerMaxHealth(player, reviveHealth);
        HealthSystem.applyReviveDegradationAfterRevive(player);

        // Convert to survival and teleport to grave
        player.setGameMode(GameType.SURVIVAL);
        if (targetGrave != null) {
            player.teleportTo(targetGrave.getX(), targetGrave.getY() + 1, targetGrave.getZ());
            targetGrave.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
    }
}

