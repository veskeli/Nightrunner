package net.veskeli.nightrunner.skills;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.SpellSystem.Attachment.SpellAttachment;
import net.veskeli.nightrunner.SpellSystem.Spell;

import java.util.Arrays;

public class SpellSelectorScreen extends Screen {

    private static final ResourceLocation SKILL_TREE_EXAMPLE =
            ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/bottle_of_experience.png");
    private final ResourceLocation CENTER_SKILL =
            ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/diamond_wand.png");

    private SpellAttachment attachment;
    private Player player;

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public SpellSelectorScreen(SpellAttachment attachment, Player player) {
        super(Component.literal("Skill Tree"));

        // Set the attachment
        this.attachment = attachment;
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();

        int widgetSize = 32;
        int centerX = width / 2 - widgetSize / 2;
        int centerY = height / 2 - widgetSize / 2;

        // Add center skill
        SpellSelectorWidget centerSkillWidget = new SpellSelectorWidget(centerX, centerY, widgetSize, widgetSize, CENTER_SKILL, attachment, player);
        // bind on click event
        centerSkillWidget.setOnClickHandler(this::onWidgetClicked);
        addRenderableWidget(centerSkillWidget);

        // Calculate radius
        double radius = widgetSize * 2; // Adjust the radius as needed

        // Add 6 surrounding skills in a circle
        for (int i = 0; i < 6; i++) {

            Spell spell = attachment.getSpell(i);

            double angle = Math.toRadians(i * 60); // 0°, 60°, ..., 300°
            int x = (int) (centerX + 8 + radius * Math.cos(angle)) - 8;
            int y = (int) (centerY + 8 + radius * Math.sin(angle)) - 8;

            // Create the widget for the spell
            if (spell != null) {
                SpellSelectorWidget skillWidget = new SpellSelectorWidget(x, y, widgetSize, widgetSize, i, attachment, player);
                // bind on click event
                skillWidget.setOnClickHandler(this::onWidgetClicked);
                addRenderableWidget(skillWidget);
            }
        }
    }

    protected void onWidgetClicked(){
        // Close the screen when a widget is clicked
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        // Reinitialize the screen with the new dimensions
        this.width = width;
        this.height = height;
        this.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

}
