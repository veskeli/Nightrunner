package net.veskeli.nightrunner.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.veskeli.nightrunner.entity.ModEntities;

import java.util.List;

public class IceKnifeProjectile extends AbstractHurtingProjectile {
    private float aoeDistance = 1.0f;
    private Player owner;

    public IceKnifeProjectile(Level level) {
        super(ModEntities.ICE_KNIFE_PROJECTILE.get(), level);
    }

    public IceKnifeProjectile(EntityType<IceKnifeProjectile> iceKnifeProjectileEntityType, Level level) {
        super(iceKnifeProjectileEntityType, level);
    }

    public void setCustomProperties(Player serverPlayer, float aoeDistance)
    {
        this.owner = serverPlayer;
        this.aoeDistance = aoeDistance;
    }

    protected void onHit()
    {
        int radius = (int) this.aoeDistance;

        AABB aoe = new AABB(
                this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                this.getX() + radius, this.getY() + radius, this.getZ() + radius
        );

        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, aoe,
                LivingEntity::isAlive);

        for (LivingEntity target : targets) {
            ApplyEffects(target);
        }

        // Particles to indicate the area of effect
        if (!this.level().isClientSide()) {
            float particleDistance = this.aoeDistance / 2;
            int particleCount = (int) (this.aoeDistance * 100);
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.DUST_PLUME, this.getX(), this.getY(), this.getZ(), particleCount, particleDistance, particleDistance, particleDistance, 0.01);
        }
    }

    protected void ApplyEffects(LivingEntity livingEntity)
    {

        // TODO: Make custom effect for frozen entities
        // Apply wither and slow effect
        livingEntity.forceAddEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0), livingEntity); // Wither effect for 5 seconds
        livingEntity.forceAddEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2), livingEntity); // Slowness effect for 5 seconds

        livingEntity.setLastHurtByMob(this.owner);
        livingEntity.setLastHurtMob(this.owner);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {

        // If owner is not null and the entity is the owner, return
        if (this.owner != null && result.getEntity() == this.getOwner()) {
            return;
        }

        super.onHitEntity(result);

        // On hit
        onHit();

        this.discard(); // Remove projectile on hit
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {

        super.onHitBlock(result);

        if (!this.level().isClientSide()) {

            onHit();

            // Optional: explosion particles or sound
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0.01);
        }

        this.discard(); // Remove the projectile
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 10; i++) { // Generate 10 particles
                level().addParticle(ParticleTypes.BUBBLE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                        0, 0, 0);
            }
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        //this.life = 0;
    }
}
