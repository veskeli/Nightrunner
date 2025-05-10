package net.veskeli.nightrunner.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.veskeli.nightrunner.ManaSystem.EntityManaOrb;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.entity.custom.ManaOrbEntity;
import org.checkerframework.checker.signature.qual.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ManaOrbRenderer extends EntityRenderer<ManaOrbEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/entity/mana_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(TEXTURE);

    public ManaOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaOrbEntity manaOrbEntity) {
        return TEXTURE;
    }

    @Override
    public void render(ManaOrbEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        int icon = entity.getIcon(); // You'll need to implement this based on XP logic
        float u0 = (float)(icon % 4 * 16) / 64.0F;
        float u1 = (float)(icon % 4 * 16 + 16) / 64.0F;
        float v0 = (float)(icon / 4 * 16) / 64.0F;
        float v1 = (float)(icon / 4 * 16 + 16) / 64.0F;

        float tickTime = ((float)entity.tickCount + partialTicks) / 2.0F;
        int red   = (int)((Mth.sin(tickTime + 0.0F) + 1.0F) * 0.2F * 255.0F);      // Low red
        int green = (int)((Mth.sin(tickTime + (float)Math.PI / 2) + 1.0F) * 0.1F * 255.0F); // Very low green
        int blue  = (int)((Mth.sin(tickTime + (float)Math.PI) + 1.0F) * 0.6F * 255.0F);     // Stronger blue

        poseStack.translate(0.0F, 0.1F, 0.0F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.3F, 0.3F, 0.3F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();

        vertex(vertexConsumer, pose, -0.5F, -0.25F, red, green, blue, u0, v1, packedLight);
        vertex(vertexConsumer, pose,  0.5F, -0.25F, red, green, blue, u1, v1, packedLight);
        vertex(vertexConsumer, pose,  0.5F,  0.75F, red, green, blue, u1, v0, packedLight);
        vertex(vertexConsumer, pose, -0.5F,  0.75F, red, green, blue, u0, v0, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, int red, int green, int blue, float u, float v, int light) {
        consumer.addVertex(pose.pose(), x, y, 0.0F)
                .setColor(red, green, blue, 128)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

}

