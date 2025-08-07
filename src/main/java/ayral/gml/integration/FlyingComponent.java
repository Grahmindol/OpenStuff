package ayral.gml.integration;

import ayral.gml.item.OpenArmorItem;
import li.cil.oc.Constants;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import li.cil.oc.common.Slot;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class FlyingComponent extends DriverItem {
    @Override
    public ManagedEnvironment createEnvironment(ItemStack itemStack, EnvironmentHost host) {
        return new Environment((ArmorHost) host);
    }

    @Override
    public String slot(ItemStack itemStack) {
        return Slot.Upgrade();
    }

    public static final class Environment extends AbstractManagedEnvironment{
        private final ArmorHost host;

        public Environment(ArmorHost host) {
            this.host = host;
            setNode(Network.newNode(this, Visibility.Network)
                    .withComponent("flying")
                    .create());
        }

        @Callback(doc = "function() -- start fall flying.")
        public Object[] enableFlying(Context context, Arguments args) {
            ItemStack chestStack = host.getHolder().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                ((OpenArmorItem) chestStack.getItem()).setFlightEnabled(chestStack,true);
                return new Object[]{true};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function() -- stop fall flying.")
        public Object[] disableFlying(Context context, Arguments args) {
            ItemStack chestStack = host.getHolder().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                return new Object[]{((OpenArmorItem) chestStack.getItem()).setFlightEnabled(chestStack,false)};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function():boolean -- check if is fall flying.")
        public Object[] isFlyingEnable(Context context, Arguments args) {
            ItemStack chestStack = host.getHolder().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                return new Object[]{((OpenArmorItem) chestStack.getItem()).isFlyingEnable(chestStack)};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function():boolean -- check if is on ground.")
        public Object[] isOnGround(Context context, Arguments args) {
            return new Object[]{host.getHolder().isOnGround()};
        }
    }
}
