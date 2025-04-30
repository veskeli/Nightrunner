package net.veskeli.nightrunner.entity.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class GraveEntity extends ArmorStand {
    public GraveEntity(EntityType<? extends ArmorStand> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.setNoGravity(true);
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

    // Optional: prevent removal by void or similar
    @Override
    public void tick() {
        if (this.getY() < -64) {
            this.teleportTo(this.getX(), 64, this.getZ());
        }
        super.tick();
    }

    private UUID ownerUUID;

    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getOwner() {
        return ownerUUID;
    }
}
