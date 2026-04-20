package net.veskeli.nightrunner.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.veskeli.nightrunner.healthsystem.ReviveSystem;

public class SelfReviveCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }

        if (!ReviveSystem.hasPendingSelfRevive(player)) {
            context.getSource().sendFailure(Component.literal("You do not have a pending self revive."));
            return 0;
        }

        if (!ReviveSystem.tryUsePendingSelfRevive(player)) {
            context.getSource().sendFailure(Component.literal("Self revive is only available while you are waiting dead in spectator."));
            return 0;
        }

        return 1;
    }
}

