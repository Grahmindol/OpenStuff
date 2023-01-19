package com.ayral.openstuff;

import com.ayral.openstuff.proxy.OpenStuffCommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = OpenStuffMod.MODID,
        name = OpenStuffMod.MOD_NAME,
        version = OpenStuffMod.VERSION,
        dependencies = "required-after:opencomputers;"
)
public class OpenStuffMod {

    public static final String MODID = "openstuff";
    public static final String MOD_NAME = "OpenStuff";
    public static final String VERSION = "1.1.1";

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MODID)
    public static OpenStuffMod INSTANCE;

    @SidedProxy(clientSide = "com.ayral.openstuff.proxy.OpenStuffClient", serverSide = "com.ayral.openstuff.proxy.OpenStuffServer")
    public static OpenStuffCommon proxy;

    public static Logger logger;

    /**
     * This is the constructor of this class.
     */
    public OpenStuffMod() {
        MinecraftForge.EVENT_BUS.register(new RegisteringHandler());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preinit(event.getSuggestedConfigurationFile());
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postinit();
    }
}
