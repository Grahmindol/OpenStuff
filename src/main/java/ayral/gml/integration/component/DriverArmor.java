package ayral.gml.integration.component;

import ayral.gml.integration.ArmorHost;
import ayral.gml.item.OpenArmorItem;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DeviceInfo;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import li.cil.oc.common.item.TabletWrapper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

public class DriverArmor extends DriverItem {
    public DriverArmor() {
        super(new ItemStack(Items.NETHERITE_CHESTPLATE));
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack itemStack, EnvironmentHost host) {
        if (!(host instanceof TabletWrapper)) return null;
        return new DriverArmor.Environment((TabletWrapper) host);
    }

    @Override
    public String slot(ItemStack itemStack) {
        return "";
    }

    public static final class Environment extends AbstractManagedEnvironment implements DeviceInfo  {
        private final LivingEntity holder;

        public Environment(EnvironmentHost host) {
            if (host instanceof TabletWrapper)
                this.holder = ((TabletWrapper) host).player();
            else if (host instanceof ArmorHost)
                this.holder = ((ArmorHost) host).getHolder();
            else this.holder = null; // this wouldn't append ....

            setNode(Network.newNode(this, Visibility.Network)
                    .withComponent("armor")
                    .create());
        }

        @Override
        public Map<String, String> getDeviceInfo() {
            return new HashMap<String,String>() {{
                put(DeviceAttribute.Class, DeviceClass.Generic);
                put(DeviceAttribute.Description, "Armor-mounted Tablet Extension");
                put(DeviceAttribute.Vendor, "Grahmindol Ind. ");
                put(DeviceAttribute.Product, "ArmorLink-1");
            }};
        }

        @Callback(doc = "function():number -- Get the RGB light color.")
        public Object[] getLightColor(Context context, Arguments args) {
            ItemStack chestStack = holder.getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                return new Object[]{((OpenArmorItem) chestStack.getItem()).getColor(chestStack)};
            }
            return new Object[]{null};
        }

        @Callback(doc = "function(color:number) -- Set the RGB light color.")
        public Object[] setLightColor(Context context, Arguments args) {
            int color = args.checkInteger(0);
            ItemStack chestStack = holder.getItemBySlot(EquipmentSlotType.CHEST);
            if (chestStack.getItem() instanceof OpenArmorItem) {
                ((OpenArmorItem) chestStack.getItem()).setColor(chestStack,color);
            }
            return new Object[]{};
        }

        @Callback(doc = "function():boolean -- check if the armor is on a player")
        public Object[] isHeld(Context context, Arguments args) {
            return new Object[]{holder instanceof PlayerEntity};
        }

    }
}
