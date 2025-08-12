package ayral.gml;

import ayral.gml.integration.ArmorStandDriver;
import ayral.gml.integration.component.DriverArmor;
import ayral.gml.integration.component.DriverFlyingUpgrade;
import ayral.gml.integration.component.DriverSwimmingUpgrade;
import ayral.gml.item.OpenStuffItems;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import li.cil.oc.api.Driver;

import static ayral.gml.OpenStuffIMC.registerArmorAssembler;
import static ayral.gml.item.OpenStuffItems.ItemColorRegister;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(OpenStuffMod.MOD_ID)
public class OpenStuffMod
{
    public static final String MOD_ID = "openstuff";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public OpenStuffMod() {
        // Register the setup method for modloading
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        OpenStuffItems.register(eventBus);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        ArmorStandDriver driver = new ArmorStandDriver();
        MinecraftForge.EVENT_BUS.register(driver);
        Driver.add(driver);


        Driver.add(new DriverFlyingUpgrade());
        Driver.add(new DriverSwimmingUpgrade());
        Driver.add(new DriverArmor());


        NetworkHandler.register();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        ClientRegistry.registerKeyBinding(KeyInputHandler.KEY_OPEN_ARMOR);
        ItemColorRegister(event);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        registerArmorAssembler();
    }

    private void processIMC(final InterModProcessEvent event)
    {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}