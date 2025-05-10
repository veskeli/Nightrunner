package net.veskeli.nightrunner.ManaSystem;

import net.minecraft.world.entity.EntityType;

public class EntityManaOrb {
    private final EntityType<?> mob;
    private final int minAmount;
    private final int maxAmount;

    public EntityManaOrb(EntityType<?> mob, int minAmount, int maxAmount) {
        this.mob = mob;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public EntityType<?> getMob() {
        return mob;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }
}
