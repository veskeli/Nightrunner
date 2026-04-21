package net.veskeli.nightrunner.entity.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Spider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.variants.ghast.MultiShotGhast;

public class MobSpawnEvents {

    /** The scoreboard tag added to every mob that has been processed by this system. */
    public static final String NIGHTRUNNER_TAG = "Nightrunner";
    public static final String NIGHTRUNNER_PRESET_PREFIX = "nightrunner_preset:";
    public static final String NIGHTRUNNER_SKIP_MODIFIERS_TAG = "nightrunner_skip_modifiers";
    private static final String BROODMOTHER_PRESET_ID = "broodmother";
    private static final float GHAST_VARIANT_CHANCE = 0.30F;
    private static final int BROODMOTHER_CHILD_COUNT = 2;
    private static final double BROODMOTHER_CHILD_SCALE = 0.7D;
    private static final double BROODMOTHER_CHILD_SPAWN_RADIUS = 0.7D;

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

    private static boolean hasPreset(Monster mob, String presetId) {
        return mob.getTags().contains(NIGHTRUNNER_PRESET_PREFIX + presetId);
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
}