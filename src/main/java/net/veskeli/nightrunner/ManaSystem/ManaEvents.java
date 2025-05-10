package net.veskeli.nightrunner.ManaSystem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.custom.ManaOrbEntity;
import net.veskeli.nightrunner.item.ModItems;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ManaEvents {

    private static final List<EntityManaOrb> HOSTILE_MOBS_WITH_DROPS = Arrays.asList(
            new EntityManaOrb(EntityType.ZOMBIE, 1, 1),
            new EntityManaOrb(EntityType.SKELETON, 1, 1),
            new EntityManaOrb(EntityType.CREEPER, 1, 1),
            new EntityManaOrb(EntityType.SPIDER, 1, 1),
            new EntityManaOrb(EntityType.CAVE_SPIDER, 1, 2),
            new EntityManaOrb(EntityType.DROWNED, 1, 2),
            new EntityManaOrb(EntityType.HUSK, 1, 2),
            new EntityManaOrb(EntityType.STRAY, 1, 2),
            new EntityManaOrb(EntityType.ZOMBIE_VILLAGER, 1, 2),
            new EntityManaOrb(EntityType.SILVERFISH, 1, 1),
            new EntityManaOrb(EntityType.ENDERMITE, 1, 1),
            new EntityManaOrb(EntityType.PHANTOM, 2, 3),
            new EntityManaOrb(EntityType.SLIME, 1, 2),
            new EntityManaOrb(EntityType.MAGMA_CUBE, 1, 2),
            new EntityManaOrb(EntityType.BLAZE, 2, 4),
            new EntityManaOrb(EntityType.GHAST, 2, 6),
            new EntityManaOrb(EntityType.WITCH, 3, 6),
            new EntityManaOrb(EntityType.ENDERMAN, 3, 5),
            new EntityManaOrb(EntityType.SHULKER, 3, 6),
            new EntityManaOrb(EntityType.GUARDIAN, 2, 3),
            new EntityManaOrb(EntityType.ELDER_GUARDIAN, 5, 8),
            new EntityManaOrb(EntityType.PILLAGER, 1, 2),
            new EntityManaOrb(EntityType.VINDICATOR, 2, 5),
            new EntityManaOrb(EntityType.EVOKER, 3, 5),
            new EntityManaOrb(EntityType.VEX, 2, 3),
            new EntityManaOrb(EntityType.RAVAGER, 4, 8),
            new EntityManaOrb(EntityType.HOGLIN, 2, 4),
            new EntityManaOrb(EntityType.ZOGLIN, 2, 4),
            new EntityManaOrb(EntityType.PIGLIN_BRUTE, 4, 8),
            new EntityManaOrb(EntityType.WITHER_SKELETON, 8, 15),
            new EntityManaOrb(EntityType.WARDEN, 15, 20),
            new EntityManaOrb(EntityType.ENDERMAN, 3, 5),
            new EntityManaOrb(EntityType.WITHER, 15, 20),
            new EntityManaOrb(EntityType.ENDER_DRAGON, 20, 40)
    );


    // Check if an entity matches the list
    public static boolean isHostileMob(EntityType<?> type) {
        return HOSTILE_MOBS_WITH_DROPS.stream()
                .anyMatch(mobDrop -> mobDrop.getMob().equals(type));
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        EntityType<?> entityType = event.getEntity().getType();

        // Find the matching EntityManaOrb for the killed entity
        HOSTILE_MOBS_WITH_DROPS.stream()
                .filter(mobDrop -> mobDrop.getMob().equals(entityType))
                .findFirst()
                .ifPresent(mobDrop -> {
                    // Generate a random amount between min and max (inclusive)
                    int amount = mobDrop.getMinAmount() + new Random().nextInt(mobDrop.getMaxAmount() - mobDrop.getMinAmount() + 1);

                    ManaOrbEntity manaOrb = new ManaOrbEntity(ModEntities.MANA_ORB.get(), event.getEntity().level(), amount);
                    manaOrb.setPos(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ());
                    event.getEntity().level().addFreshEntity(manaOrb);
                });
    }
}
