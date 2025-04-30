package net.veskeli.nightrunner.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.client.GraveModel;
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
}
