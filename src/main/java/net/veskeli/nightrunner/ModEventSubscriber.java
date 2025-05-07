package net.veskeli.nightrunner;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.veskeli.nightrunner.networking.ClientPayloadHandler;
import net.veskeli.nightrunner.networking.ManaSyncPacket;
import net.veskeli.nightrunner.networking.ServerPayloadHandler;
import net.veskeli.nightrunner.networking.TogglePronePacket;

@EventBusSubscriber(modid = Nightrunner.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // “1” is your network version—must match on client & server
        PayloadRegistrar registrar = event.registrar("1");
        // Register a server→client only packet
        registrar.playToClient(
                ManaSyncPacket.TYPE,
                ManaSyncPacket.STREAM_CODEC,
                // Handler on client main thread:
                new DirectionalPayloadHandler<>(ClientPayloadHandler::handleDataOnMain, null)
        );
        // Register a client→server only packet | Prone toggle
        registrar.playToServer(
                TogglePronePacket.TYPE,
                TogglePronePacket.STREAM_CODEC,
                // Handler on server main thread:
                new DirectionalPayloadHandler<>(null, ServerPayloadHandler::onToggleProne)
        );
    }
}
