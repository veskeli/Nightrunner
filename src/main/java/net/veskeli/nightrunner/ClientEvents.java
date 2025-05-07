package net.veskeli.nightrunner;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.item.custom.WandItem;
import net.veskeli.nightrunner.skills.SkillTreeScreen;

import java.util.Set;
import java.util.stream.Collectors;

import static net.veskeli.nightrunner.Nightrunner.ClientModEvents.SKILL_TREE_MAPPING;

public class ClientEvents {

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        ShowManaWhenHoldingCorrectItem(event);

        CheckForOpenSkillTreeKeybind(event);
    }

    private static void CheckForOpenSkillTreeKeybind(PlayerTickEvent.Post event) {
        // If server side, return
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            System.out.println("Minecraft instance or player is null");
            return;
        }
        if (SKILL_TREE_MAPPING.get().consumeClick()) {
            // 1) Get the current advancement tree
            ClientAdvancements advancements = mc.player.connection.getAdvancements();

            AdvancementTree tree = advancements.getTree();

            // 2) Build a set of all "minecraft" advancement IDs
            Set<ResourceLocation> toRemove = tree.nodes().stream()
                    .map(node -> {
                        AdvancementHolder holder = node.holder();  // each node’s holder
                        return holder.id();                          // the ResourceLocation ID
                    })
                    .filter(id -> id.getNamespace().equals("minecraft"))
                    .collect(Collectors.toSet());

            // 3) Remove them
            tree.remove(toRemove);

            // 4) Create a new screen with the modified tree
            mc.setScreen(new SkillTreeScreen(mc.player.connection.getAdvancements()));
        }
    }

    private static void ShowManaWhenHoldingCorrectItem(PlayerTickEvent.Post event) {
        // return if server side
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        // Check if the player is holding a specific item
        if (event.getEntity().getMainHandItem().getItem() instanceof WandItem)
        {
            // Get the mana data from the player
            Mana mana = event.getEntity().getData(ModAttachments.PLAYER_MANA);
            MutableComponent manaText = Component.literal("Mana: ").withStyle(style -> style.withColor(ChatFormatting.AQUA).withBold(true))
                    .append(Component.literal(String.valueOf(mana.getMana()))
                            .withStyle(style -> style.withColor(ChatFormatting.BLUE).withBold(true)))
                    .append(Component.literal(" / ")
                            .withStyle(style -> style.withColor(ChatFormatting.GRAY)))
                    .append(Component.literal(String.valueOf(mana.getMaxMana()))
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN).withBold(true)));

            if (mana.getMana() < mana.getMaxMana()) {
                manaText.append(Component.literal(" ⏳ ")
                                .withStyle(style -> style.withColor(ChatFormatting.GOLD)))
                        .append(Component.literal(String.valueOf(mana.getCurrentRecharge()))
                                .withStyle(style -> style.withColor(ChatFormatting.YELLOW).withBold(false)))
                        .append(Component.literal("t")
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
            }

            event.getEntity().displayClientMessage(manaText, true);

            // Show mana on action bar
            event.getEntity().displayClientMessage(manaText, true);
        }
    }
}
