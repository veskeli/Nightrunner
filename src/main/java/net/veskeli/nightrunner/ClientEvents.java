package net.veskeli.nightrunner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.SpellSystem.Attachment.SpellAttachment;
import net.veskeli.nightrunner.SpellSystem.ModSpells;
import net.veskeli.nightrunner.SpellSystem.Spell;
import net.veskeli.nightrunner.SpellSystem.Spells.FireballSpell;
import net.veskeli.nightrunner.item.ModItems;
import net.veskeli.nightrunner.item.custom.WandItem;
import net.veskeli.nightrunner.skills.SpellSelectorScreen;

import static net.veskeli.nightrunner.Nightrunner.ClientModEvents.SKILL_TREE_MAPPING;

public class ClientEvents {

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        // Convert mana orbs to spell levels
        if(!event.getEntity().level().isClientSide())
        {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            // Check if player inventory contains mana orbs
            if (player.getInventory().contains(ModItems.ManaOrb.get().getDefaultInstance())) {
                // Get the mana data from the player
                Mana mana = player.getData(ModAttachments.PLAYER_MANA);
                // Get the amount of mana orbs in the inventory
                int manaOrbs = player.getInventory().countItem(ModItems.ManaOrb.get());
                System.out.println("Mana orbs: " + manaOrbs);
                // Convert them to spell levels
                mana.regenSpellSlots(manaOrbs);
                // Get the index of the mana orbs in the inventory
                int manaOrbIndex = player.getInventory().findSlotMatchingItem(ModItems.ManaOrb.get().getDefaultInstance());
                // Remove the mana orbs from the inventory
                player.getInventory().removeItem(manaOrbIndex, manaOrbs);
                // Set the mana data to the player
                player.setData(ModAttachments.PLAYER_MANA, mana);

                // Send the mana data to the client
                Mana.replicateData(mana, player);
            }
        }

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

            // TEMP HARD CODED FOR TESTING
            SpellAttachment spellAttachment = event.getEntity().getData(ModAttachments.PLAYER_SPELLS);

            spellAttachment.setSpell(0, ModSpells.FIREBALL.get());
            spellAttachment.setSpell(1, ModSpells.ICEKNIFE.get());

            event.getEntity().setData(ModAttachments.PLAYER_SPELLS, spellAttachment);

            // Get the Sell Attachment
            //SpellAttachment spellAttachment = event.getEntity().getData(ModAttachments.PLAYER_SPELLS);
            // Draw the skill tree screen
            mc.setScreen(new SpellSelectorScreen(spellAttachment, event.getEntity()));
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
            Mana mana = event.getEntity().getData(ModAttachments.PLAYER_MANA);
            MutableComponent manaText;
            // Spell level
            manaText = Component.literal("Spell Level: ")
                    .withStyle(style -> style.withColor(ChatFormatting.AQUA).withBold(true))
                    .append(Component.literal(String.valueOf(mana.getSpellAmount()))
                            .withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE).withBold(true)))
                    .append(Component.literal(" / ")
                            .withStyle(style -> style.withColor(ChatFormatting.GRAY)))
                    .append(Component.literal(String.valueOf(mana.getMaxSpellAmount()))
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN).withBold(true)));

            // Separator
            manaText.append(Component.literal(" | ")
                    .withStyle(style -> style.withColor(ChatFormatting.WHITE)));

            // Get the mana data from the player
            manaText.append(Component.literal("Mana: ").withStyle(style -> style.withColor(ChatFormatting.AQUA).withBold(true)))
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
