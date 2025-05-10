package net.veskeli.nightrunner.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.ModAttachments;

import java.util.List;

public class ManaOrbEntity extends Entity {

    private int manaAmount;
    private int age;
    private Player followingPlayer;

    public ManaOrbEntity(EntityType<? extends Entity> entityType, Level level, int manaAmount) {
        super(entityType, level);
        this.manaAmount = manaAmount;
    }

    public ManaOrbEntity(EntityType<ManaOrbEntity> manaOrbEntityEntityType, Level level) {
        super(manaOrbEntityEntityType, level);
        this.manaAmount = 1;
    }

    public void setManaAmount(int manaAmount) {
        this.manaAmount = Math.max(0, Math.min(manaAmount, 100)); // Clamp between 0 and 100
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else {
            this.applyGravity();
        }

        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement(
                    (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F),
                    0.2F,
                    (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F)
            );
        }

        if (!this.level().noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        }

        if (this.tickCount % 20 == 1) {
            this.scanForEntities();
        }

        if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
            this.followingPlayer = null;
        }

        if (this.followingPlayer != null) {
            Vec3 vec3 = new Vec3(
                    this.followingPlayer.getX() - this.getX(),
                    this.followingPlayer.getY() + (double)this.followingPlayer.getEyeHeight() / 2.0 - this.getY(),
                    this.followingPlayer.getZ() - this.getZ()
            );
            double d0 = vec3.lengthSqr();
            if (d0 < 64.0) {
                double d1 = 1.0 - Math.sqrt(d0) / 8.0;
                this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.1)));
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        float f = 0.98F;
        if (this.onGround()) {
            BlockPos pos = getBlockPosBelowThatAffectsMyMovement();
            f = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.98, (double)f));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.9, 1.0));
        }

        this.age++;
        if (this.age >= 6000) {
            this.discard();
        }
    }

    private void scanForEntities() {
        double range = 8.0;
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(range));

        Player closest = null;
        double closestDistSqr = Double.MAX_VALUE;

        for (Player player : players) {
            if (player.isSpectator() || player.isDeadOrDying()) continue;
            double distSqr = player.distanceToSqr(this);
            if (distSqr < closestDistSqr) {
                closestDistSqr = distSqr;
                closest = player;
            }
        }

        this.followingPlayer = closest;
    }

    private void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.99F, Math.min(vec3.y + 5.0E-4F, 0.06F), vec3.z * 0.99F);
    }

    @Override
    public void playerTouch(Player player) {
        // Return if client side
        if (player.level().isClientSide()) {
            return;
        }
        Mana mana = player.getData(ModAttachments.PLAYER_MANA);

        // Check if the player's spell slots are full
        if (mana.areSpellSlotsFull()) {
            double range = 7.0; // Define the range for finding the next closest player
            List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(range));
            Player closestPlayer = null;
            double closestDistSqr = Double.MAX_VALUE;

            for (Player nearbyPlayer : nearbyPlayers) {
                if (nearbyPlayer == player || nearbyPlayer.isSpectator() || nearbyPlayer.isDeadOrDying()) continue;
                double distSqr = nearbyPlayer.distanceToSqr(this);
                if (distSqr < closestDistSqr) {
                    closestDistSqr = distSqr;
                    closestPlayer = nearbyPlayer;
                }
            }

            if (closestPlayer != null) {
                Mana closestPlayerMana = closestPlayer.getData(ModAttachments.PLAYER_MANA);

                int transferAmount = Math.max(1, manaAmount / 2);
                closestPlayerMana.regenSpellSlots(transferAmount);

                closestPlayer.setData(ModAttachments.PLAYER_MANA, closestPlayerMana);
                Mana.replicateData(closestPlayerMana, (ServerPlayer) closestPlayer);

                // Play sound effect
                closestPlayer.level().playSound(null, closestPlayer.getX(), closestPlayer.getY(), closestPlayer.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, .15F);
            }
        } else {
            mana.regenSpellSlots(manaAmount);
            player.setData(ModAttachments.PLAYER_MANA, mana);
            Mana.replicateData(mana, (ServerPlayer) player);

            // Play sound effect
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, .15F);
        }

        // Remove the entity from the world
        this.remove(RemovalReason.DISCARDED);
    }

    public int getIcon() {
        if (this.manaAmount >= 30) {
            return 10;
        } else if (this.manaAmount >= 20) {
            return 9;
        } else if (this.manaAmount >= 15) {
            return 8;
        } else if (this.manaAmount >= 10) {
            return 7;
        } else if (this.manaAmount >= 7) {
            return 6;
        } else if (this.manaAmount >= 5) {
            return 5;
        } else if (this.manaAmount >= 4) {
            return 4;
        } else if (this.manaAmount >= 3) {
            return 3;
        } else if (this.manaAmount >= 2) {
            return 2;
        } else {
            return this.manaAmount >= 3 ? 1 : 0;
        }
    }
}
