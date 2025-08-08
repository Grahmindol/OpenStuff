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
import li.cil.oc.common.item.TabletWrapper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import static ayral.gml.item.OpenStuffItems.FLYING_UPGRADE;

public class FlyingComponent extends DriverItem {
    public FlyingComponent() {
        super(new ItemStack(FLYING_UPGRADE.get()) );
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack itemStack, EnvironmentHost host) {
        if (!(host instanceof TabletWrapper)) return null;
        if (!itemStack.getOrCreateTag().getBoolean("is_on_armor")) return null;
        return new Environment((TabletWrapper) host);
    }

    @Override
    public String slot(ItemStack itemStack) {
        return Slot.Upgrade();
    }

    public static final class Environment extends AbstractManagedEnvironment{
        private final TabletWrapper host;

        public Environment(TabletWrapper host) {
            this.host = host;
            setNode(Network.newNode(this, Visibility.Network)
                    .withComponent("flying")
                    .create());
        }

        @Callback(doc = "function() -- start fall flying.")
        public Object[] enableFlying(Context context, Arguments args) {
            ItemStack chestStack = host.player().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                ((OpenArmorItem) chestStack.getItem()).setFlightEnabled(chestStack,true);
                return new Object[]{true};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function() -- stop fall flying.")
        public Object[] disableFlying(Context context, Arguments args) {
            ItemStack chestStack = host.player().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                return new Object[]{((OpenArmorItem) chestStack.getItem()).setFlightEnabled(chestStack,false)};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function():boolean -- check if is fall flying.")
        public Object[] isFlyingEnable(Context context, Arguments args) {
            ItemStack chestStack = host.player().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                return new Object[]{((OpenArmorItem) chestStack.getItem()).isFlyingEnable(chestStack)};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function():boolean -- check if is on ground.")
        public Object[] isOnGround(Context context, Arguments args) {
            return new Object[]{host.player().isOnGround()};
        }
    }
}
