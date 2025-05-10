package net.veskeli.nightrunner.events;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.client.FireBallRenderer;
import net.veskeli.nightrunner.entity.client.GraveModel;
import net.veskeli.nightrunner.entity.client.NoopRenderer;
import net.veskeli.nightrunner.entity.custom.GraveEntity;

@EventBusSubscriber(modid = Nightrunner.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventsBusEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GraveModel.LAYER_LOCATION, GraveModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.GRAVE.get(), GraveEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.WAND_PROJECTILE.get(), FireBallRenderer::new);
        EntityRenderers.register(ModEntities.ICE_KNIFE_PROJECTILE.get(), NoopRenderer::new);
    }
}
