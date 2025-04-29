package net.veskeli.nightrunner.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veskeli.nightrunner.Nightrunner;

public class ModItems {
    // Create a DeferredRegister to hold our items
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Nightrunner.MODID);

    // Wands
    public static final DeferredItem<Item> WoodenWand = ITEMS.register("wooden_wand", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> StoneWand = ITEMS.register("stone_wand", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GoldWand = ITEMS.register("gold_wand", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> IronWand = ITEMS.register("iron_wand", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DiamondWand = ITEMS.register("diamond_wand", () -> new Item(new Item.Properties()));

    // Staffs
    public static final DeferredItem<Item> WoodenStaff = ITEMS.register("wooden_staff", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> StoneStaff = ITEMS.register("stone_staff", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GoldStaff = ITEMS.register("gold_staff", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> IronStaff = ITEMS.register("iron_staff", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DiamondStaff = ITEMS.register("diamond_staff", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        // Register the DeferredRegister to the event bus so blocks get registered
        ITEMS.register(eventBus);
    }
}
