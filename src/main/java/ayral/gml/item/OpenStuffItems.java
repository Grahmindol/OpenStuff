package ayral.gml.item;

import ayral.gml.OpenStuffMod;
import li.cil.oc.api.CreativeTab;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class OpenStuffItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OpenStuffMod.MOD_ID);


    public static final RegistryObject<Item> OPEN_BOOTS = ITEMS.register("open_boots",
            () -> new OpenStuffArmorItem(EquipmentSlotType.FEET));

    public static final RegistryObject<Item> OPEN_CHESTPLATE = ITEMS.register("open_chestplate",
            () -> new OpenStuffArmorItem(EquipmentSlotType.CHEST));

    public static final RegistryObject<Item> OPEN_LEGGINGS = ITEMS.register("open_leggings",
            () -> new OpenStuffArmorItem(EquipmentSlotType.LEGS));

    public static final RegistryObject<Item> OPEN_HELMET = ITEMS.register("open_helmet",
            () -> new OpenStuffArmorItem(EquipmentSlotType.HEAD));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
