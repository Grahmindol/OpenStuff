package com.ayral.openstuff.util;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Packet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings("unused")
public class OpenArmorMethod {
    private static void wirelessReturn(OpenArmorManager m,Packet p,Object[] o){
        Network.sendWirelessPacket(m,10,Network.newPacket(p.destination(),p.source(), p.port(), o));
    }
    /*
    function toList(a)local b={}for c in string.gmatch(a,"[^%s]+")do if c=="true"then table.insert(b,true)elseif c=="false"then table.insert(b,false)elseif tonumber(c)~=nil then table.insert(b,tonumber(c))else table.insert(b,c)end end;return table.unpack(b)end;event.listen("modem_message",print)component.modem.open(1)while true do component.modem.broadcast(1,"openarmor",toList(io.read()))end
    */

    public static void setColor(OpenArmorManager m, EntityPlayer player, Packet p){
        int color = ((Number)p.data()[2]).intValue() ;
        if(color > 15){color = 15;}
        if(color < 0){color = 0;}
        for (ItemStack stack : player.getArmorInventoryList()) {
            if (stack.hasTagCompound()) {
                stack.getTagCompound().setInteger("color", color);
            }
        }
        wirelessReturn(m,p, new Object[]{color});
    }

    public static void getColor(OpenArmorManager m, EntityPlayer player, Packet p){
        Iterator<ItemStack> it = player.getArmorInventoryList().iterator();
        int color = it.next().getTagCompound().getInteger("color");
        while (it.hasNext()){
            ItemStack stack = it.next();
            if(stack.hasTagCompound()){
                stack.getTagCompound().setInteger("color",color);
            }
        }
        wirelessReturn(m,p,  new Object[]{color});
    }
    public static void displayText(OpenArmorManager m, EntityPlayer player, Packet p){
        m.addHudString(m.byteArrayToStr((byte[]) p.data()[2]),  ((Number)p.data()[3]).intValue(),((Number)p.data()[4]).intValue(),((Number)p.data()[5]).intValue());
    }

    public static void notDisplayText(OpenArmorManager m, EntityPlayer player, Packet p){
        m.remHudString(m.byteArrayToStr((byte[]) p.data()[2]),  ((Number)p.data()[3]).intValue(),((Number)p.data()[4]).intValue());
    }

    public static void getDimensionIndex(OpenArmorManager m, EntityPlayer player, Packet p){
        wirelessReturn(m,p,  new Object[]{player.dimension});
    }

    public static void getPosition(OpenArmorManager m, EntityPlayer player, Packet p){
        wirelessReturn(m,p,  new Object[]{player.getPosition().getX(),player.getPosition().getY(),player.getPosition().getZ()});
    }

    public static void getBedLocation(OpenArmorManager m, EntityPlayer player, Packet p){
        if (player.bedLocation != null) {
            wirelessReturn(m, p, new Object[]{player.bedLocation.getX(), player.bedLocation.getY(), player.bedLocation.getZ()});
        }
    }
    public static void hasBedLocation(OpenArmorManager m, EntityPlayer player, Packet p){
        wirelessReturn(m,p,  new Object[]{player.bedLocation != null});
    }

    public static void getExperienceLevel(OpenArmorManager m, EntityPlayer player, Packet p){
        wirelessReturn(m,p,  new Object[]{player.experienceLevel});
    }

    public static void getDisplayName(OpenArmorManager m, EntityPlayer player, Packet p){
        wirelessReturn(m,p,  new Object[]{player.getDisplayNameString()});
    }

    public static void getFoodLevel(OpenArmorManager m, EntityPlayer player, Packet p){
        wirelessReturn(m,p,  new Object[]{player.getFoodStats().getFoodLevel()});
    }

    public static void getHealth(OpenArmorManager m, EntityPlayer player, Packet p){
        wirelessReturn(m,p,  new Object[]{player.getHealth()});
    }

    public static void getSaturationLevel(OpenArmorManager m, EntityPlayer player, Packet p){
        wirelessReturn(m,p,  new Object[]{player.getFoodStats().getSaturationLevel()});
    }

    @Optional.Method(modid = "opensecurity")
    public static void turret(OpenArmorManager m, EntityPlayer player, Packet p) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getTagCompound().hasKey("turret")){
            String cmd = m.byteArrayToStr((byte[]) p.data()[2]);
            OpenArmorManager.turret t = m.getTurret();
            if(cmd.contentEquals("powerOn")){
                wirelessReturn(m,p,  new Object[]{t.setPower(true)});
            }else if(cmd.contentEquals("powerOff")){
                wirelessReturn(m,p,  new Object[]{t.setPower(false)});
            }else if(cmd.contentEquals("isReady")){
                wirelessReturn(m,p,  new Object[]{t.isReady()});
            }else if(cmd.contentEquals("isPowered")){
                wirelessReturn(m,p,  new Object[]{t.isPower()});
            }else if(cmd.contentEquals("setArmed")){
                wirelessReturn(m,p,  new Object[]{t.setArmed((Boolean) p.data()[3])});
            }else if(cmd.contentEquals("isOnTarget")){
                wirelessReturn(m,p,  new Object[]{t.isOnTarget()});
            }else if(cmd.contentEquals( "fire")){
                wirelessReturn(m,p,  new Object[]{t.fire(player)});
            }
            m.setTurret(t);
        }else{
            wirelessReturn(m,p,  new Object[]{"turret upgrade not found"});
        }
    }

}
