package net.veskeli.nightrunner.skills;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.Nightrunner;

public class SkillTreeScreen extends Screen {

    private final int imageWidth = 176;
    private final int imageHeight = 166;

    private static final ResourceLocation SKILL_TREE_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/gui/testing_gui.png");

    protected SkillTreeScreen(Component title) {
        super(title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        // Render the background
        //renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // Render the title
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Set the color to white with full opacity
        int alpha = 255; // Full opacity
        int red = 255; // Red
        int green = 255; // Green
        int blue = 255; // Blue
        int color = (alpha << 24) | (red << 16) | (green << 8) | blue;

        //guiGraphics.drawString(Minecraft.getInstance().font, "Skill Tree", x + 4, y + 4, color);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
        RenderSystem.setShaderTexture(0, SKILL_TREE_BACKGROUND);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(SKILL_TREE_BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);
    }
}
