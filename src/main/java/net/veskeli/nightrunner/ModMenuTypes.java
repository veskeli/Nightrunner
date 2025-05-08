package net.veskeli.nightrunner;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Nightrunner.MODID);

    public static void register(IEventBus eventBus)
    {
        MENUS.register(eventBus);
    }
}
