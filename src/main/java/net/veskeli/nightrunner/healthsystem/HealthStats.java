package net.veskeli.nightrunner.healthsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class HealthStats implements IHealthStats, INBTSerializable<CompoundTag> {

    private int maxHealth = 16; // Default max health
    private float currentReviveHealth = 0.0f;
    private float reviveItemMaxHealth = 0.0f;
    private float reviveHealthDegradeStep = 0.0f;
    private boolean pendingSelfRevive = false;
    private String pendingSelfReviveSourceId = "";

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
    public float getCurrentReviveHealth() {
        return currentReviveHealth;
    }

    @Override
    public void setCurrentReviveHealth(float health) {
        currentReviveHealth = health;
    }

    @Override
    public float getReviveItemMaxHealth() {
        return reviveItemMaxHealth;
    }

    @Override
    public void setReviveItemMaxHealth(float health) {
        reviveItemMaxHealth = health;
    }

    @Override
    public float getReviveHealthDegradeStep() {
        return reviveHealthDegradeStep;
    }

    @Override
    public void setReviveHealthDegradeStep(float step) {
        reviveHealthDegradeStep = step;
    }

    @Override
    public boolean hasPendingSelfRevive() {
        return pendingSelfRevive;
    }

    @Override
    public void setPendingSelfRevive(boolean pending) {
        pendingSelfRevive = pending;
    }

    @Override
    public String getPendingSelfReviveSourceId() {
        return pendingSelfReviveSourceId;
    }

    @Override
    public void setPendingSelfReviveSourceId(String sourceId) {
        pendingSelfReviveSourceId = sourceId == null ? "" : sourceId;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("MaxHealthStat", maxHealth);
        tag.putFloat("CurrentReviveHealth", currentReviveHealth);
        tag.putFloat("ReviveItemMaxHealth", reviveItemMaxHealth);
        tag.putFloat("ReviveHealthDegradeStep", reviveHealthDegradeStep);
        tag.putBoolean("PendingSelfRevive", pendingSelfRevive);
        tag.putString("PendingSelfReviveSourceId", pendingSelfReviveSourceId);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        maxHealth = compoundTag.getInt("MaxHealthStat");
        currentReviveHealth = compoundTag.contains("CurrentReviveHealth") ? compoundTag.getFloat("CurrentReviveHealth") : 0.0f;
        reviveItemMaxHealth = compoundTag.contains("ReviveItemMaxHealth") ? compoundTag.getFloat("ReviveItemMaxHealth") : 0.0f;
        reviveHealthDegradeStep = compoundTag.contains("ReviveHealthDegradeStep") ? compoundTag.getFloat("ReviveHealthDegradeStep") : 0.0f;
        pendingSelfRevive = compoundTag.contains("PendingSelfRevive") && compoundTag.getBoolean("PendingSelfRevive");
        pendingSelfReviveSourceId = compoundTag.contains("PendingSelfReviveSourceId") ? compoundTag.getString("PendingSelfReviveSourceId") : "";
    }
}
