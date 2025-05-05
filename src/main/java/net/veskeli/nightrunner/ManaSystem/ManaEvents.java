package net.veskeli.nightrunner.ManaSystem;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.veskeli.nightrunner.item.ModItems;

public class ManaEvents {

    @SubscribeEvent
    public void onMobDeath(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            // Create and add the Mana Orb item to drops
            ItemStack manaOrb = new ItemStack(ModItems.ManaOrb.get());
            ItemEntity entityItem = new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    manaOrb
            );
            event.getDrops().add(entityItem);
        }
    }
}
