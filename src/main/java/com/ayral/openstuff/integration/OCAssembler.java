package com.ayral.openstuff.integration;

import com.ayral.openstuff.items.ArmorUpgrade;
import com.ayral.openstuff.items.OpenStuffItems;
import com.ayral.openstuff.util.OpenArmorHost;
import li.cil.oc.api.IMC;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.common.Tier;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ayral.openstuff.items.OpenArmorItem.setDefaultEnergyTag;

@SuppressWarnings("unused")
public class OCAssembler {
    public static void init()
    {
        String path = "com.ayral.openstuff.integration.OCAssembler.";
        IMC.registerAssemblerFilter(path+"AssemblerFilter");
        IMC.registerAssemblerFilter("com.ayral.openstuff.Integration.AssemblerFilter");
        ArrayList<Pair<String, Integer>> componentSlots = new ArrayList<>();
        componentSlots.add(new ImmutablePair<>(Slot.Card, 3));
        componentSlots.add(new ImmutablePair<>(Slot.None, 0));
        componentSlots.add(new ImmutablePair<>(Slot.None, 0));
        componentSlots.add(new ImmutablePair<>(Slot.CPU, 2));
        componentSlots.add(new ImmutablePair<>(Slot.Memory, 3));
        registerCustomAssemblerTemplate("openHelmet",path+"openHelmetSelect",path+"openHelmetValidate",path+"openHelmetAssemble",null,null,componentSlots);
        registerCustomAssemblerTemplate("openChestPlate",path+"openChestPlateSelect",path+"openChestPlateValidate",path+"openChestPlateAssemble", path+"UpgradeFilter",new int[]{3,3,3,3,3,3,3,3,3},componentSlots);
        registerCustomAssemblerTemplate("openLeggings",path+"openLeggingsSelect",path+"openLeggingsValidate",path+"openLeggingsAssemble",null,null,componentSlots);
        registerCustomAssemblerTemplate("openBoots",path+"openBootsSelect",path+"openBootsValidate",path+"openBootsAssemble",null,null,componentSlots);
    }

    public static void registerCustomAssemblerTemplate(String name, String select, String validate, String assemble,String upgradeValidate, int[] upgradeTiers, Iterable<Pair<String, Integer>> componentSlots) {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("name", name);
        nbt.setString("select", select);
        nbt.setString("validate", validate);
        nbt.setString("assemble", assemble);



        int var12;
        int tier;
        NBTTagCompound slotNbt;

        NBTTagList upgradesNbt = new NBTTagList();
        if (upgradeTiers != null) {
            int[] var17 = upgradeTiers;
            var12 = upgradeTiers.length;

            for(tier = 0; tier < var12; ++tier) {
                slotNbt = new NBTTagCompound();
                slotNbt.setInteger("tier", var17[tier]);
                slotNbt.setString("validate", upgradeValidate);
                upgradesNbt.appendTag(slotNbt);
            }
        }

        if (upgradesNbt.tagCount() > 0) {
            nbt.setTag("upgradeSlots", upgradesNbt);
        }

        NBTTagList componentsNbt = new NBTTagList();
        if (componentSlots != null) {
            Iterator var19 = componentSlots.iterator();

            while(var19.hasNext()) {
                Pair<String, Integer> slot = (Pair)var19.next();
                if (slot == null) {
                    componentsNbt.appendTag(new NBTTagCompound());
                } else {
                    slotNbt = new NBTTagCompound();
                    slotNbt.setString("type", (String)slot.getLeft());
                    slotNbt.setInteger("tier", (Integer)slot.getRight());
                    componentsNbt.appendTag(slotNbt);
                }
            }
        }

        if (componentsNbt.tagCount() > 0) {
            nbt.setTag("componentSlots", componentsNbt);
        }

        FMLInterModComms.sendMessage("opencomputers", "registerAssemblerTemplate", nbt);
    }

    private static boolean hasComponentRequired(IInventory inventory){
        boolean hasWirelessCard = false;
        boolean hasRam = false;
        boolean hasProc = false;
        int size = inventory.getSizeInventory();
        for (int i = size - 1; i > 0; i--) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                if(inventory.getStackInSlot(i).getItem().toString().contentEquals("item.oc.card") && inventory.getStackInSlot(i).getMetadata() == 7){
                    hasWirelessCard = true;
                }
                if(inventory.getStackInSlot(i).getItem().toString().contentEquals("item.oc.component") && inventory.getStackInSlot(i).getMetadata() == 11){
                    hasRam = true;
                }
                if(inventory.getStackInSlot(i).getItem().toString().contentEquals("item.oc.component") && inventory.getStackInSlot(i).getMetadata() == 2){
                    hasProc = true;
                }
            }
        }

        return (hasWirelessCard && hasRam && hasProc);
    }

    public static boolean UpgradeFilter(IInventory inventory, int slot, int tier, ItemStack stack) {
        return stack.getItem() instanceof ArmorUpgrade;
    }

    public static boolean AssemblerFilter(ItemStack stack) {
        return stack.getItem() == Items.DIAMOND_BOOTS ||
                stack.getItem() == Items.DIAMOND_LEGGINGS||
                stack.getItem() == Items.DIAMOND_CHESTPLATE ||
                stack.getItem() == Items.DIAMOND_HELMET ||
                stack.getItem() == getRegisteredItem("opencomputers:casecreative") ||
                stack.getItem() == getRegisteredItem("opencomputers:case1") ||
                stack.getItem() == getRegisteredItem("opencomputers:case2") ||
                stack.getItem() == getRegisteredItem("opencomputers:case3") ||
                stack.getItem() == getRegisteredItem("opencomputers:material") ;
    }

    private static Item getRegisteredItem(String name)
    {
        Item item = Item.REGISTRY.getObject(new ResourceLocation(name));

        if (item == null)
        {
            throw new IllegalStateException("Invalid Item requested: " + name);
        }
        else
        {
            return item;
        }
    }

    public static boolean openHelmetSelect(ItemStack stack)
    {
        return stack.getItem() == Items.DIAMOND_HELMET;
    }
    public static Object[] openHelmetValidate(IInventory inventory)
    {
        if(hasComponentRequired(inventory)){
            return new Object[]{true};
        }
        return new Object[]{false};
    }
    public static Object[] openHelmetAssemble(IInventory inventory)
    {
        return new Object[]{setDefaultEnergyTag(new ItemStack(OpenStuffItems.openHelmet),720000,inventory),720000};
    }

    public static boolean openChestPlateSelect(ItemStack stack)
    {
        return stack.getItem() == Items.DIAMOND_CHESTPLATE;
    }
    public static Object[] openChestPlateValidate(IInventory inventory)
    {
        if(hasComponentRequired(inventory)){
            return new Object[]{true};
        }
        return new Object[]{false};
    }
    public static Object[] openChestPlateAssemble(IInventory inventory)
    {
        return new Object[]{setDefaultEnergyTag(new ItemStack(OpenStuffItems.openChestPlate),720000, inventory),720000};
    }

    public static boolean openLeggingsSelect(ItemStack stack)
    {
        return stack.getItem() == Items.DIAMOND_LEGGINGS;
    }
    public static Object[] openLeggingsValidate(IInventory inventory)
    {
        if(hasComponentRequired(inventory)){
            return new Object[]{true};
        }
        return new Object[]{false};
    }
    public static Object[] openLeggingsAssemble(IInventory inventory)
    {
        return new Object[]{setDefaultEnergyTag(new ItemStack(OpenStuffItems.openLeggings),720000, inventory),720000};
    }

    public static boolean openBootsSelect(ItemStack stack)
    {
        return stack.getItem() == Items.DIAMOND_BOOTS;
    }
    public static Object[] openBootsValidate(IInventory inventory)
    {
        if(hasComponentRequired(inventory)){
            return new Object[]{true};
        }
        return new Object[]{false};
    }
    public static Object[] openBootsAssemble(IInventory inventory)
    {
        return new Object[]{setDefaultEnergyTag(new ItemStack(OpenStuffItems.openBoots),720000, inventory),720000};
    }
}
