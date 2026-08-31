package gml.openstuff.integration.opencomputers;

import gml.openstuff.ItemMachineWrapper;
import li.cil.oc.api.network.EnvironmentHost;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ArmorHost implements EnvironmentHost {
    private LivingEntity holder;
    private EquipmentSlot slot;
    private ItemMachineWrapper wrapper;

    public ArmorHost(ItemMachineWrapper wrapper, EquipmentSlot slot){
        this.wrapper = wrapper;
        this.holder = wrapper.player;
        this.slot = slot;
    }

    public LivingEntity getHolder() {
        return holder;
    }
    public EquipmentSlot getSlot() {return slot; }

    @Override
    public Level getEnvironmentLevel() {return holder.level();}
    @Override
    public double xPosition() { return holder.getX();}
    @Override
    public double yPosition() { return holder.getY();}
    @Override
    public double zPosition() { return holder.getZ();}
    @Override
    public void markChanged() {
        wrapper.setChanged();
    }
}
