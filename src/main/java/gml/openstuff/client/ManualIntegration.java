package gml.openstuff.client;

import gml.openstuff.OpenStuff;
import li.cil.oc.api.Manual;
import li.cil.oc.api.prefab.TextureTabIconRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

// inspired by https://github.com/PC-Logix/OpenSecurity/blob/1.21.1/src/main/java/pcl/opensecurity/client/ManualIntegration.java
// under MIT License Copyright (c) 2016 PC-Logix
// took the 17/08/2026

@EventBusSubscriber(modid = OpenStuff.MOD_ID, value = Dist.CLIENT)
public final class ManualIntegration {
    @SubscribeEvent
    public static void registerManual(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Manual.addProvider(new WikiContentProvider());
            Manual.addTab(
                    new TextureTabIconRenderer(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/netherite_chestplate.png") ),
                    "OpenStuff",
                    "openstuff/Home");
        });
    }

    private ManualIntegration() {}
}
