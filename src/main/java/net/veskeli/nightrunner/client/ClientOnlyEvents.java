package net.veskeli.nightrunner.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.veskeli.nightrunner.ModAttachments;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.SpellSystem.Attachment.SpellAttachment;
import net.veskeli.nightrunner.SpellSystem.ModSpells;
import net.veskeli.nightrunner.skills.SpellSelectorScreen;

import static net.veskeli.nightrunner.Nightrunner.ClientModEvents.SKILL_TREE_MAPPING;

@Mod(value = Nightrunner.MODID, dist = Dist.CLIENT)
public class ClientOnlyEvents
{
    public static void onPlayerTick(PlayerTickEvent.Post event) {

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
            spellAttachment.setSpell(5, ModSpells.Longstrider.get());

            event.getEntity().setData(ModAttachments.PLAYER_SPELLS, spellAttachment);

            // Get the Sell Attachment
            //SpellAttachment spellAttachment = event.getEntity().getData(ModAttachments.PLAYER_SPELLS);
            // Draw the skill tree screen
            mc.setScreen(new SpellSelectorScreen(spellAttachment, event.getEntity()));
        }
    }
}
