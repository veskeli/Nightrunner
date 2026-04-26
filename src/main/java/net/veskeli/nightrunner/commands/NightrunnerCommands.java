package net.veskeli.nightrunner.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
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
                .then(
                    Commands.literal("summon")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("mob_id", ResourceLocationArgument.id())
                            .suggests(SummonCommand.MOB_ID_SUGGESTIONS)
                            .then(Commands.argument("pos", Vec3Argument.vec3())
                                .then(Commands.argument("preset_id", StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        try {
                                            var mobId = ResourceLocationArgument.getId(context, "mob_id");
                                            String remaining = builder.getRemaining().toLowerCase();
                                            for (String preset : SummonCommand.getPresetsForMob(context.getSource(), mobId)) {
                                                if (preset.toLowerCase().startsWith(remaining)) {
                                                    builder.suggest(preset);
                                                }
                                            }
                                        } catch (Exception ignored) {
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> SummonCommand.execute(context,
                                        ResourceLocationArgument.getId(context, "mob_id"),
                                        Vec3Argument.getVec3(context, "pos"),
                                        StringArgumentType.getString(context, "preset_id")))
                                )
                            )
                        )
                )
        );
    }
}



