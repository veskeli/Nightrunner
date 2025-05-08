package net.veskeli.nightrunner.SpellSystem;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.SpellSystem.Spells.FireballSpell;
import net.veskeli.nightrunner.SpellSystem.Spells.IceKnifeSpell;

import java.util.function.Supplier;

public class ModSpells {
    // We use spells as an example for the registry here, without any details about what a spell actually is (as it doesn't matter).
    // Of course, all mentions of spells can and should be replaced with whatever your registry actually is.
    public static final ResourceKey<Registry<Spell>> SPELL_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "spells"));
    public static final Registry<Spell> SPELL_REGISTRY = new RegistryBuilder<>(SPELL_REGISTRY_KEY)
            // If you want to enable integer id syncing, for networking.
            // These should only be used in networking contexts, for example in packets or purely networking-related NBT data.
            .sync(true)
            // The default key. Similar to minecraft:air for blocks. This is optional.
            .defaultKey(ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "empty"))
            // Effectively limits the max count. Generally discouraged, but may make sense in settings such as networking.
            .maxId(256)
            // Build the registry.
            .create();

    public static final DeferredRegister<Spell> SPELLS = DeferredRegister.create(SPELL_REGISTRY, Nightrunner.MODID);

    // Register the spells

    public static final Supplier<Spell> FIREBALL = SPELLS.register("fireball", FireballSpell::new);
    public static final Supplier<Spell> ICEKNIFE = SPELLS.register("ice_knife", IceKnifeSpell::new);


    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}
