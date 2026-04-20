package net.veskeli.nightrunner.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.healthsystem.GraveDataStore;

import java.util.UUID;

public class ClearBuggedGravesCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();

        int removedCount = 0;

        // Get all grave entities in the level
        for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
            if (entity instanceof GraveEntity grave) {
                UUID ownerId = grave.getOwner();
                UUID graveId = grave.getGraveId();

                // Check if the grave has null owner or grave ID (bugged grave)
                if (ownerId == null || graveId == null) {
                    grave.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                    removedCount++;
                    continue;
                }

                // Check if the grave data doesn't exist in the store (mismatch)
                if (!GraveDataStore.hasGrave(level, graveId)) {
                    grave.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                    removedCount++;
                }
            }
        }

        if (removedCount == 0) {
            context.getSource().sendSuccess(
                () -> Component.literal("No bugged graves found.").withStyle(ChatFormatting.GREEN),
                true
            );
        } else {
            int finalRemovedCount = removedCount;
            context.getSource().sendSuccess(
                () -> Component.literal("Removed ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(String.valueOf(finalRemovedCount)).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" bugged grave entity/entities.").withStyle(ChatFormatting.RED)),
                true
            );
        }

        return 1;
    }
}

