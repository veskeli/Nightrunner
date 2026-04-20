package net.veskeli.nightrunner.healthsystem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class GraveDataStore {
    public static void storeInventory(ServerLevel level, UUID ownerId, UUID graveId, List<ItemStack> inventory) {
        GraveSavedData.get(level).storeInventory(ownerId, graveId, inventory);
    }

    public static List<ItemStack> peekInventory(ServerLevel level, UUID graveId) {
        return GraveSavedData.get(level).peekInventory(graveId);
    }

    public static List<ItemStack> consumeInventory(ServerLevel level, UUID ownerId, UUID graveId) {
        return GraveSavedData.get(level).consumeInventory(ownerId, graveId);
    }

    public static void removeGrave(ServerLevel level, UUID ownerId, UUID graveId) {
        GraveSavedData.get(level).removeGrave(ownerId, graveId);
    }

    public static boolean hasGrave(ServerLevel level, UUID graveId) {
        return GraveSavedData.get(level).hasGrave(graveId);
    }

    public static Set<UUID> getGravesForOwner(ServerLevel level, UUID ownerId) {
        return GraveSavedData.get(level).getGravesForOwner(ownerId);
    }
}
