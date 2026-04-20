package net.veskeli.nightrunner.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.healthsystem.GraveDataStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GravesCommand {

    public static int executeAll(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();

        // Get all grave entities in the level
        java.util.List<GraveEntity> allGraves = new java.util.ArrayList<>();
        for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
            if (entity instanceof GraveEntity grave && grave.getOwner() != null && grave.getGraveId() != null) {
                allGraves.add(grave);
            }
        }

        if (allGraves.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No graves found in this world"));
            return 0;
        }

        // Group graves by owner
        Map<UUID, List<GraveEntity>> gravesByOwner = new HashMap<>();
        for (GraveEntity grave : allGraves) {
            gravesByOwner.computeIfAbsent(grave.getOwner(), k -> new java.util.ArrayList<>()).add(grave);
        }

        // Build message
        net.minecraft.network.chat.MutableComponent message = Component.literal("\n========== ALL GRAVES IN WORLD ==========\n")
            .withStyle(ChatFormatting.GOLD);

        for (Map.Entry<UUID, List<GraveEntity>> entry : gravesByOwner.entrySet()) {
            UUID ownerId = entry.getKey();
            List<GraveEntity> ownerGraves = entry.getValue();

            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
            String ownerName = owner != null ? owner.getName().getString() : ownerId.toString();

            message = message.append(
                Component.literal("\n" + ownerName).withStyle(ChatFormatting.AQUA).withStyle(s -> s.withBold(true))
                    .append(Component.literal(" (" + ownerGraves.size() + " grave" + (ownerGraves.size() > 1 ? "s" : "") + ")").withStyle(ChatFormatting.GRAY))
                    .append("\n")
            );

            for (GraveEntity grave : ownerGraves) {
                String graveId = grave.getGraveId().toString();
                String locationStr = String.format("X: %.1f Y: %.1f Z: %.1f", grave.getX(), grave.getY(), grave.getZ());

                // Create clickable component with copy to clipboard on hover
                net.minecraft.network.chat.MutableComponent graveComponent = Component.literal("  • ")
                    .append(Component.literal(graveId).withStyle(
                        Style.EMPTY
                            .withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent(
                                ClickEvent.Action.COPY_TO_CLIPBOARD,
                                graveId
                            ))
                            .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to copy grave ID").withStyle(ChatFormatting.GREEN)
                            ))
                    ));

                graveComponent = graveComponent.append(
                    Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(locationStr).withStyle(ChatFormatting.AQUA))
                );

                // Add dimension info
                graveComponent = graveComponent.append(
                    Component.literal(" [" + level.dimension().location().getPath() + "]").withStyle(ChatFormatting.DARK_GRAY)
                );

                graveComponent = graveComponent.append("\n");

                message = message.append(graveComponent);
            }
        }

        message = message.append(Component.literal("=========================================\n").withStyle(ChatFormatting.GOLD));

        net.minecraft.network.chat.MutableComponent finalMessage = message;
        context.getSource().sendSuccess(() -> finalMessage, false);
        return 1;
    }

    public static int executeForPlayers(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        ServerLevel level = context.getSource().getLevel();

        // Build message for specific players
        net.minecraft.network.chat.MutableComponent message = Component.literal("\n========== GRAVES FOR SELECTED PLAYERS ==========\n")
            .withStyle(ChatFormatting.GOLD);

        boolean hasGraves = false;

        for (ServerPlayer player : players) {
            UUID playerUUID = player.getUUID();
            Set<UUID> playerGraves = GraveDataStore.getGravesForOwner(level, playerUUID);

            if (playerGraves.isEmpty()) {
                message = message.append(
                    Component.literal(player.getName().getString()).withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(" - No graves").withStyle(ChatFormatting.GRAY))
                        .append("\n")
                );
                continue;
            }

            hasGraves = true;

            message = message.append(
                Component.literal("\n" + player.getName().getString()).withStyle(ChatFormatting.AQUA).withStyle(s -> s.withBold(true))
                    .append(Component.literal(" (" + playerGraves.size() + " grave" + (playerGraves.size() > 1 ? "s" : "") + ")").withStyle(ChatFormatting.GRAY))
                    .append("\n")
            );

            for (UUID graveId : playerGraves) {
                GraveEntity grave = null;
                for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
                    if (entity instanceof GraveEntity g && g.getGraveId() != null && g.getGraveId().equals(graveId)) {
                        grave = g;
                        break;
                    }
                }

                if (grave == null) continue;

                String graveIdStr = graveId.toString();
                String locationStr = String.format("X: %.1f Y: %.1f Z: %.1f", grave.getX(), grave.getY(), grave.getZ());

                // Create clickable component with copy to clipboard on hover
                net.minecraft.network.chat.MutableComponent graveComponent = Component.literal("  • ")
                    .append(Component.literal(graveIdStr).withStyle(
                        Style.EMPTY
                            .withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent(
                                ClickEvent.Action.COPY_TO_CLIPBOARD,
                                graveIdStr
                            ))
                            .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to copy grave ID").withStyle(ChatFormatting.GREEN)
                            ))
                    ));

                graveComponent = graveComponent.append(
                    Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(locationStr).withStyle(ChatFormatting.AQUA))
                );

                // Add dimension info
                graveComponent = graveComponent.append(
                    Component.literal(" [" + level.dimension().location().getPath() + "]").withStyle(ChatFormatting.DARK_GRAY)
                );

                graveComponent = graveComponent.append("\n");

                message = message.append(graveComponent);
            }
        }

        if (!hasGraves) {
            message = message.append(Component.literal("No graves found for selected players").withStyle(ChatFormatting.GRAY));
        }

        message = message.append(Component.literal("====================================================\n").withStyle(ChatFormatting.GOLD));

        net.minecraft.network.chat.MutableComponent finalMessage = message;
        context.getSource().sendSuccess(() -> finalMessage, false);
        return 1;
    }
}

