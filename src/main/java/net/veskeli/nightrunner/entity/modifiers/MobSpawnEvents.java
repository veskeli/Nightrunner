package net.veskeli.nightrunner.entity.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ghast;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.variants.ghast.MultiShotGhast;

public class MobSpawnEvents {

    /** The scoreboard tag added to every mob that has been processed by this system. */
    public static final String NIGHTRUNNER_TAG = "Nightrunner";
    public static final String NIGHTRUNNER_PRESET_PREFIX = "nightrunner_preset:";
    private static final float GHAST_VARIANT_CHANCE = 0.30F;

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
            mob.setCustomNameVisible(true);
            mob.setCustomName(Component.nullToEmpty(preset.id()));
        });

        mob.addTag(NIGHTRUNNER_TAG);
    }
}