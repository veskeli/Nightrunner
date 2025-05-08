package net.veskeli.nightrunner.networking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.veskeli.nightrunner.ModAttachments;
import net.veskeli.nightrunner.SpellSystem.Attachment.SpellAttachment;
import net.veskeli.nightrunner.SpellSystem.ModSpells;

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

    public static void onSpellSelected(SpellSelectPacket packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player == null) return;

        // Get the spell attachment from the player
        SpellAttachment spellAttachment = player.getData(ModAttachments.PLAYER_SPELLS);

        // Set the selected spell based on the index received from the packet
        int index = packet.spellIndex();
        if(index == -1)
        {
            spellAttachment.setSelectedSpell(null);
        }
        else
        {
            spellAttachment.setSelectedSpell(ModSpells.SPELL_REGISTRY.byId(index));
        }

        // Update the player's data with the new spell attachment
        player.setData(ModAttachments.PLAYER_SPELLS, spellAttachment);
    }
}
