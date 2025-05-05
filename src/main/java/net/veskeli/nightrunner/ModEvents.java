package net.veskeli.nightrunner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.healthsystem.GraveDataStore;
import net.veskeli.nightrunner.healthsystem.HealthStats;
import net.veskeli.nightrunner.healthsystem.HealthSystem;
import net.veskeli.nightrunner.healthsystem.ReviveSystem;

import java.lang.reflect.Field;
import java.util.*;

public class ModEvents {

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player newPlayer = event.getEntity();

        // Load health stats
        HealthSystem.loadPlayerMaxHealth(newPlayer);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        Mana mana = player.getData(ModAttachments.PLAYER_MANA);

        // Load health stats
        HealthSystem.loadPlayerMaxHealth(player);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        event.setCanceled(true); // Cancel actual death
        player.setGameMode(GameType.SPECTATOR); // Set to spectator
        player.setHealth(1.0f); // Set to 0.5 heart so they don't die after spectator switch

        // Send title
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("You died").withStyle(style -> style.withColor(ChatFormatting.RED))));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));

        // Summon grave
        SummonGraveForPlayer(player);

        // Drop items to the floor (inventory)
        dropItemsToFloor(player);

        // Drop experience orbs (player's experience)
        dropExperience(player);

        // Store inventory
        //GraveDataStore.storeInventory(player.getUUID(), new ArrayList<>(player.getInventory().items));
        player.getInventory().clearContent();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()) {
            return; // Exit if on the client side
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack itemInHand = event.getItem();

        // Try to use health modifier item
        HealthSystem.tryUseHealthModifierItem(event, player, itemInHand);
    }

    private void dropItemsToFloor(ServerPlayer player) {
        // Get the player's inventory and drop each item
        for (ItemStack itemStack : player.getInventory().items) {
            if (!itemStack.isEmpty()) {
                // Drop the item in the world at the player's current position
                ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), itemStack);
                player.level().addFreshEntity(itemEntity);
            }
        }
        // Clear the player's inventory (if needed)
        player.getInventory().clearContent();
    }

    private void dropExperience(ServerPlayer player) {
        // Get the player's experience level and total experience
        int experience = player.totalExperience;
        // Drop experience orbs
        if (experience > 0) {
            ExperienceOrb experienceOrb = new ExperienceOrb(player.level(), player.getX(), player.getY(), player.getZ(), experience);
            player.level().addFreshEntity(experienceOrb);
        }
    }

    private static void SummonGraveForPlayer(ServerPlayer player) {
        if (player.level().isClientSide()) return;

        GraveEntity grave = new GraveEntity(ModEntities.GRAVE.get(), player.level());
        grave.setPos(player.getX(), player.getY(), player.getZ());

        grave.setCustomName(Component.literal(player.getName().getString() + "'s Grave"));
        grave.setOwner(player.getUUID());

        player.level().addFreshEntity(grave);
    }

    private static ListTag floatArray(float[] values) {
        ListTag list = new ListTag();
        for (float v : values) {
            list.add(FloatTag.valueOf(v));
        }
        return list;
    }

    @SubscribeEvent
    public void onGraveRightClick(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getTarget() instanceof GraveEntity grave)) return;
        if (!(event.getEntity() instanceof ServerPlayer interactor)) return;

        ItemStack itemInHand = event.getItemStack();

        ReviveSystem.TryRevive(event, grave, interactor, itemInHand);
    }
}
