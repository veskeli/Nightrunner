package net.veskeli.nightrunner.ManaSystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class Mana implements IMana, INBTSerializable<CompoundTag> {

    private int mana = 100;
    private final int maxMana = 100;

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = Math.min(mana, maxMana);
    }

    @Override
    public void addMana(int amount) {
        setMana(this.mana + amount);
    }

    @Override
    public void subtractMana(int amount) {
        setMana(this.mana - amount);
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mana", mana);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        mana = compoundTag.getInt("Mana");
    }
}
