package net.veskeli.nightrunner.skills;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SkillTreeWidget extends AbstractWidget {

    public final ResourceLocation texture;
    private final int width;
    private final int height;
    private final int x;
    private final int y;

    public SkillTreeWidget(int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height, Component.literal("Skill Tree Widget"));

        this.texture = texture;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Render the texture
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Use blit with proper texture coordinates
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);

        // Disable blending after rendering
        RenderSystem.disableBlend();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        onMouseClick();
        super.onClick(mouseX, mouseY, button);
    }

    public void onMouseClick() {

    }
}
