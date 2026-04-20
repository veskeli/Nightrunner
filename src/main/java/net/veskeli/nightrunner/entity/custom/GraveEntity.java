package net.veskeli.nightrunner.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class GraveEntity extends ArmorStand {
    private static final EntityDataAccessor<java.util.Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(
            GraveEntity.class,
            EntityDataSerializers.OPTIONAL_UUID
    );

    public GraveEntity(EntityType<? extends ArmorStand> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, java.util.Optional.empty());
    }

    // Prevent damage from all sources
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    // Explosions
    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean isMarker() {
        return false;
    }

    // Optional: prevent removal by void or similar
    @Override
    public void tick() {
        if (this.getY() < -64) {
            this.teleportTo(this.getX(), 64, this.getZ());
        }
        super.tick();
    }

    public void setOwner(UUID uuid) {
        this.entityData.set(OWNER_UUID, java.util.Optional.ofNullable(uuid));
    }

    public UUID getOwner() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID owner = getOwner();
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            setOwner(tag.getUUID("Owner"));
        }

        // Keep the grave interactable and stationary after reload.
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }
}
