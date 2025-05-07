package net.veskeli.nightrunner.networking;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.ModAttachments;

public class ManaSync {
    public static void syncManaToClient(ServerPlayer player) {
        Mana m = player.getData(ModAttachments.PLAYER_MANA);
        ManaSyncPacket pkt = new ManaSyncPacket(m.getMana(), m.getMaxMana(), m.getRegenCooldown());
        PacketDistributor.sendToPlayer(player, pkt);
    }
}
