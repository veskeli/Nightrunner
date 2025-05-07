package net.veskeli.nightrunner.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ManaData(int currentMana, int maxMana, int currentRecharge) implements CustomPacketPayload {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }

    public ManaData(int currentMana, int maxMana, int currentRecharge) {
        this.currentMana = currentMana;
        this.maxMana = maxMana;
        this.currentRecharge = currentRecharge;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(currentMana);
        buf.writeInt(maxMana);
        buf.writeInt(currentRecharge);
    }

    public static ManaData decode(RegistryFriendlyByteBuf buf) {
        return new ManaData(buf.readInt(), buf.readInt(), buf.readInt());
    }
}
