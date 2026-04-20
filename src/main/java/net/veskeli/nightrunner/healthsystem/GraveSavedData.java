package net.veskeli.nightrunner.healthsystem;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GraveSavedData extends SavedData {
    private static final String DATA_NAME = "nightrunner_grave_data";

    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_OWNER = "Owner";
    private static final String TAG_GRAVE_ID = "GraveId";
    private static final String TAG_SIZE = "Size";
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_SLOT = "Slot";
    private static final String TAG_ITEM = "Item";

    private final Map<UUID, List<ItemStack>> graveItemsByGraveId = new HashMap<>();
    private final Map<UUID, Set<UUID>> gravesByOwner = new HashMap<>();

    public static GraveSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(GraveSavedData::new, GraveSavedData::load),
                DATA_NAME
        );
    }

    public void storeInventory(UUID ownerId, UUID graveId, List<ItemStack> inventory) {
        graveItemsByGraveId.put(graveId, copyInventory(inventory));
        gravesByOwner.computeIfAbsent(ownerId, ignored -> new LinkedHashSet<>()).add(graveId);
        setDirty();
    }

    public List<ItemStack> peekInventory(UUID graveId) {
        List<ItemStack> stored = graveItemsByGraveId.get(graveId);
        return stored == null ? null : copyInventory(stored);
    }

    public List<ItemStack> consumeInventory(UUID ownerId, UUID graveId) {
        List<ItemStack> stored = graveItemsByGraveId.get(graveId);
        if (stored == null) {
            return null;
        }

        removeGrave(ownerId, graveId);
        return copyInventory(stored);
    }

    public void removeGrave(UUID ownerId, UUID graveId) {
        graveItemsByGraveId.remove(graveId);
        Set<UUID> ownerGraves = gravesByOwner.get(ownerId);
        if (ownerGraves != null) {
            ownerGraves.remove(graveId);
            if (ownerGraves.isEmpty()) {
                gravesByOwner.remove(ownerId);
            }
        }
        setDirty();
    }

    public boolean hasGrave(UUID graveId) {
        return graveItemsByGraveId.containsKey(graveId);
    }

    public Set<UUID> getGravesForOwner(UUID ownerId) {
        return new LinkedHashSet<>(gravesByOwner.getOrDefault(ownerId, Collections.emptySet()));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();

        for (Map.Entry<UUID, List<ItemStack>> entry : graveItemsByGraveId.entrySet()) {
            UUID graveId = entry.getKey();
            UUID ownerId = findOwnerForGrave(graveId);
            if (ownerId == null) {
                continue;
            }

            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(TAG_OWNER, ownerId);
            entryTag.putUUID(TAG_GRAVE_ID, graveId);

            List<ItemStack> inventory = entry.getValue();
            entryTag.putInt(TAG_SIZE, inventory.size());

            ListTag itemsTag = new ListTag();
            for (int slot = 0; slot < inventory.size(); slot++) {
                ItemStack stack = inventory.get(slot);
                if (stack == null || stack.isEmpty()) {
                    continue;
                }

                CompoundTag slotTag = new CompoundTag();
                slotTag.putInt(TAG_SLOT, slot);
                slotTag.put(TAG_ITEM, stack.save(registries, new CompoundTag()));
                itemsTag.add(slotTag);
            }

            entryTag.put(TAG_ITEMS, itemsTag);
            entries.add(entryTag);
        }

        tag.put(TAG_ENTRIES, entries);
        return tag;
    }

    private static GraveSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        GraveSavedData data = new GraveSavedData();
        ListTag entries = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);

        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entryTag = entries.getCompound(i);
            if (!entryTag.hasUUID(TAG_OWNER) || !entryTag.hasUUID(TAG_GRAVE_ID)) {
                continue;
            }

            UUID ownerId = entryTag.getUUID(TAG_OWNER);
            UUID graveId = entryTag.getUUID(TAG_GRAVE_ID);
            int size = Math.max(0, entryTag.getInt(TAG_SIZE));

            List<ItemStack> inventory = new ArrayList<>(size);
            for (int slot = 0; slot < size; slot++) {
                inventory.add(ItemStack.EMPTY);
            }

            ListTag itemsTag = entryTag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
            for (int j = 0; j < itemsTag.size(); j++) {
                CompoundTag slotTag = itemsTag.getCompound(j);
                int slot = slotTag.getInt(TAG_SLOT);
                if (slot < 0 || slot >= inventory.size() || !slotTag.contains(TAG_ITEM, Tag.TAG_COMPOUND)) {
                    continue;
                }

                ItemStack stack = ItemStack.parseOptional(registries, slotTag.getCompound(TAG_ITEM));
                inventory.set(slot, stack);
            }

            data.graveItemsByGraveId.put(graveId, copyInventory(inventory));
            data.gravesByOwner.computeIfAbsent(ownerId, ignored -> new LinkedHashSet<>()).add(graveId);
        }

        return data;
    }

    private UUID findOwnerForGrave(UUID graveId) {
        for (Map.Entry<UUID, Set<UUID>> entry : gravesByOwner.entrySet()) {
            if (entry.getValue().contains(graveId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static List<ItemStack> copyInventory(List<ItemStack> inventory) {
        List<ItemStack> copy = new ArrayList<>(inventory.size());
        for (ItemStack stack : inventory) {
            copy.add(stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
        return copy;
    }
}

