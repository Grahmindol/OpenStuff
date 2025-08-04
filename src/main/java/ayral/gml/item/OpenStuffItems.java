package ayral.gml.item;

import ayral.gml.OpenStuffMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class OpenStuffItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OpenStuffMod.MOD_ID);

    public static final RegistryObject<Item> OPEN_HELMET = ITEMS.register("open_helmet",
            () -> new OpenStuffArmorItem(EquipmentSlotType.HEAD));

    public static final RegistryObject<Item> OPEN_CHESTPLATE = ITEMS.register("open_chestplate",
            () -> new OpenStuffArmorItem(EquipmentSlotType.CHEST));

    public static final RegistryObject<Item> OPEN_LEGGINGS = ITEMS.register("open_leggings",
            () -> new OpenStuffArmorItem(EquipmentSlotType.LEGS));

    public static final RegistryObject<Item> OPEN_BOOTS = ITEMS.register("open_boots",
            () -> new OpenStuffArmorItem(EquipmentSlotType.FEET));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static void ItemColorRegister(FMLClientSetupEvent event){
        event.enqueueWork(() -> {
            ItemColors itemColors = Minecraft.getInstance().getItemColors();

            itemColors.register((stack, layer) -> {
                        if (layer == 0 && stack.getItem() instanceof OpenStuffArmorItem) {
                            OpenStuffArmorItem armor = (OpenStuffArmorItem) stack.getItem();
                            int color = armor.getColor(stack);
                            if (!stack.getOrCreateTag().getBoolean("is_armor_running")) {
                                int red = ((color >>> 16) & 0xFF) >> 2;
                                int green = ((color >>> 8) & 0xFF) >> 2;
                                int blue = ((color >>> 0) & 0xFF) >> 2;
                                return (red << 16) | (green << 8) | blue;
                            }
                            return color;
                        }
                        return -1;
                    },
                    OpenStuffItems.OPEN_HELMET.get(),
                    OpenStuffItems.OPEN_CHESTPLATE.get(),
                    OpenStuffItems.OPEN_LEGGINGS.get(),
                    OpenStuffItems.OPEN_BOOTS.get());
        });
    }
}
