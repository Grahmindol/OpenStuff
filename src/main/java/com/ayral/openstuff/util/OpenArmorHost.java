package com.ayral.openstuff.util;

import li.cil.oc.api.network.EnvironmentHost;
import net.minecraft.world.World;

public class OpenArmorHost implements EnvironmentHost {
    @Override
    public World world() {
        return null;
    }

    @Override
    public double xPosition() {
        return 0;
    }

    @Override
    public double yPosition() {
        return 0;
    }

    @Override
    public double zPosition() {
        return 0;
    }

    @Override
    public void markChanged() {

    }
}
