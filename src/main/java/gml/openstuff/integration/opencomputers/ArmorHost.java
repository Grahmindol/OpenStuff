package gml.openstuff.integration.opencomputers;

import li.cil.oc.api.network.EnvironmentHost;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ArmorHost implements EnvironmentHost {
    private LivingEntity holder;

    public ArmorHost(LivingEntity holder){
        this.holder = holder;
    }

    public LivingEntity getHolder() {
        return holder;
    }

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

    }
}
