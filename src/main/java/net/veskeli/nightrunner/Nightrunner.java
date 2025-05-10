package net.veskeli.nightrunner;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.SpellSystem.ModSpells;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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

        // Set spell level
        dispatcher.register(net.minecraft.commands.Commands.literal("SetSpellLevel")
                    .then(Commands.argument("Player", EntityArgument.player())
                    .then(Commands.argument("Amount", IntegerArgumentType.integer(0, 100))
                    .requires(source -> source.hasPermission(2)) // Permission level (2 = OP)
                    .executes(context -> {
                        Player player = EntityArgument.getPlayer(context, "Player");
                        int amount = IntegerArgumentType.getInteger(context, "Amount");

                        Mana mana = player.getData(ModAttachments.PLAYER_MANA);
                        mana.setSpellLevel(amount);

                        player.setData(ModAttachments.PLAYER_MANA, mana);

                        System.out.println("Is Server: " + context.getSource().getLevel().isClientSide());

                        return 1; // Return a success code
                    }))));
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
                GLFW.GLFW_KEY_I,
                "key.categories.nightrunner"
        ));

        // Event is on the mod event bus only on the physical client
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(SKILL_TREE_MAPPING.get());
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
