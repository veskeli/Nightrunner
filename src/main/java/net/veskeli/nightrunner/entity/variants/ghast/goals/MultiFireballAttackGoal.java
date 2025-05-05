package net.veskeli.nightrunner.entity.variants.ghast.goals;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MultiFireballAttackGoal extends Goal {
    private final Ghast ghast;
    private int attackTimer;

    public MultiFireballAttackGoal(Ghast ghast) {
        this.ghast = ghast;
    }

    private int chargeTime;

    @Override
    public void tick() {
        LivingEntity livingentity = this.ghast.getTarget();
        if (livingentity != null) {
            double d0 = 64.0;
            if (livingentity.distanceToSqr(this.ghast) < 4096.0 && this.ghast.hasLineOfSight(livingentity)) {
                Level level = this.ghast.level();
                ++this.chargeTime;
                if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                    level.levelEvent((Player)null, 1015, this.ghast.blockPosition(), 0);
                }

                if (this.chargeTime == 20)
                {
                    if (this.ghast.level().random.nextFloat() < 0.3f)
                    {
                        shootBurst();
                    }
                    else
                    {
                        double d1 = 4.0;
                        Vec3 vec3 = this.ghast.getViewVector(1.0F);
                        double d2 = livingentity.getX() - (this.ghast.getX() + vec3.x * 4.0);
                        double d3 = livingentity.getY(0.5) - (0.5 + this.ghast.getY(0.5));
                        double d4 = livingentity.getZ() - (this.ghast.getZ() + vec3.z * 4.0);
                        Vec3 vec31 = new Vec3(d2, d3, d4);

                        LargeFireball largefireball = new LargeFireball(level, this.ghast, vec31.normalize(), this.ghast.getExplosionPower());
                        largefireball.setPos(this.ghast.getX() + vec3.x * 4.0, this.ghast.getY(0.5) + 0.5, largefireball.getZ() + vec3.z * 4.0);
                        level.addFreshEntity(largefireball);
                    }
                    if (!this.ghast.isSilent()) {
                        level.levelEvent((Player)null, 1016, this.ghast.blockPosition(), 0);
                    }
                    this.chargeTime = -40;
                }
            } else if (this.chargeTime > 0) {
                --this.chargeTime;
            }

            this.ghast.setCharging(this.chargeTime > 10);
        }
    }

    private void shootBurst() {
        LivingEntity target = ghast.getTarget();
        if (target == null) return;

        // Fixed spawn offset from the Ghast to avoid collision, but not too far
        double spawnOffsetX = 1.0; // Fireballs spawn 1 block in front of the Ghast
        double spawnOffsetY = 1.5; // Fireballs spawn slightly above the Ghast to avoid collision
        double spawnOffsetZ = 0.0; // No lateral offset in the Z direction

        for (int i = 0; i < 3; i++) {
            // Calculate fixed spawn position
            double startX = ghast.getX() + spawnOffsetX + (i *1.5f);
            double startY = ghast.getY() + spawnOffsetY + i + randomOffset();
            double startZ = ghast.getZ() + spawnOffsetZ + randomOffset();

            // Calculate the direction vector towards the target
            double dx = target.getX() - startX;
            double dy = target.getY(0.5) - startY;
            double dz = target.getZ() - startZ;

            Vec3 direction = new Vec3(dx, dy, dz).normalize(); // Normalize to get the unit vector

            // Create the fireball at the fixed spawn position
            LargeFireball fireball = new LargeFireball(
                    ghast.level(),
                    ghast,
                    direction.scale(0.8),
                    1
            );

            // Set the fireball position to the fixed spawn location
            fireball.setPos(startX, startY, startZ);
            ghast.level().addFreshEntity(fireball); // Add fireball to the level
        }
    }

    private double randomOffset() {
        return (ghast.getRandom().nextDouble() - 0.5) * 5.0; // spread
    }

    @Override
    public boolean canUse() {
        return ghast.getTarget() != null;
    }
}
