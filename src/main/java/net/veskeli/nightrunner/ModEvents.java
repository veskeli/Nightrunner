package net.veskeli.nightrunner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.veskeli.nightrunner.healthsystem.GraveDataStore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;

public class ModEvents {

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player newPlayer = event.getEntity();
        newPlayer.setHealth(16.0f); // 8 hearts
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        player.setHealth(16.0f); // 8 hearts
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

        // Store inventory
        GraveDataStore.storeInventory(player.getUUID(), new ArrayList<>(player.getInventory().items));
        player.getInventory().clearContent();
    }

    private static void SummonGraveForPlayer(ServerPlayer player) {
        if (player.level().isClientSide()) return;
        
    }

    private static ListTag floatArray(float[] values) {
        ListTag list = new ListTag();
        for (float v : values) {
            list.add(FloatTag.valueOf(v));
        }
        return list;
    }
}
