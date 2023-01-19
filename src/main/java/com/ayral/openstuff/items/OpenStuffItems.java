package com.ayral.openstuff.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.ayral.openstuff.OpenStuffMod.MODID;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = MODID)
public class OpenStuffItems
{
    public static ItemArmor.ArmorMaterial openArmorMaterial = EnumHelper.addArmorMaterial("OpenArmor", "openstuff:open_armor",40, new int[]{3, 6, 8, 3}, 0, SoundEvents.ITEM_ARMOR_EQUIP_IRON,2.0F);
    public static final Item openHelmet  = new OpenArmorItem("openHelmet",openArmorMaterial,1, EntityEquipmentSlot.HEAD);
    public static final Item openChestPlate  = new OpenArmorItem("openChestPlate",openArmorMaterial,1,EntityEquipmentSlot.CHEST);
    public static final Item openLeggings  = new OpenArmorItem("openLeggings",openArmorMaterial,2,EntityEquipmentSlot.LEGS);
    public static final Item openBoots  = new OpenArmorItem("openBoots",openArmorMaterial,1,EntityEquipmentSlot.FEET);

    public static final Item turretUpgrade = new TurretUpgrade("turretUpgrade");

    public static void setItemName(Item item, String name)
    {
        item.setRegistryName(MODID, name).setTranslationKey(MODID + "." + name);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerItemsModels(ModelRegistryEvent event)
    {
        registerModel(openHelmet, 0);
        registerModel(openChestPlate, 0);
        registerModel(openLeggings, 0);
        registerModel(openBoots, 0);

        registerModel(turretUpgrade, 0);
    }

    @SideOnly(Side.CLIENT)
    public static void registerModel(Item item, int metadata)
    {
        if (metadata < 0) metadata = 0;
        System.out.println(item.getTranslationKey());
        String resourceName = item.getTranslationKey().substring(5).replace('.', ':');
        if (metadata > 0) resourceName += "_m" + String.valueOf(metadata);
        System.out.println(new ModelResourceLocation(resourceName, "inventory").getPath());
        ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(resourceName, "inventory"));
    }

}