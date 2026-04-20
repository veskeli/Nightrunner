package net.veskeli.nightrunner.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.healthsystem.GraveDataStore;

import java.util.Set;
import java.util.UUID;

public class ClearGravesCommand {

    public static int executeForPlayer(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer) {
        ServerLevel level = context.getSource().getLevel();
        UUID playerUUID = targetPlayer.getUUID();

        // Get all graves for this player
        Set<UUID> graves = GraveDataStore.getGravesForOwner(level, playerUUID);
        int graveCount = graves.size();

        // Remove all graves from data store
        for (UUID graveId : graves) {
            GraveDataStore.removeGrave(level, playerUUID, graveId);
        }

        // Remove grave entities
        for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
            if (entity instanceof GraveEntity grave && grave.getOwner() != null && grave.getOwner().equals(playerUUID)) {
                grave.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }
        }

        context.getSource().sendSuccess(
            () -> Component.literal("Cleared ")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal(String.valueOf(graveCount)).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" grave(s) for ").withStyle(ChatFormatting.RED))
                .append(targetPlayer.getDisplayName().copy().withStyle(ChatFormatting.AQUA)),
            true
        );

        return 1;
    }

    public static int executeAll(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();

        // Get all grave entities in the level
        java.util.List<GraveEntity> allGraves = new java.util.ArrayList<>();
        for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
            if (entity instanceof GraveEntity grave && grave.getOwner() != null) {
                allGraves.add(grave);
            }
        }

        int totalGraves = allGraves.size();

        // Remove all graves from data store and entities
        for (GraveEntity grave : allGraves) {
            UUID ownerId = grave.getOwner();
            UUID graveId = grave.getGraveId();
            if (ownerId != null && graveId != null) {
                GraveDataStore.removeGrave(level, ownerId, graveId);
            }
            grave.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }

        context.getSource().sendSuccess(
            () -> Component.literal("Cleared all ")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal(String.valueOf(totalGraves)).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" grave(s) in the world").withStyle(ChatFormatting.RED)),
            true
        );

        return 1;
    }
}

