package net.veskeli.nightrunner.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class WandProjectile extends AbstractHurtingProjectile {

    private double baseDamage = 2.0;
    private float aoeDistance = 1.0f;
    private Player owner;

    public WandProjectile(EntityType<? extends WandProjectile> type, Level level) {
        super(type, level);
    }

    public void setCustomProperties(Player serverPlayer, float damage, float aoeDistance)
    {
        this.owner = serverPlayer;
        this.baseDamage = damage;
        this.aoeDistance = aoeDistance;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {

        // If owner is not null and the entity is the owner, return
        if (this.owner != null && result.getEntity() == this.getOwner()) {
            return;
        }

        double d0 = this.baseDamage;
        DamageSource damagesource = this.damageSources().magic();

        super.onHitEntity(result);

        // Damage the entity
        result.getEntity().hurt(damagesource, (float) d0);

        // Set the last damage source to the owner
        if (result.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) result.getEntity();
            target.setLastHurtByMob(this.owner);
            target.setLastHurtMob(this.owner);
        }

        this.discard(); // Remove projectile on hit
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {

        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            double radius = aoeDistance;
            double damage = this.baseDamage / 2;
            DamageSource damagesource = this.damageSources().magic();

            AABB aoe = new AABB(
                    this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                    this.getX() + radius, this.getY() + radius, this.getZ() + radius
            );

            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, aoe,
                    LivingEntity::isAlive);

            for (LivingEntity target : targets) {
                target.hurt(damagesource, (float) damage);
            }

            // Optional: explosion particles or sound
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0.01);
        }

        this.discard(); // Remove the projectile
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        //this.life = 0;
    }

/*
    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
 */
}
