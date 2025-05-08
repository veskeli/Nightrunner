package net.veskeli.nightrunner.skills;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.SpellSystem.Attachment.SpellAttachment;
import net.veskeli.nightrunner.SpellSystem.Spell;

public class SpellSelectorWidget extends AbstractWidget {

    public final ResourceLocation texture;
    private final int width;
    private final int height;
    private final int x;
    private final int y;
    private SpellAttachment attachment;
    private int index;
    private Spell spell;

    public SpellSelectorWidget(int x, int y, int width, int height, ResourceLocation texture, SpellAttachment attachment)
    {
        super(x, y, width, height, Component.literal("Skill Tree Widget"));

        this.texture = texture;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        // Set the attachment to the widget
        this.attachment = attachment;
    }

    public SpellSelectorWidget(int x, int y, int width, int height, int index, SpellAttachment attachment)
    {
        super(x, y, width, height, Component.literal("Skill Tree Widget"));


        this.index = index;
        this.spell = attachment.getSpell(index);
        this.texture = this.spell.getSpellTexture();
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        // Set the attachment to the widget
        this.attachment = attachment;
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
        if (onClickHandler != null) {
            onClickHandler.onClick();
        }
        super.onClick(mouseX, mouseY, button);
    }

    private OnClickHandler onClickHandler;

    public void setOnClickHandler(OnClickHandler onClickHandler) {
        this.onClickHandler = onClickHandler;
    }
}
