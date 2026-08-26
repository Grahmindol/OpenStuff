package gml.openstuff;

import gml.openstuff.item.OpenBoots;
import gml.openstuff.item.OpenChestplate;
import gml.openstuff.item.OpenHelmet;
import gml.openstuff.item.OpenLeggings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(OpenStuff.MOD_ID)
public final class OpenStuff {
    public static final String MOD_ID = "openstuff";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredHolder<Item, ArmorItem> OPEN_HELMET = ITEMS.register("open_helmet",() -> new OpenHelmet(new Item.Properties()));
    public static final DeferredHolder<Item, ArmorItem> OPEN_CHEST = ITEMS.register("open_chest",() -> new OpenChestplate(new Item.Properties()));
    public static final DeferredHolder<Item, ArmorItem> OPEN_LEGS = ITEMS.register("open_legs",() -> new OpenLeggings(new Item.Properties()));
    public static final DeferredHolder<Item, ArmorItem> OPEN_BOOTS = ITEMS.register("open_boots",() -> new OpenBoots(new Item.Properties()));


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
