package net.veskeli.nightrunner.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.entity.projectile.WandProjectile;

import java.util.function.Supplier;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Nightrunner.MODID);

    public static final Supplier<EntityType<GraveEntity>> GRAVE =
            ENTITIES.register("grave", () -> EntityType.Builder.of(GraveEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.5f)
                    .build(Nightrunner.MODID + ":grave"));

    public static final Supplier<EntityType<WandProjectile>> WAND_PROJECTILE = ENTITIES.register("wand_projectile",
            () -> EntityType.Builder.<WandProjectile>of(WandProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f) // size like a snowball
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("wand_projectile"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
