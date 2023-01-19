package com.ayral.openstuff;

import com.ayral.openstuff.items.OpenStuffItems;
import com.ayral.openstuff.util.OpenArmorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class EventHandler {
    List<OpenArmorManager> OpenArmorManagerList = new ArrayList();

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if(event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (hasManager(player)) {
                if (!hasArmor(player)) {
                    System.out.println("delete ArmorManager");
                    int size = OpenArmorManagerList.size();
                    for (int i = size - 1; i >= 0; i--) {
                        if (OpenArmorManagerList.get(i).getPlayerName() == player.getName()) {
                            OpenArmorManagerList.remove(i);
                        }
                    }
                }
            } else if (hasArmor(player)) {
                System.out.println("make new ArmorManager");
                OpenArmorManagerList.add(new OpenArmorManager(player.getName(), player.world));
            }

            int size = OpenArmorManagerList.size();
            for (int i = size - 1; i >= 0; i--) {
                if (OpenArmorManagerList.get(i).getPlayerName() == player.getName()) {
                    OpenArmorManagerList.get(i).update(player.getPosition(), player.world);
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderGameOverlay(RenderGameOverlayEvent event){
        if(event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            int size = OpenArmorManagerList.size();
            for (int i = size - 1; i >= 0; i--) {
                OpenArmorManagerList.get(i).drawnHud(Minecraft.getMinecraft().fontRenderer);
            }
        }
    }

    private boolean hasArmor(EntityPlayer player) {
        return player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == OpenStuffItems.openHelmet && player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).hasTagCompound() &&
                player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == OpenStuffItems.openChestPlate && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).hasTagCompound() &&
                player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == OpenStuffItems.openLeggings && player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).hasTagCompound() &&
                player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == OpenStuffItems.openBoots && player.getItemStackFromSlot(EntityEquipmentSlot.FEET).hasTagCompound();
    }

    private boolean hasManager(EntityPlayer player) {
        int size = OpenArmorManagerList.size();
        for (int i = size - 1; i >= 0; i--) {
            if (OpenArmorManagerList.get(i).getPlayerName() == player.getName()) {
                return true;
            }
        }
        return false;
    }
}
