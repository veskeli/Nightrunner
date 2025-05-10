package net.veskeli.nightrunner.networking;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.ModAttachments;

public class ClientPayloadHandler {

    public static void handleDataOnMain(final ManaSyncPacket data, final IPayloadContext context) {
        // Get the player entity from the context
        Player player = context.player();
        // Make sure this is client
        if(!player.level().isClientSide()) return;

        // Get the mana data from the player
        Mana mana = player.getData(ModAttachments.PLAYER_MANA);
        // Update the mana data
        mana.setReplicatedData(data);

        // Update the player data
        player.setData(ModAttachments.PLAYER_MANA, mana);
    }
}
