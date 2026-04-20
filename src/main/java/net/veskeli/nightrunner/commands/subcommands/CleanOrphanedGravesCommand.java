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

public class CleanOrphanedGravesCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();

        int removedCount = 0;

        // Get all grave entities in the level
        for (net.minecraft.world.entity.decoration.ArmorStand entity : level.getEntities(EntityType.ARMOR_STAND, e -> e instanceof GraveEntity)) {
            if (entity instanceof GraveEntity grave) {
                UUID graveId = grave.getGraveId();

                // Check if this grave has data in the store
                if (graveId == null || !GraveDataStore.hasGrave(level, graveId)) {
                    // This is an orphaned grave (entity exists but no data)
                    grave.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                    removedCount++;
                }
            }
        }

        if (removedCount == 0) {
            context.getSource().sendSuccess(
                () -> Component.literal("No orphaned graves found.").withStyle(ChatFormatting.GREEN),
                true
            );
        } else {
            int finalRemovedCount = removedCount;
            context.getSource().sendSuccess(
                () -> Component.literal("Cleaned ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(String.valueOf(finalRemovedCount)).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" orphaned grave entity/entities.").withStyle(ChatFormatting.GREEN)),
                true
            );
        }

        return 1;
    }
}

