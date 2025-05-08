package net.veskeli.nightrunner;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.veskeli.nightrunner.ManaSystem.IMana;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.SpellSystem.Attachment.SpellAttachment;
import net.veskeli.nightrunner.healthsystem.HealthStats;

import java.util.function.Supplier;

public class ModAttachments {
    // Create the DeferredRegister for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Nightrunner.MODID);

    // Mana
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Mana>> PLAYER_MANA =
            ATTACHMENT_TYPES.register("player_mana", () ->
                    AttachmentType.serializable(Mana::new).build());

    // HealthStats
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HealthStats>> PLAYER_HEALTH_STATS =
            ATTACHMENT_TYPES.register("player_health_stats", () ->
                    AttachmentType.serializable(HealthStats::new).build());

    // Spell attachment
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SpellAttachment>> PLAYER_SPELLS =
            ATTACHMENT_TYPES.register("player_spells", () ->
                    AttachmentType.serializable(SpellAttachment::new).build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
