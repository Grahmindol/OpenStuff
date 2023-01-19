package com.ayral.openstuff.util;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Packet;
import li.cil.oc.api.network.WirelessEndpoint;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class OpenArmorManager implements WirelessEndpoint {
    private final String playerName;

    private BlockPos pos;
    private World world;

    private turret t = new turret();

    public OpenArmorManager(String playerNameIn,World worldIn){
        playerName = playerNameIn;
        pos = new BlockPos(0,0,0);
        world = worldIn;
        Network.joinWirelessNetwork(this);
    }

    protected void finalize()
    {
        Network.leaveWirelessNetwork(this);
    }

    private EntityPlayer getEntityPlayer(){
        int size = world.playerEntities.size();
        for (int i = size - 1; i >= 0; i--) {
            if (world.playerEntities.get(i).getName() == playerName) {
                return world.playerEntities.get(i);
            }
        }
        return null;
    }
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public int x() {
        return pos.getX();
    }

    @Override
    public int y() {
        return pos.getY();
    }

    @Override
    public int z() {
        return pos.getZ();
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public void receivePacket(Packet packet, WirelessEndpoint wirelessEndpoint) {
        Iterator<Object> it = Arrays.stream(packet.data()).iterator();
        Object o = it.next();
        if(o.getClass().isArray()) {
            if(byteArrayToStr((byte[]) o).equals("openarmor")) {
                if(it.hasNext()){
                    o = it.next();
                    if(o.getClass().isArray()){
                        try {
                            String MethodClassName = "com.ayral.openstuff.util.OpenArmorMethod";
                            Class<?> MethodClass = Class.forName(MethodClassName); // convert string classname to class
                            Object MethodClassObj = MethodClass.newInstance(); // invoke empty constructor
                            String methodName = byteArrayToStr((byte[]) o);
                            Method methodCalled = MethodClassObj.getClass().getMethod(methodName,this.getClass(), EntityPlayer.class, Packet.class);
                            methodCalled.invoke(MethodClassObj,this,getEntityPlayer(),packet);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                                 ClassNotFoundException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public String byteArrayToStr(byte[] b){
        char[] c = new char[b.length];
        for(int i = 0 ; i< b.length ; i++){ c[i] = (char) b[i]; }
        return new String(c);
    }

    public void update(BlockPos position, World entityWorld) {
        pos = position;
        world = entityWorld;
        Network.updateWirelessNetwork(this);
    }

    private List<HudString> customHud = new ArrayList();
    public void drawnHud(FontRenderer fontRenderer){
        int size = customHud.size();
        for (int i = size - 1; i >= 0; i--) {
            fontRenderer.drawString(customHud.get(i).txt,customHud.get(i).x,customHud.get(i).y,customHud.get(i).c);
        }
        fontRenderer.drawString(" ",0,0,0xffffff);
    }

    public boolean addHudString(String txt, int x, int y, int c){
        return customHud.add(new HudString(txt,x,y,c));
    }

    public void remHudString(String txt,int x,int y){
        int size = customHud.size();
        for (int i = size - 1; i >= 0; i--) {
            if(Objects.equals(customHud.get(i).txt, txt) && customHud.get(i).x == x && customHud.get(i).y == y){
                customHud.remove(i);
            }
        }
    }

    private class HudString{
        public final String txt;
        public final int x;
        public final int y;
        public final int c;

        public HudString(String tIn,int xIn,int yIn,int cIn){
            txt = tIn;
            x = xIn;
            y = yIn;
            c = cIn;
        }
    }

    public turret getTurret() {
        return t;
    }

    public void setTurret(turret t) {
        this.t = t;
    }

    protected class turret{
        boolean power = false;
        boolean armed = false;
        int cooled = 0;

        public boolean setArmed(boolean armed) {
            this.armed = armed;
            return this.armed;
        }

        public boolean isArmed() {
            return armed;
        }

        public boolean isReady(){
            return armed && power && (cooled < 1) ;
        }

        public boolean setPower(boolean power) {
            this.power = power;
            return this.power;
        }

        public boolean isPower() {
            return power;
        }

        public boolean isOnTarget() {
            return true;
        }

        public boolean fire(EntityPlayer player) {
            if (!this.power)
               return false;

            if (!this.armed)
               return false;

            if (this.cooled > 0)
               return false;

            //if (!tile.consumeEnergy(energyTurretStats.getEnergyUsage()))
            //    return new Object[] { false, "not enough energy" };

            this.cooled = 100;
            pcl.opensecurity.common.entity.EntityEnergyBolt bolt = new pcl.opensecurity.common.entity.EntityEnergyBolt(player.world);
            float Pitch = ((float)Math.PI) * player.getPitchYaw().x / 180;
            float Yaw = (((float)Math.PI) * player.getPitchYaw().y / 180) + (float)Math.PI;
            bolt.setHeading(Yaw, Pitch);
            bolt.setDamage(20);
            bolt.setPosition(player.posX , player.posY +  player.eyeHeight, player.posZ );

            player.world.playSound(null, player.getPosition(), pcl.opensecurity.common.SoundHandler.turretFire, SoundCategory.BLOCKS, 15.5F, 1.0F);
            player.world.spawnEntity(bolt);

            return true;
        }
    }
}
