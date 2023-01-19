package com.ayral.openstuff;

import com.ayral.openstuff.items.OpenStuffItems;
import com.ayral.openstuff.util.OpenArmorHost;
import li.cil.oc.api.IMC;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.fs.FileSystem;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


import java.util.ArrayList;
import java.util.concurrent.Callable;

import static com.ayral.openstuff.items.OpenArmorItem.setDefaultEnergyTag;

@SuppressWarnings("unused")
public class Integration {
    public static void init()
    {
        li.cil.oc.api.Items.registerFloppy("Open Armor Controller", EnumDyeColor.RED, new OCLootDiskFileSystem("shell/armorController"), true);
        IMC.registerProgramDiskLabel("armor_shell", "Open Armor Controller", "Lua 5.2", "Lua 5.3", "LuaJ");

        String path = "com.ayral.openstuff.Integration.";


    }

    private static class OCLootDiskFileSystem implements Callable<FileSystem> {
        private final String name;
        OCLootDiskFileSystem(String name) {
            this.name = name;
        }

        @Override
        @Optional.Method(modid = "opencomputers")
        public FileSystem call() {
            return li.cil.oc.api.FileSystem.asReadOnly(li.cil.oc.api.FileSystem.fromClass(OpenStuffMod.class, OpenStuffMod.MODID, "loot/" + this.name));
        }
    }




}
