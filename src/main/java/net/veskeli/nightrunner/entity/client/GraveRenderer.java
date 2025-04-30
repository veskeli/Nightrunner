package net.veskeli.nightrunner.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.entity.custom.GraveEntity;

public class GraveRenderer extends LivingEntityRenderer<GraveEntity, GraveModel<GraveEntity>>
{

    public GraveRenderer(EntityRendererProvider.Context context) {
        super(context, new GraveModel<>(context.bakeLayer(GraveModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(GraveEntity graveEntity) {
        return ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/entity/grave/grave.png");
    }

    @Override
    public void render(GraveEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
}
