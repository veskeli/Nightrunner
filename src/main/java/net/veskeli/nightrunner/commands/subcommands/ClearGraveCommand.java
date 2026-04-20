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

public class ClearGraveCommand {

    public static int execute(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer, String graveIdStr) {
        ServerLevel level = context.getSource().getLevel();
        UUID playerUUID = targetPlayer.getUUID();

        // Try to parse the grave ID
        UUID graveId;
        try {
            graveId = UUID.fromString(graveIdStr);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid grave ID format: " + graveIdStr));
            return 0;
        }

        // Check if the player has this grave
        Set<UUID> playerGraves = GraveDataStore.getGravesForOwner(level, playerUUID);
        if (!playerGraves.contains(graveId)) {
            context.getSource().sendFailure(
                Component.literal("Player ").append(targetPlayer.getDisplayName())
                    .append(Component.literal(" does not have a grave with ID: " + graveIdStr))
            );
            return 0;
        }

        // Remove the grave from data store
        GraveDataStore.removeGrave(level, playerUUID, graveId);

        // Remove the grave entity
        for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
            if (entity instanceof GraveEntity grave && grave.getGraveId() != null && grave.getGraveId().equals(graveId)) {
                grave.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                break;
            }
        }

        context.getSource().sendSuccess(
            () -> Component.literal("Cleared grave ")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal(graveIdStr).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" for ").withStyle(ChatFormatting.RED))
                .append(targetPlayer.getDisplayName().copy().withStyle(ChatFormatting.AQUA)),
            true
        );

        return 1;
    }
}

