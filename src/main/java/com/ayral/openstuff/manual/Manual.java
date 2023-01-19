package com.ayral.openstuff.manual;

import com.ayral.openstuff.OpenStuffMod;
import net.minecraft.util.ResourceLocation;

public class Manual {
    private static ResourceLocation iconResourceLocation = new ResourceLocation(OpenStuffMod.MODID, "textures/items/open_chestplate_item.png");
    private static String tooltip = "OpenStuff";
    private static String homepage = "assets/" + OpenStuffMod.MODID + "/doc/_Sidebar";

    public static void preInit(){
        new ManualPathProvider().initialize(iconResourceLocation, tooltip, homepage);
    }


}
