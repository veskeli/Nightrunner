package net.veskeli.nightrunner.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.healthsystem.GraveDataStore;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GraveInfoCommand {

    public static int execute(CommandContext<CommandSourceStack> context, String targetStr) {
        ServerLevel level = context.getSource().getLevel();

        // First, try to parse as a UUID (grave ID)
        UUID targetId;
        try {
            targetId = UUID.fromString(targetStr);
            // If it's a valid UUID, search for graves with this ID
            return showGraveInfo(context, level, targetId);
        } catch (IllegalArgumentException e) {
            // Not a UUID, try as player name
            ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayerByName(targetStr);
            if (targetPlayer == null) {
                context.getSource().sendFailure(Component.literal("Could not find grave or player: " + targetStr));
                return 0;
            }
            return showPlayerGravesInfo(context, level, targetPlayer);
        }
    }

    private static int showGraveInfo(CommandContext<CommandSourceStack> context, ServerLevel level, UUID graveId) {
        // Check if grave exists in data
        if (!GraveDataStore.hasGrave(level, graveId)) {
            context.getSource().sendFailure(Component.literal("Grave not found: " + graveId));
            return 0;
        }

        // Find the grave entity
        GraveEntity grave = null;
        for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
            if (entity instanceof GraveEntity g && g.getGraveId() != null && g.getGraveId().equals(graveId)) {
                grave = g;
                break;
            }
        }

        if (grave == null) {
            context.getSource().sendFailure(Component.literal("Grave entity not found: " + graveId));
            return 0;
        }
        UUID ownerId = grave.getOwner();
        if (ownerId == null) {
            context.getSource().sendFailure(Component.literal("Grave has no owner"));
            return 0;
        }

        // Get owner name
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        String ownerName = owner != null ? owner.getName().getString() : ownerId.toString();

        // Get items
        List<ItemStack> items = GraveDataStore.peekInventory(level, graveId);

        // Build message
        net.minecraft.network.chat.MutableComponent message = Component.literal("\n========== GRAVE INFO ==========\n")
            .withStyle(ChatFormatting.GOLD);

        message = message.append(
            Component.literal("Grave ID: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(graveId.toString()).withStyle(ChatFormatting.AQUA))
                .append("\n")
        );

        message = message.append(
            Component.literal("Owner: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(ownerName).withStyle(ChatFormatting.AQUA))
                .append("\n")
        );

        message = message.append(
            Component.literal("Location: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(String.format("X: %.1f Y: %.1f Z: %.1f", grave.getX(), grave.getY(), grave.getZ()))
                    .withStyle(ChatFormatting.AQUA))
                .append("\n")
                .append(Component.literal("Dimension: ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(level.dimension().location().toString()).withStyle(ChatFormatting.AQUA))
                .append("\n")
        );

        message = message.append(
            Component.literal("\nItems (").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(String.valueOf(items != null ? items.size() : 0)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("):").withStyle(ChatFormatting.YELLOW))
                .append("\n")
        );

        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (!item.isEmpty()) {
                    message = message.append(
                        Component.literal("  [" + i + "] ").withStyle(ChatFormatting.GRAY)
                            .append(item.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" x" + item.getCount()).withStyle(ChatFormatting.DARK_GRAY))
                            .append("\n")
                    );
                }
            }
        } else {
            message = message.append(Component.literal("  (Empty)").withStyle(ChatFormatting.DARK_GRAY).append("\n"));
        }

        message = message.append(Component.literal("================================\n").withStyle(ChatFormatting.GOLD));

        net.minecraft.network.chat.MutableComponent finalMessage = message;
        context.getSource().sendSuccess(() -> finalMessage, false);
        return 1;
    }

    private static int showPlayerGravesInfo(CommandContext<CommandSourceStack> context, ServerLevel level, ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        Set<UUID> playerGraves = GraveDataStore.getGravesForOwner(level, playerUUID);

        if (playerGraves.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Player " + player.getName().getString() + " has no graves"));
            return 0;
        }

        if (playerGraves.size() == 1) {
            // If only one grave, show its info
            UUID graveId = playerGraves.iterator().next();
            return showGraveInfo(context, level, graveId);
        } else {
            // Multiple graves, show list
            net.minecraft.network.chat.MutableComponent message = Component.literal("\n========== GRAVES FOR " + player.getName().getString().toUpperCase() + " ==========\n")
                .withStyle(ChatFormatting.GOLD);

            for (UUID graveId : playerGraves) {
                GraveEntity grave = null;
                for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
                    if (entity instanceof GraveEntity g && g.getGraveId() != null && g.getGraveId().equals(graveId)) {
                        grave = g;
                        break;
                    }
                }

                if (grave == null) continue;

                message = message.append(
                    Component.literal("Grave: ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(graveId.toString()).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" at X: ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(String.format("%.1f", grave.getX())).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(", Y: ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(String.format("%.1f", grave.getY())).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(", Z: ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(String.format("%.1f", grave.getZ())).withStyle(ChatFormatting.AQUA))
                        .append("\n")
                );
            }

            message = message.append(Component.literal("================================\n").withStyle(ChatFormatting.GOLD));

            net.minecraft.network.chat.MutableComponent finalMessage = message;
            context.getSource().sendSuccess(() -> finalMessage, false);
            return 1;
        }
    }
}

