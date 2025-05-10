package net.veskeli.nightrunner;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.monster.Ghast;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.veskeli.ModCommands;
import net.veskeli.nightrunner.SpellSystem.ModSpells;
import net.veskeli.nightrunner.client.ClientOnlyEvents;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.client.GraveRenderer;
import net.veskeli.nightrunner.item.ModCreativeModeTabs;
import net.veskeli.nightrunner.item.ModItems;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static net.veskeli.nightrunner.SpellSystem.ModSpells.SPELL_REGISTRY;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Nightrunner.MODID)
public class Nightrunner
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "nightrunner_difficulty";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Nightrunner(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // Register the registries
        modEventBus.addListener(this::registerRegistries);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        NeoForge.EVENT_BUS.register(new ModEvents());

        NeoForge.EVENT_BUS.register(new ClientEvents());

        //ClientEvents.register(modEventBus);

        //NeoForge.EVENT_BUS.register(new ManaEvents());

        //ModNetworkEvents.register(modEventBus);

        // Register the mod entities
        ModEntities.register(modEventBus);

        // Register the creative mode tab
        ModCreativeModeTabs.register(modEventBus);

        // Register the mod items
        ModItems.register(modEventBus);

        // Register the mod menu types
        ModMenuTypes.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Register the attachments
        ModAttachments.register(modEventBus);

        ModSpells.register(modEventBus);
        //modEventBus.addListener(ModEventSubscriber::register);
    }

    void registerRegistries(NewRegistryEvent event) {
        event.register(SPELL_REGISTRY);
    }

    @SubscribeEvent
    void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ModCommands.register(dispatcher);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("Nightrunner loaded");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntities.GRAVE.get(), GraveRenderer::new);

            EntityRenderers.register(ModEntities.MULTI_GHAST.get(), GhastRenderer::new);
        }

        // Key mapping for skill tree
        public static final Lazy<KeyMapping> SKILL_TREE_MAPPING = Lazy.of(() -> new KeyMapping(
                "key.nightrunner_difficulty.skill_tree",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_MOUSE_BUTTON_4,
                "key.categories.nightrunner"
        ));

        // Event is on the mod event bus only on the physical client
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(SKILL_TREE_MAPPING.get());
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientGameEvents
    {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            ClientOnlyEvents.onPlayerTick(event);
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEventSubscriber {

        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(ModEntities.MULTI_GHAST.get(), Ghast.createAttributes().build());
        }


        /*
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playBidirectional(
                    ManaSyncPacket.TYPE,
                    ManaSyncPacket.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleDataOnMain,
                            ServerPayloadHandler::handleDataOnMain
                    )
            );
        }*/
    }
}
