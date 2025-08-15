package ayral.gml.integration.component;

import ayral.gml.item.OpenArmorItem;
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

import static ayral.gml.item.OpenStuffItems.SWIMMING_UPGRADE;

public class DriverSwimmingUpgrade extends DriverItem {
    public DriverSwimmingUpgrade() {
        super(new ItemStack(SWIMMING_UPGRADE.get()) );
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
                    .withComponent("swimming")
                    .create());
            ItemStack chestStack = host.player().getItemBySlot(EquipmentSlotType.CHEST);
            OpenArmorItem.setSwamEnabled(chestStack,false);
        }

        @Callback(doc = "function() -- start swimming.")
        public Object[] enableSwimming(Context context, Arguments args) {
            ItemStack chestStack = host.player().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                return new Object[]{OpenArmorItem.setSwamEnabled(chestStack,true)};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function() -- stop swimming.")
        public Object[] disableSwimming(Context context, Arguments args) {
            ItemStack chestStack = host.player().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                return new Object[]{OpenArmorItem.setSwamEnabled(chestStack,false)};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function():boolean -- check if is swimming.")
        public Object[] isSwimmingEnable(Context context, Arguments args) {
            ItemStack chestStack = host.player().getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                return new Object[]{OpenArmorItem.isSwimmingEnable(chestStack)};
            }
            return new Object[]{false};
        }

        @Callback(doc = "function():boolean -- check if is on water.")
        public Object[] isOnWater(Context context, Arguments args) {
            return new Object[]{host.player().isInWater()};
        }
    }
}
