package net.veskeli.nightrunner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.custom.GraveEntity;
import net.veskeli.nightrunner.healthsystem.GraveDataStore;
import net.veskeli.nightrunner.healthsystem.HealthSystem;
import net.veskeli.nightrunner.healthsystem.ReviveSystem;
import net.veskeli.nightrunner.item.ModItems;

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

        // Send the mana data to the client
        Mana.replicateData(mana, (ServerPlayer) player);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Respect totems in either hand before applying custom death flow.
        if (consumeTotemIfPresent(player)) {
            event.setCanceled(true);
            return;
        }

        event.setCanceled(true); // Cancel actual death
        player.setGameMode(GameType.SPECTATOR); // Set to spectator
        player.setHealth(1.0f); // Set to 0.5 heart so they don't die after spectator switch

        UUID graveId = UUID.randomUUID();

        applyHalfXpPenalty(player);
        storeInventoryWithoutVanishing(player, graveId);
        broadcastDeathDetails(player);

        // Send title
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("You died").withStyle(style -> style.withColor(ChatFormatting.RED))));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));

        // Summon grave
        SummonGraveForPlayer(player, graveId);

        // Drop items to the floor (inventory)
        //dropItemsToFloor(player);

        // Drop experience orbs (player's experience)
        //dropExperience(player);
    }

    private static boolean consumeTotemIfPresent(ServerPlayer player) {
        if (consumeTotemFromHand(player, InteractionHand.MAIN_HAND) || consumeTotemFromHand(player, InteractionHand.OFF_HAND)) {
            player.setHealth(1.0f);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    private static boolean consumeTotemFromHand(ServerPlayer player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.TOTEM_OF_UNDYING)) {
            return false;
        }

        held.shrink(1);
        return true;
    }

    private static void applyHalfXpPenalty(ServerPlayer player) {
        int keptXp = Math.max(0, player.totalExperience / 2);
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        player.totalExperience = 0;
        player.experienceProgress = 0.0f;
        player.giveExperiencePoints(keptXp);
    }

    private static void storeInventoryWithoutVanishing(ServerPlayer player, UUID graveId) {
        int size = player.getInventory().getContainerSize();
        List<ItemStack> stored = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            stored.add(ItemStack.EMPTY);
        }

        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (hasVanishingCurse(stack)) {
                continue;
            }

            stored.set(i, stack.copy());
        }

        GraveDataStore.storeInventory(player.serverLevel(), player.getUUID(), graveId, stored);
        player.getInventory().clearContent();
    }

    private static void broadcastDeathDetails(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        String dimensionId = player.level().dimension().location().toString();

        Component details = Component.literal(String.format(" [Dim: %s | XYZ: %d %d %d]", dimensionId, pos.getX(), pos.getY(), pos.getZ()))
                .withStyle(ChatFormatting.AQUA);

        // Use vanilla death message so mob/type/cause details stay localized and consistent.
        Component vanillaDeathMessage = player.getCombatTracker().getDeathMessage().copy();
        MutableComponent fullMessage = Component.empty().append(vanillaDeathMessage).append(details);
        player.server.getPlayerList().broadcastSystemMessage(fullMessage, false);
    }

    private static boolean hasVanishingCurse(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (enchantment.unwrapKey().isPresent()
                    && "minecraft:vanishing_curse".equals(enchantment.unwrapKey().get().location().toString())) {
                return true;
            }
        }
        return false;
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
        boolean healthSuccess = HealthSystem.tryUseHealthModifierItem(event, player, itemInHand);

        if(healthSuccess)
        {
            return; // Exit if health modifier item was used successfully
        }

        // Check if the item is heart fruit or heart fruit plus
        if(itemInHand.is(ModItems.HeartFruit))
        {
            HealthSystem.addTemporaryHealth(player, 1.0f, 2.0f);
        }
        else if(itemInHand.is(ModItems.HeartFruitPlus))
        {
            HealthSystem.addTemporaryHealth(player, 3.0f, 6.0f);
        }
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

    private static void SummonGraveForPlayer(ServerPlayer player, UUID graveId) {
        if (player.level().isClientSide()) return;

        GraveEntity grave = new GraveEntity(ModEntities.GRAVE.get(), player.level());
        grave.setPos(player.getX(), player.getY(), player.getZ());

        grave.setCustomName(Component.literal(player.getName().getString() + "'s Grave"));
        grave.setOwner(player.getUUID());
        grave.setGraveId(graveId);

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

        UUID ownerId = grave.getOwner();
        if (ownerId != null && ownerId.equals(interactor.getUUID()) && itemInHand.isEmpty()) {
            claimGraveItemsAsDrops(event, grave, interactor);
            return;
        }

        ReviveSystem.TryRevive(event, grave, interactor, itemInHand);
    }

    private static void claimGraveItemsAsDrops(PlayerInteractEvent.EntityInteractSpecific event, GraveEntity grave, ServerPlayer owner) {
        UUID ownerId = grave.getOwner();
        UUID graveId = grave.getGraveId();
        if (ownerId == null || graveId == null) {
            owner.displayClientMessage(Component.literal("This grave has no recoverable inventory."), true);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        List<ItemStack> stored = GraveDataStore.consumeInventory(owner.serverLevel(), ownerId, graveId);
        if (stored != null) {
            for (ItemStack stack : stored) {
                if (stack.isEmpty()) {
                    continue;
                }

                ItemEntity drop = new ItemEntity(owner.level(), grave.getX(), grave.getY() + 0.5, grave.getZ(), stack.copy());
                owner.level().addFreshEntity(drop);
            }
        }

        grave.discard();
        owner.displayClientMessage(Component.literal("You claimed your grave. Items dropped on the ground."), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // Check if the item is golden apple
        if (stack.getItem() == Items.GOLDEN_APPLE) {
            event.getToolTip().add(Component.literal("Sets max health to 10 hearts").withStyle(ChatFormatting.GREEN));
            event.getToolTip().add(Component.literal("Grants +5 temporary hearts").withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.literal("Resets revive degradation to max").withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.literal("Does not reduce max health").withStyle(ChatFormatting.GRAY));
        }
        // Check if the item is golden carrot
        else if (stack.getItem() == Items.GOLDEN_CARROT) {
            event.getToolTip().add(Component.literal("Sets max health to 8 hearts").withStyle(ChatFormatting.GREEN));
            event.getToolTip().add(Component.literal("Resets revive degradation to max").withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.literal("Does not reduce max health").withStyle(ChatFormatting.GRAY));
        }
        // Check if the item is enchanted golden apple
        else if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            event.getToolTip().add(Component.literal("Sets max health to 11 hearts").withStyle(ChatFormatting.GREEN));
            event.getToolTip().add(Component.literal("Grants +8 temporary hearts").withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.literal("Resets revive degradation to max").withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.literal("Does not reduce max health").withStyle(ChatFormatting.GRAY));
        }
    }

}
