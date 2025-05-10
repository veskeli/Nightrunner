package net.veskeli;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.ModAttachments;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Set spell level
        dispatcher.register(net.minecraft.commands.Commands.literal("SetSpellLevel")
                .then(Commands.argument("Player", EntityArgument.player())
                        .then(Commands.argument("Amount", IntegerArgumentType.integer(0, 100))
                                .requires(source -> source.hasPermission(2)) // Permission level (2 = OP)
                                .executes(context -> {
                                    Player player = EntityArgument.getPlayer(context, "Player");
                                    int amount = IntegerArgumentType.getInteger(context, "Amount");

                                    Mana mana = player.getData(ModAttachments.PLAYER_MANA);
                                    mana.setSpellLevel(amount);

                                    player.setData(ModAttachments.PLAYER_MANA, mana);

                                    System.out.println("Is Server: " + context.getSource().getLevel().isClientSide());

                                    return 1; // Return a success code
                                }))));

        // Reset mana stats
        dispatcher.register(net.minecraft.commands.Commands.literal("ResetMana")
                .then(Commands.argument("Player", EntityArgument.player())
                        .requires(source -> source.hasPermission(2)) // Permission level (2 = OP)
                        .executes(context -> {
                            Player player = EntityArgument.getPlayer(context, "Player");

                            Mana mana = player.getData(ModAttachments.PLAYER_MANA);

                            // Reset mana stats
                            mana.setMaxMana(Mana.MAX_MANA);
                            mana.setMana(Mana.MAX_MANA);
                            mana.setMaxSpellAmount(Mana.MAX_SPELL_AMOUNT);
                            mana.setSpellLevel(Mana.MAX_SPELL_AMOUNT);

                            player.setData(ModAttachments.PLAYER_MANA, mana);

                            System.out.println("Is Server: " + context.getSource().getLevel().isClientSide());

                            return 1; // Return a success code
                        })));
    }
}
