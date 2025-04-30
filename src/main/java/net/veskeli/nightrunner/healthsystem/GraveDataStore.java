package net.veskeli.nightrunner.healthsystem;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class GraveDataStore {
    private static final Map<UUID, List<ItemStack>> GRAVE_ITEMS = new HashMap<>();

    public static void storeInventory(UUID playerId, List<ItemStack> inventory) {
        GRAVE_ITEMS.put(playerId, inventory);
    }

    public static List<ItemStack> retrieveInventory(UUID playerId) {
        return GRAVE_ITEMS.remove(playerId); // Remove after retrieval
    }

    public static boolean hasGrave(UUID playerId) {
        return GRAVE_ITEMS.containsKey(playerId);
    }
}
