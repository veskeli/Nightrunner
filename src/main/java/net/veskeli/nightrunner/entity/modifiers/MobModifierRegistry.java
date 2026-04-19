package net.veskeli.nightrunner.entity.modifiers;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central place to register per-mob permanent modifiers and weighted presets.
 */
public class MobModifierRegistry {

    private static final Map<EntityType<?>, MobPresetTable> REGISTRY = new HashMap<>();
    private static final Map<Class<? extends Mob>, MobPresetTable> FAMILY_REGISTRY = new LinkedHashMap<>();

    /** Call once during mod init (e.g. from commonSetup). */
    public static void init() {
        REGISTRY.clear();
        FAMILY_REGISTRY.clear();

        /* Zombie base stats:
          Speed 0.31
          Damage 10
         */
        MobPresetTable zombiePresets = MobPresetTable.builder()
                // Permanent modifiers applied to every zombie
                .always(setSpawnReinforcements(0.125))
                // normal zombie
                .preset("normal", 30,
                        setDamage(10),
                        setSpeed(0.31)
                )
                // tanky but weaker
                .preset("tanky", 30,
                        multiplyMaxHealth(1.3),
                        setDamage(8),
                        setSpeed(0.29)
                )
                // glass-cannon style
                .preset("berserker", 30,
                        multiplyMaxHealth(0.70),
                        setDamage(10),
                        setSpeed(0.33),
                        setScale(0.85)
                )
                .build();
        register(EntityType.ZOMBIE, zombiePresets);
        registerFamily(Zombie.class, zombiePresets);

        // Skeleton
        MobPresetTable skeletonPresets = MobPresetTable.builder()
                .always(setSpeed(0.30))
                .build();
        register(EntityType.SKELETON, skeletonPresets);
        registerFamily(AbstractSkeleton.class, skeletonPresets);

        // Wither Skeleton
        register(EntityType.WITHER_SKELETON, MobPresetTable.builder()
                .preset("brute", 60,
                        setSpeed(0.27),
                        setDamage(7),
                        setMaxHealth(24)
                )
                .preset("juggernaut", 40,
                        setSpeed(0.24),
                        setDamage(9),
                        setMaxHealth(30),
                        setScale(1.3)
                )
                .build());

        // Illagers (pillager, vindicator, evoker, illusioner if enabled)
        MobPresetTable illagerPresets = MobPresetTable.builder()
                // Small baseline buff so all illagers feel a bit more threatening.
                .always(multiplySpeed(1.08))
                .preset("standard", 60)
                .preset("agile", 25,
                        multiplySpeed(1.10),
                        setScale(0.95)
                )
                .preset("bruiser", 15,
                        multiplyMaxHealth(1.20),
                        setDamage(7),
                        setScale(1.08)
                )
                .build();
        registerFamily(AbstractIllager.class, illagerPresets);

        // Creeper
        register(EntityType.CREEPER, MobPresetTable.builder()
                .preset("Nuke", 5,
                        setExplosionRadius(15),
                        setFuse(45),
                        setSpeed(0.20),
                        setMaxHealth(30),
                        setScale(1.25)
                )
                .preset("Lite", 10,
                        setExplosionRadius(2),
                        setFuse(15),
                        setSpeed(0.30),
                        setMaxHealth(10),
                        setScale(0.75)
                )
                .preset("Default", 85,
                        setExplosionRadius(5),
                        setFuse(30),
                        setSpeed(0.25),
                        setMaxHealth(20),
                        setScale(1.0)
                )
                .build());

        // Spiders are a bit faster than other mobs, so we give them a permanent speed boost but no presets for now.
        register(EntityType.SPIDER, MobPresetTable.builder()
                .always(setSpeed(0.35), setDamage(6))
                .build());
    }

    /** Sets the mob's base ATTACK_DAMAGE attribute. */
    public static IMobModifier setDamage(double damageAmount) {
        return mob -> {
            var attr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attr != null) {
                attr.setBaseValue(damageAmount);
            }
        };
    }

    /** Multiplies the mob's base MAX_HEALTH attribute and heals it to full. */
    public static IMobModifier multiplyMaxHealth(double multiplier) {
        return mob -> {
            var attr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(attr.getBaseValue() * multiplier);
                mob.setHealth(mob.getMaxHealth());
            }
        };
    }

    /** Sets the mob's base MAX_HEALTH attribute and heals it to full. */
    public static IMobModifier setMaxHealth(double healthAmount) {
        return mob -> {
            var attr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(healthAmount);
                mob.setHealth(mob.getMaxHealth());
            }
        };
    }

    /** Multiplies the mob's MOVEMENT_SPEED attribute. */
    public static IMobModifier multiplySpeed(double multiplier) {
        return mob -> {
            var attr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.setBaseValue(attr.getBaseValue() * multiplier);
            }
        };
    }

    /** sets the mob's MOVEMENT_SPEED attribute. */
    public static IMobModifier setSpeed(double speed) {
        return mob -> {
            var attr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.setBaseValue(speed);
            }
        };
    }

    /** Sets the mob's SPAWN_REINFORCEMENTS_CHANCE attribute. */
    public static IMobModifier setSpawnReinforcements(double amount) {
        return mob -> {
            var attr = mob.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
            if (attr != null) {
                attr.setBaseValue(amount);
            }
        };
    }

    /** Sets creeper explosion radius. */
    public static IMobModifier setExplosionRadius(int radius) {
        return mob -> {
            if (mob instanceof Creeper creeper) {
                setCreeperIntField(creeper, "explosionRadius", radius);
            }
        };
    }

    /** Sets creeper fuse duration (ticks). */
    public static IMobModifier setFuse(int fuseTicks) {
        return mob -> {
            if (mob instanceof Creeper creeper) {
                setCreeperIntField(creeper, "maxSwell", fuseTicks);
            }
        };
    }

    // Uses reflection because direct setters are not exposed in this mapping set.
    private static void setCreeperIntField(Creeper creeper, String fieldName, int value) {
        try {
            var field = Creeper.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(creeper, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    /** Sets the mob's base SCALE attribute. */
    public static IMobModifier setScale(double scale) {
        return mob -> {
            var attr = mob.getAttribute(Attributes.SCALE);
            if (attr != null) {
                attr.setBaseValue(scale);
            }
        };
    }

    private static void register(EntityType<?> type, MobPresetTable table) {
        REGISTRY.put(type, table);
    }

    private static void registerFamily(Class<? extends Mob> mobClass, MobPresetTable table) {
        FAMILY_REGISTRY.put(mobClass, table);
    }

    public static MobPresetTable getPresetTable(EntityType<?> type) {
        return REGISTRY.get(type);
    }

    public static MobPresetTable getPresetTable(Mob mob) {
        MobPresetTable exact = REGISTRY.get(mob.getType());
        if (exact != null) {
            return exact;
        }

        // Keep Wither Skeleton out of the shared skeleton-family presets.
        if (mob instanceof WitherSkeleton) {
            return null;
        }

        for (Map.Entry<Class<? extends Mob>, MobPresetTable> entry : FAMILY_REGISTRY.entrySet()) {
            if (entry.getKey().isInstance(mob)) {
                return entry.getValue();
            }
        }

        return null;
    }
}