package net.veskeli.nightrunner.item.properties;

import net.minecraft.world.item.Item;

public class WandItemProperties extends Item.Properties {
    private float power;
    private float aoeRadius;

    public WandItemProperties power(float power) {
        this.power = power;
        return this;
    }

    public WandItemProperties aoeRadius(float aoeRadius) {
        this.aoeRadius = aoeRadius;
        return this;
    }

    public float getPower() {
        return power;
    }

    public float getAoeRadius() {
        return aoeRadius;
    }
}
