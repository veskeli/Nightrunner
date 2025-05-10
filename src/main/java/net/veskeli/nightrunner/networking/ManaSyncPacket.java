package net.veskeli.nightrunner.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.Nightrunner;

public record ManaSyncPacket(int currentMana, int maxMana, int currentRecharge, int spellSlots, int maxSpellSlots) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ManaSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "mana_sync"));

    public static final StreamCodec<ByteBuf, ManaSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ManaSyncPacket::currentMana,
            ByteBufCodecs.VAR_INT,
            ManaSyncPacket::maxMana,
            ByteBufCodecs.VAR_INT,
            ManaSyncPacket::spellSlots,
            ByteBufCodecs.VAR_INT,
            ManaSyncPacket::maxSpellSlots,
            ByteBufCodecs.VAR_INT,
            ManaSyncPacket::currentRecharge,
            ManaSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
