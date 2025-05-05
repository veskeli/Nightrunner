package net.veskeli.nightrunner.entity.variants.ghast;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.veskeli.nightrunner.entity.variants.ghast.goals.MultiFireballAttackGoal;

public class MultiShotGhast extends Ghast {

    public MultiShotGhast(EntityType<? extends Ghast> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Replace the default attack goal with a custom one
        this.goalSelector.addGoal(10, new MultiFireballAttackGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && this.target != null && this.target.isAlive();
            }

            @Override
            protected AABB getTargetSearchArea(double targetDistance) {
                // Wider detection horizontally and vertically
                return this.mob.getBoundingBox().inflate(32.0D, 32.0D, 32.0D);
            }
        });
    }
}
