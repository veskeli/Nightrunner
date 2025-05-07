package net.veskeli.nightrunner.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.Nightrunner;

public record TogglePronePacket(boolean proneState) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TogglePronePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "prone_toggle"));

    public static final StreamCodec<ByteBuf, TogglePronePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            TogglePronePacket::proneState,
            TogglePronePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
