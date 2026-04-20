package net.veskeli.nightrunner.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.veskeli.nightrunner.commands.subcommands.*;

public class NightrunnerCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Main /nr command with subcommands
        dispatcher.register(
            Commands.literal("nr")
                .then(
                    Commands.literal("revive")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(context -> ReviveCommand.execute(context, EntityArgument.getPlayer(context, "player"))))
                )
                .then(
                    Commands.literal("selfrevive")
                        .executes(SelfReviveCommand::execute)
                )
                .then(
                    Commands.literal("cleargraves")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> ClearGravesCommand.executeAll(context))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(context -> ClearGravesCommand.executeForPlayer(context, EntityArgument.getPlayer(context, "player"))))
                )
                .then(
                    Commands.literal("cleargrave")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("grave", StringArgumentType.greedyString())
                                .executes(context -> ClearGraveCommand.execute(context,
                                    EntityArgument.getPlayer(context, "player"),
                                    StringArgumentType.getString(context, "grave")))))
                )
                .then(
                    Commands.literal("graveinfo")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", StringArgumentType.greedyString())
                            .executes(context -> GraveInfoCommand.execute(context, StringArgumentType.getString(context, "target"))))
                )
                .then(
                    Commands.literal("graves")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> GravesCommand.executeAll(context))
                        .then(Commands.argument("players", EntityArgument.players())
                            .executes(context -> GravesCommand.executeForPlayers(context, EntityArgument.getPlayers(context, "players"))))
                )
                .then(
                    Commands.literal("cleanorphaned")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> CleanOrphanedGravesCommand.execute(context))
                )
                .then(
                    Commands.literal("clearbugged")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> ClearBuggedGravesCommand.execute(context))
                )
        );
    }
}

