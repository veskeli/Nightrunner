package net.veskeli.nightrunner.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.entity.projectile.IceKnifeProjectile;
import net.veskeli.nightrunner.entity.projectile.WandProjectile;
import net.veskeli.nightrunner.entity.variants.ghast.MultiShotGhast;

import java.util.function.Supplier;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Nightrunner.MODID);

    // Grave
    public static final Supplier<EntityType<GraveEntity>> GRAVE =
            ENTITIES.register("grave", () -> EntityType.Builder.of(GraveEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.5f)
                    .build(Nightrunner.MODID + ":grave"));

    // Wand Projectile
    public static final Supplier<EntityType<WandProjectile>> WAND_PROJECTILE = ENTITIES.register("wand_projectile",
            () -> EntityType.Builder.<WandProjectile>of(WandProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f) // size like a snowball
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("wand_projectile"));

    // Ice Knife Projectile
    public static final Supplier<EntityType<IceKnifeProjectile>> ICE_KNIFE_PROJECTILE = ENTITIES.register("ice_knife_projectile",
            () -> EntityType.Builder.<IceKnifeProjectile>of(IceKnifeProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f) // size like a snowball
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("ice_knife_projectile"));

    // Multi shot ghast
    public static final Supplier<EntityType<MultiShotGhast>> MULTI_GHAST = ENTITIES.register("multi_ghast",
            () -> EntityType.Builder.of(MultiShotGhast::new, MobCategory.MONSTER)
                    .sized(4.0F, 4.0F) // same as vanilla ghast
                    .build(Nightrunner.MODID + ":multi_ghast"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
