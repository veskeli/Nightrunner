package net.veskeli.nightrunner.healthsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class HealthStats implements IHealthStats, INBTSerializable<CompoundTag> {

    private int maxHealth = 16; // Default max health

    @Override
    public int getHealth() {
        return maxHealth;
    }

    @Override
    public void setMaxHealth(int health) {
        maxHealth = health;
    }

    @Override
    public void addMaxHealth(int amount) {
        maxHealth += amount;
    }

    @Override
    public void subtractMaxHealth(int amount) {
        maxHealth -= amount;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("MaxHealthStat", maxHealth);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        maxHealth = compoundTag.getInt("MaxHealthStat");
    }
}
