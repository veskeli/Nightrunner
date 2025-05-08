package net.veskeli.nightrunner.skills;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.veskeli.nightrunner.Nightrunner;

import java.util.Arrays;

public class SkillTreeScreen extends Screen {

    private final int imageWidth = 176;
    private final int imageHeight = 166;
    private int localX;
    private int localY;

    private static final ResourceLocation SKILL_TREE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/gui/testing_gui.png");

    private static final ResourceLocation SKILL_TREE_EXAMPLE =
            ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/bottle_of_experience.png");
    private final ResourceLocation CENTER_SKILL =
            ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/bottle_of_experience.png");
    private final ResourceLocation[] SKILL_TREE_LIST;

    public SkillTreeScreen() {
        super(Component.literal("Skill Tree"));

        // update local coordinates
        updateLocalCoordinates();

        // Initialize the skill tree list
        SKILL_TREE_LIST = new ResourceLocation[6];
        Arrays.fill(SKILL_TREE_LIST, SKILL_TREE_EXAMPLE);
    }

    private void updateLocalCoordinates() {
        localX = (width - imageWidth) / 2;
        localY = (height - imageHeight) / 2;
    }

    @Override
    protected void init() {
        super.init();
        // Update local coordinates
        updateLocalCoordinates();

        int centerX = width / 2 - 8;
        int centerY = height / 2 - 8;

        // Add center skill
        addRenderableWidget(new SkillTreeWidget(centerX, centerY, 16, 16, CENTER_SKILL));

        // Calculate radius as 40% of the smaller dimension
        double radius = 0.4 * Math.min(imageWidth, imageHeight);

        // Add 6 surrounding skills in a circle
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60); // 0°, 60°, ..., 300°
            int x = (int) (centerX + 8 + radius * Math.cos(angle)) - 8;
            int y = (int) (centerY + 8 + radius * Math.sin(angle)) - 8;
            SkillTreeWidget skillWidget = new SkillTreeWidget(x, y, 16, 16, SKILL_TREE_LIST[i]);
            // bind on click event
            skillWidget.onMouseClick(() -> {
                // Call your custom function here

            });
            addRenderableWidget(skillWidget);


        }
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Convert to local space
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int localMouseX = (int) (mouseX - x);
        int localMouseY = (int) (mouseY - y);

        return super.mouseClicked(mouseX, mouseY, button);
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
        RenderSystem.setShaderTexture(0, SKILL_TREE_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(SKILL_TREE_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

}
