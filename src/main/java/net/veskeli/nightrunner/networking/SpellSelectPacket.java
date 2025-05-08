package net.veskeli.nightrunner.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.Nightrunner;

public record SpellSelectPacket(int spellIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpellSelectPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "spell_select"));

    public static final StreamCodec<ByteBuf, SpellSelectPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SpellSelectPacket::spellIndex,
            SpellSelectPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
