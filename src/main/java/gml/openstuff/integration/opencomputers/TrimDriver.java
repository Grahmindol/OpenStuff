package gml.openstuff.integration.opencomputers;

import gml.openstuff.OpenStuff;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DeviceInfo;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class TrimDriver extends DriverItem {

    @Override
    public boolean worksWith(final ItemStack stack){
        return stack.is(ItemTags.TRIM_TEMPLATES);
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        return new Trim(stack);
    }

    @Override
    public String slot(ItemStack stack) {
        return "trim";
    }

    public static class Trim extends AbstractManagedEnvironment implements DeviceInfo {
        public Trim(ItemStack stack) {
            setNode(Network.newNode(this, Visibility.Neighbors).
                    create());
        }

        @Override
        public void onConnect(final Node node) {
            super.onConnect(node);
        }

        @Override
        public Map<String, String> getDeviceInfo() {
            return Map.of();
        }
    }
}
