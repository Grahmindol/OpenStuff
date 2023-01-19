package com.ayral.openstuff;

import com.ayral.openstuff.items.OpenStuffItems;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class RegisteringHandler {
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(OpenStuffItems.openBoots,OpenStuffItems.openLeggings,OpenStuffItems.openChestPlate, OpenStuffItems.openHelmet
                ,OpenStuffItems.turretUpgrade);
    }
}
