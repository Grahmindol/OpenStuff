package ayral.gml.integration;

import li.cil.oc.api.machine.MachineHost;
import li.cil.oc.api.network.EnvironmentHost;
import net.minecraft.entity.player.PlayerEntity;

public interface ArmorHost extends EnvironmentHost, MachineHost {
    PlayerEntity player();
}
