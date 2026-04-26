package net.veskeli.nightrunner.entity.modifiers;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.variants.ghast.MultiShotGhast;
import org.joml.Vector3f;

public class MobSpawnEvents {

    /** The scoreboard tag added to every mob that has been processed by this system. */
    public static final String NIGHTRUNNER_TAG = "Nightrunner";
    public static final String NIGHTRUNNER_PRESET_PREFIX = "nightrunner_preset:";
    public static final String NIGHTRUNNER_SKIP_MODIFIERS_TAG = "nightrunner_skip_modifiers";
    private static final String BROODMOTHER_PRESET_ID = "broodmother";
    private static final String SPLITTER_PRESET_ID = "splitter";
    private static final String TOXIC_PRESET_ID = "toxic";
    private static final String SPLITTER_ARMED_DATA_KEY = "nightrunner_splitter_armed";
    private static final String SPLITTER_SPLIT_AT_DATA_KEY = "nightrunner_splitter_split_at";
    private static final float GHAST_VARIANT_CHANCE = 0.30F;
    private static final int BROODMOTHER_CHILD_COUNT = 2;
    private static final double BROODMOTHER_CHILD_SCALE = 0.7D;
    private static final double BROODMOTHER_CHILD_SPAWN_RADIUS = 0.7D;
    private static final double SPLITTER_TRIGGER_DISTANCE = 4.0D;
    private static final double SPLITTER_TRIGGER_DISTANCE_SQUARED = SPLITTER_TRIGGER_DISTANCE * SPLITTER_TRIGGER_DISTANCE;
    private static final int SPLITTER_MIN_DELAY_TICKS = 20;
    private static final int SPLITTER_MAX_DELAY_TICKS = 34;
    private static final int SPLITTER_CHILD_COUNT = 2;
    private static final int SPLITTER_CHILD_FUSE = 30;
    private static final int SPLITTER_CHILD_EXPLOSION_RADIUS = 3;
    private static final double SPLITTER_CHILD_SCALE = 0.5D;
    private static final double SPLITTER_CHILD_SPEED = 0.3D;
    private static final double SPLITTER_CHILD_SPAWN_RADIUS = 0.9D;
    private static final DustParticleOptions SPLITTER_WARNING_PARTICLE = new DustParticleOptions(new Vector3f(0.2F, 0.95F, 0.2F), 1.0F);
    private static final DustParticleOptions TOXIC_AURA_PARTICLE = new DustParticleOptions(new Vector3f(0.15F, 0.85F, 0.15F), 0.9F);
    private static final float TOXIC_CLOUD_RADIUS = 5.0F;
    private static final int TOXIC_CLOUD_DURATION_TICKS = 180;
    private static final int TOXIC_CLOUD_WAIT_TICKS = 10;
    private static final int TOXIC_POISON_DURATION_TICKS = 220;

    @SubscribeEvent
    public void onGhastFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Ghast ghast)) return;
        if (ghast.getType() != EntityType.GHAST) return;
        if (!(ghast.level() instanceof ServerLevel serverLevel)) return;
        if (ghast.getRandom().nextFloat() >= GHAST_VARIANT_CHANCE) return;

        MultiShotGhast replacement = ModEntities.MULTI_GHAST.get().create(serverLevel);
        if (replacement == null) return;

        replacement.moveTo(ghast.getX(), ghast.getY(), ghast.getZ(), ghast.getYRot(), ghast.getXRot());
        replacement.setDeltaMovement(ghast.getDeltaMovement());
        ghast.discard();
        serverLevel.addFreshEntity(replacement);
    }

    @SubscribeEvent
    public void onMobFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster mob)) return;

        // Skip if already processed (safety guard against duplicate processing)
        if (mob.getTags().contains(NIGHTRUNNER_TAG)) return;
        if (mob.getTags().contains(NIGHTRUNNER_SKIP_MODIFIERS_TAG)) return;

        MobPresetTable table = MobModifierRegistry.getPresetTable(mob);
        if (table == null) return;

        // Always-on modifiers apply to every mob of this type.
        table.applyAlways(mob);

        // Then pick and apply one weighted preset for this individual spawn.
        table.pickPreset(mob.getRandom()).ifPresent(preset -> {
            for (IMobModifier modifier : preset.modifiers()) {
                modifier.apply(mob);
            }
            mob.addTag(NIGHTRUNNER_PRESET_PREFIX + preset.id());
            // For testing add preset name as mob name
            // mob.setCustomNameVisible(true);
            mob.setCustomName(Component.nullToEmpty(preset.id()));
        });

        mob.addTag(NIGHTRUNNER_TAG);
    }

    @SubscribeEvent
    public void onMobDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Spider spider)) return;
        if (spider.getType() != EntityType.SPIDER) return;
        if (!(spider.level() instanceof ServerLevel serverLevel)) return;
        if (!hasPreset(spider, BROODMOTHER_PRESET_ID)) return;

        spawnBroodmotherChildren(serverLevel, spider);
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(event.getExplosion().getDirectSourceEntity() instanceof Creeper creeper)) return;
        if (creeper.getType() != EntityType.CREEPER) return;
        if (!hasPreset(creeper, TOXIC_PRESET_ID)) return;

        spawnToxicLingeringCloud(serverLevel, creeper);
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;
        if (creeper.getType() != EntityType.CREEPER) return;
        if (!(creeper.level() instanceof ServerLevel serverLevel)) return;
        if (!creeper.isAlive() || creeper.isRemoved()) return;

        if (hasPreset(creeper, TOXIC_PRESET_ID)) {
            emitToxicAuraParticles(serverLevel, creeper);
        }

        if (!hasPreset(creeper, SPLITTER_PRESET_ID)) return;

        handleSplitterCreeperTick(serverLevel, creeper);
    }

    private static boolean hasPreset(Monster mob, String presetId) {
        return mob.getTags().contains(NIGHTRUNNER_PRESET_PREFIX + presetId);
    }

    private static void handleSplitterCreeperTick(ServerLevel serverLevel, Creeper creeper) {
        CompoundTag persistentData = creeper.getPersistentData();

        if (!persistentData.getBoolean(SPLITTER_ARMED_DATA_KEY)) {
            if (!(creeper.getTarget() instanceof Player targetPlayer)) {
                return;
            }

            if (creeper.distanceToSqr(targetPlayer) >= SPLITTER_TRIGGER_DISTANCE_SQUARED) {
                return;
            }

            persistentData.putBoolean(SPLITTER_ARMED_DATA_KEY, true);
            persistentData.putLong(
                    SPLITTER_SPLIT_AT_DATA_KEY,
                    serverLevel.getGameTime() + creeper.getRandom().nextInt(SPLITTER_MIN_DELAY_TICKS, SPLITTER_MAX_DELAY_TICKS + 1)
            );
        }

        emitSplitterParticles(serverLevel, creeper);

        if (serverLevel.getGameTime() < persistentData.getLong(SPLITTER_SPLIT_AT_DATA_KEY)) {
            return;
        }

        spawnSplitterChildren(serverLevel, creeper);
        creeper.discard();
    }

    private static void emitSplitterParticles(ServerLevel serverLevel, Creeper creeper) {
        double particleSpread = creeper.getBbWidth() * 0.4D;
        serverLevel.sendParticles(
                SPLITTER_WARNING_PARTICLE,
                creeper.getX(),
                creeper.getY() + (creeper.getBbHeight() * 0.5D),
                creeper.getZ(),
                10,
                particleSpread,
                creeper.getBbHeight() * 0.3D,
                particleSpread,
                0.01D
        );
    }

    private static void emitToxicAuraParticles(ServerLevel serverLevel, Creeper creeper) {
        double spread = creeper.getBbWidth() * 0.45D;
        serverLevel.sendParticles(
                TOXIC_AURA_PARTICLE,
                creeper.getX(),
                creeper.getY() + (creeper.getBbHeight() * 0.55D),
                creeper.getZ(),
                4,
                spread,
                creeper.getBbHeight() * 0.35D,
                spread,
                0.002D
        );
    }

    private static void spawnToxicLingeringCloud(ServerLevel serverLevel, Creeper creeper) {
        AreaEffectCloud cloud = new AreaEffectCloud(serverLevel, creeper.getX(), creeper.getY(), creeper.getZ());
        cloud.setRadius(TOXIC_CLOUD_RADIUS);
        cloud.setDuration(TOXIC_CLOUD_DURATION_TICKS);
        cloud.setWaitTime(TOXIC_CLOUD_WAIT_TICKS);
        cloud.setRadiusPerTick(-TOXIC_CLOUD_RADIUS / TOXIC_CLOUD_DURATION_TICKS);
        cloud.setParticle(TOXIC_AURA_PARTICLE);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, TOXIC_POISON_DURATION_TICKS, 0));
        serverLevel.addFreshEntity(cloud);
    }

    private static void spawnBroodmotherChildren(ServerLevel serverLevel, Spider parent) {
        for (int index = 0; index < BROODMOTHER_CHILD_COUNT; index++) {
            Spider child = EntityType.SPIDER.create(serverLevel);
            if (child == null) {
                continue;
            }

            double angle = ((Math.PI * 2D) / BROODMOTHER_CHILD_COUNT) * index;
            double x = parent.getX() + Math.cos(angle) * BROODMOTHER_CHILD_SPAWN_RADIUS;
            double z = parent.getZ() + Math.sin(angle) * BROODMOTHER_CHILD_SPAWN_RADIUS;

            child.moveTo(x, parent.getY(), z, parent.getYRot(), parent.getXRot());
            child.setDeltaMovement(parent.getDeltaMovement().scale(0.25D));
            child.addTag(NIGHTRUNNER_SKIP_MODIFIERS_TAG);
            MobModifierRegistry.setScale(BROODMOTHER_CHILD_SCALE).apply(child);

            if (parent.getTarget() != null) {
                child.setTarget(parent.getTarget());
            }

            serverLevel.addFreshEntity(child);
        }
    }

    private static void spawnSplitterChildren(ServerLevel serverLevel, Creeper parent) {
        for (int index = 0; index < SPLITTER_CHILD_COUNT; index++) {
            Creeper child = EntityType.CREEPER.create(serverLevel);
            if (child == null) {
                continue;
            }

            double angle = ((Math.PI * 2D) / SPLITTER_CHILD_COUNT) * index;
            double x = parent.getX() + Math.cos(angle) * SPLITTER_CHILD_SPAWN_RADIUS;
            double z = parent.getZ() + Math.sin(angle) * SPLITTER_CHILD_SPAWN_RADIUS;

            child.moveTo(x, parent.getY(), z, parent.getYRot(), parent.getXRot());
            child.setDeltaMovement(parent.getDeltaMovement().scale(0.25D));
            child.addTag(NIGHTRUNNER_SKIP_MODIFIERS_TAG);

            MobModifierRegistry.setScale(SPLITTER_CHILD_SCALE).apply(child);
            MobModifierRegistry.setSpeed(SPLITTER_CHILD_SPEED).apply(child);
            MobModifierRegistry.setFuse(SPLITTER_CHILD_FUSE).apply(child);
            MobModifierRegistry.setExplosionRadius(SPLITTER_CHILD_EXPLOSION_RADIUS).apply(child);

            if (parent.getTarget() != null) {
                child.setTarget(parent.getTarget());
            }

            serverLevel.addFreshEntity(child);
        }
    }
}