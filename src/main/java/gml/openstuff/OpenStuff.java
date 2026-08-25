package gml.openstuff;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(OpenStuff.MOD_ID)
public final class OpenStuff {
    public static final String MOD_ID = "openstuff";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    //public static final DeferredHolder<Item, SecurityCardItem> RFID_CARD = ITEMS.register(...);



    public OpenStuff(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(OpenStuff::commonSetup);
    }


    private static void commonSetup(FMLCommonSetupEvent event) {
        //event.enqueueWork(() -> li.cil.oc.api.Driver.add(new Driver()));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
