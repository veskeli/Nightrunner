package net.veskeli.nightrunner.networking;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.ModAttachments;

public class ServerPayloadHandler {
    public static void handleDataOnMain(final ManaData data, final IPayloadContext context) {
        // Do not sync the data to the server
    }
}
