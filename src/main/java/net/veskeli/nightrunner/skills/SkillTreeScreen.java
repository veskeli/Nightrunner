package net.veskeli.nightrunner.skills;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;

import java.lang.reflect.Field;
import java.util.Map;

public class SkillTreeScreen extends AdvancementsScreen {
    public SkillTreeScreen(ClientAdvancements advancements) {
        super(advancements);

        // remove all tabs not from our mod’s namespace
        try {
            Field tabsField = AdvancementsScreen.class.getDeclaredField("tabs");
            tabsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Advancement, AdvancementTab> tabs =
                    (Map<Advancement, AdvancementTab>) tabsField.get(this);
            /*tabs.entrySet().removeIf(e ->
                    !e.getKey().name()
            );*/ // !e.getKey().getId().getNamespace().equals("nightrunner_difficulty")
            System.out.println("Tabs: " + tabs.size());
            // Print names
            for (Map.Entry<Advancement, AdvancementTab> entry : tabs.entrySet()) {
                System.out.println("Advancement: " + entry.getKey().name());
            }
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // TODO: Render skill points
    }
}
