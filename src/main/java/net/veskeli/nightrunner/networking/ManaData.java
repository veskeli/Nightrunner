package net.veskeli.nightrunner.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ManaData(int currentMana, int maxMana) implements CustomPacketPayload {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }

    public ManaData(int currentMana, int maxMana) {
        this.currentMana = currentMana;
        this.maxMana = maxMana;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(currentMana);
        buf.writeInt(maxMana);
    }

    public static ManaData decode(RegistryFriendlyByteBuf buf) {
        return new ManaData(buf.readInt(), buf.readInt());
    }
}
