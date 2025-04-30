package net.veskeli.nightrunner.entity.custom;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class GraveEntity extends ArmorStand {
    public GraveEntity(EntityType<? extends ArmorStand> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean isMarker() {
        return true;
    }

    @Override
    public void handleDamageEvent(DamageSource damageSource) {
        //super.handleDamageEvent(damageSource);
    }
}
