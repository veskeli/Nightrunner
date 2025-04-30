package net.veskeli.nightrunner.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.item.custom.WandItem;
import net.veskeli.nightrunner.item.properties.WandItemProperties;

public class ModItems {
    // Create a DeferredRegister to hold our items
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Nightrunner.MODID);

    // Wands
    public static final DeferredItem<Item> WoodenWand = ITEMS.register("wooden_wand",
            () -> new WandItem((WandItemProperties) new WandItemProperties().power(3.0f).aoeRadius(0.5f).durability(59)));
    public static final DeferredItem<Item> StoneWand = ITEMS.register("stone_wand",
            () -> new WandItem((WandItemProperties) new WandItemProperties().power(4.0f).aoeRadius(0.5f).durability(131)));
    public static final DeferredItem<Item> GoldWand = ITEMS.register("gold_wand",
            () -> new WandItem((WandItemProperties) new WandItemProperties().power(3.0f).aoeRadius(1.0f).durability(32)));
    public static final DeferredItem<Item> IronWand = ITEMS.register("iron_wand",
            () -> new WandItem((WandItemProperties) new WandItemProperties().power(5.0f).aoeRadius(1.5f).durability(250)));
    public static final DeferredItem<Item> DiamondWand = ITEMS.register("diamond_wand",
            () -> new WandItem((WandItemProperties) new WandItemProperties().power(6.0f).aoeRadius(2.0f).durability(1561)));

    // Staffs
    public static final DeferredItem<Item> WoodenStaff = ITEMS.register("wooden_staff", () -> new Item(new Item.Properties().durability(59)));
    public static final DeferredItem<Item> StoneStaff = ITEMS.register("stone_staff", () -> new Item(new Item.Properties().durability(131)));
    public static final DeferredItem<Item> GoldStaff = ITEMS.register("gold_staff", () -> new Item(new Item.Properties().durability(32)));
    public static final DeferredItem<Item> IronStaff = ITEMS.register("iron_staff", () -> new Item(new Item.Properties().durability(250)));
    public static final DeferredItem<Item> DiamondStaff = ITEMS.register("diamond_staff", () -> new Item(new Item.Properties().durability(1561)));

    // Revive items
    public static final DeferredItem<Item> Soulstone = ITEMS.register("soulstone", () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus){
        // Register the DeferredRegister to the event bus so blocks get registered
        ITEMS.register(eventBus);
    }
}
