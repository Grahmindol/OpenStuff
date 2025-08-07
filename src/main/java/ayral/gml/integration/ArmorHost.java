package ayral.gml.integration;

import li.cil.oc.api.network.EnvironmentHost;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class ArmorHost implements EnvironmentHost {
    private LivingEntity holder;

    public ArmorHost(LivingEntity holder){
        this.holder = holder;
    }

    public LivingEntity getHolder() {
        return holder;
    }

    @Override
    public World world() {return holder.level;}
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
