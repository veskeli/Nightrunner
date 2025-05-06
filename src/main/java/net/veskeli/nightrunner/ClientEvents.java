package net.veskeli.nightrunner;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.item.custom.WandItem;

public class ClientEvents {

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        // return if server side
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        // Check if the player is holding a specific item
        if (event.getEntity().getMainHandItem().getItem() instanceof WandItem)
        {
            // Get the mana data from the player
            Mana mana = event.getEntity().getData(ModAttachments.PLAYER_MANA);
            // Show mana on action bar
            event.getEntity().displayClientMessage(
                    Component.literal("Mana: ")
                            .append(Component.literal(String.valueOf(mana.getMana()))
                                    .withStyle(style -> style.withColor(ChatFormatting.BLUE).withBold(true)))
                            .append(Component.literal(" / ")
                                    .withStyle(style -> style.withColor(ChatFormatting.GRAY)))
                            .append(Component.literal(String.valueOf(mana.getMaxMana()))
                                    .withStyle(style -> style.withColor(ChatFormatting.GREEN).withBold(true))),
                                    true);
        }
    }
}
