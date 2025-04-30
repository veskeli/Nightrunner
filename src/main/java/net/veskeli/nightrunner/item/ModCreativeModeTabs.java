package net.veskeli.nightrunner.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veskeli.nightrunner.Nightrunner;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Nightrunner.MODID);

    public static final Supplier<CreativeModeTab> NIGHTRUNNER_ITEMS_TAB = CREATIVE_MODE_TABS.register("nightrunner_items_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack((ModItems.DiamondStaff.get())))
            .title(Component.translatable("creativetab.nightrunner_difficulty.nightrunner_items"))
            .displayItems((itemDisplayParameters, output) -> {
                // Wands
                output.accept(ModItems.WoodenWand.get());
                output.accept(ModItems.StoneWand.get());
                output.accept(ModItems.GoldWand.get());
                output.accept(ModItems.IronWand.get());
                output.accept(ModItems.DiamondWand.get());

                // Staffs
                output.accept(ModItems.WoodenStaff.get());
                output.accept(ModItems.StoneStaff.get());
                output.accept(ModItems.GoldStaff.get());
                output.accept(ModItems.IronStaff.get());
                output.accept(ModItems.DiamondStaff.get());

                // Revive items
                output.accept(ModItems.Soulstone.get());
            }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
