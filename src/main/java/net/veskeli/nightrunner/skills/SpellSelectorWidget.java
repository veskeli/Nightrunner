package net.veskeli.nightrunner.skills;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veskeli.nightrunner.ModAttachments;
import net.veskeli.nightrunner.SpellSystem.Attachment.SpellAttachment;
import net.veskeli.nightrunner.SpellSystem.ModSpells;
import net.veskeli.nightrunner.SpellSystem.Spell;
import net.veskeli.nightrunner.networking.SpellSelectPacket;

public class SpellSelectorWidget extends AbstractWidget {

    public final ResourceLocation texture;
    private final int width;
    private final int height;
    private final int x;
    private final int y;
    private final int spellIndex;
    private SpellAttachment attachment;
    private int index;
    private Spell spell;
    private Player player;

    public SpellSelectorWidget(int x, int y, int width, int height, ResourceLocation texture, SpellAttachment attachment, Player player)
    {
        super(x, y, width, height, Component.literal("Skill Tree Widget"));

        this.texture = texture;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        this.index = -1;

        // Set the attachment to the widget
        this.attachment = attachment;
        this.player = player;
        this.spellIndex = 0;
    }

    public SpellSelectorWidget(int x, int y, int width, int height, int index, SpellAttachment attachment, Player player)
    {
        super(x, y, width, height, Component.literal("Skill Tree Widget"));


        this.index = index;
        this.spell = attachment.getSpell(index);
        this.spellIndex = ModSpells.SPELL_REGISTRY.getId(this.spell);
        this.texture = this.spell.getSpellTexture();
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        // Set the attachment to the widget
        this.attachment = attachment;
        this.player = player;
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

        if (index != -1) {
            // Render the spell name
            if (spell != null) {
                Font font = Minecraft.getInstance().font;
                int nameWidth = font.width(spell.getName());
                guiGraphics.drawString(font, spell.getName(), x + (width - nameWidth) / 2, y + height + 5, 0xFFFFFF);
            }

            // Render the spell cost
            if (spell != null) {
                Font font = Minecraft.getInstance().font;
                String costText = "Cost: " + spell.getCost();
                int costWidth = font.width(costText);
                guiGraphics.drawString(font, costText, x + (width - costWidth) / 2, y, 0xFFFFFF);
            }
        } else {
            // Default text for the center skill
            Font font = Minecraft.getInstance().font;
            String defaultText = "Cantrip";
            int defaultWidth = font.width(defaultText);
            guiGraphics.drawString(font, defaultText, x + (width - defaultWidth) / 2, y + height + 5, 0xFFFFFF);
        }

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

        if(index == -1)
        {
            attachment.setSelectedSpell(null);
            System.out.println("Selected spell: null");
            // Send data to server
            SpellSelectPacket packet = new SpellSelectPacket(-1);
            PacketDistributor.sendToServer(packet);
            return;
        }
        else
        {
            // Set selected spell
            if (attachment != null) {
                attachment.setSelectedSpell(spell);
                System.out.println("Selected spell: " + spell.getName());
            }
            else {
                attachment.setSelectedSpell(null);
                System.out.println("Selected spell: null");
            }
        }

        // Send data to player
        if (player != null) {
            // Send data to the player
            player.setData(ModAttachments.PLAYER_SPELLS, attachment);
        }

        // Send data to server
        SpellSelectPacket packet = new SpellSelectPacket(spellIndex);
        PacketDistributor.sendToServer(packet);
    }

    private OnClickHandler onClickHandler;

    public void setOnClickHandler(OnClickHandler onClickHandler) {
        this.onClickHandler = onClickHandler;
    }
}
