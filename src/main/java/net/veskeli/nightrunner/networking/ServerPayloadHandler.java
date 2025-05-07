package net.veskeli.nightrunner.networking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleDataOnMain(final ManaData data, final IPayloadContext context) {
        // Do not sync the data to the server
    }

    public static void onToggleProne(TogglePronePacket packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) return;

        boolean isProne = packet.proneState();
        Pose newPose = isProne ? Pose.SWIMMING : Pose.STANDING;
        player.setPose(newPose);
        player.refreshDimensions(); // Update hitbox to the new pose
    }
}
