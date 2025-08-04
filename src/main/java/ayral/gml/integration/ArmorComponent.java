package ayral.gml.integration;

import ayral.gml.item.OpenStuffArmorItem;
import li.cil.oc.Constants;
import li.cil.oc.Settings;
import li.cil.oc.api.Nanomachines;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DeviceInfo;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ArmorComponent extends AbstractManagedEnvironment implements DeviceInfo {

    private final PlayerEntity player;
    private final Node node; // Node field

    private final Map<String, String> deviceInfo = new HashMap<String, String>() {{
        put(DeviceAttribute.Class, DeviceClass.Generic);
        put(DeviceAttribute.Description, "Armor-mounted Tablet Extension");
        put(DeviceAttribute.Vendor, "DefaultVendor");
        put(DeviceAttribute.Product, "ArmorLink-1");
    }};

    public ArmorComponent(PlayerEntity player) {
        this.player = player;
        this.node = Network.newNode(this, Visibility.Network)
                .withComponent("armor") // Your component name visible to Lua
                .withConnector(Settings.get().bufferTablet())
                .create();
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public Map<String, String> getDeviceInfo() {
        return deviceInfo;
    }

    @Callback(doc = "function():number -- Get the RGB light color.")
    public Object[] getLightColor(Context context, Arguments args) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.getItem() instanceof OpenStuffArmorItem) {
            return new Object[]{((OpenStuffArmorItem) chestStack.getItem()).getColor(chestStack)};
        }
        return new Object[]{null};
    }

    @Callback(doc = "function(color:number) -- Set the RGB light color.")
    public Object[] setLightColor(Context context, Arguments args) {
        int color = args.checkInteger(0);
        ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.getItem() instanceof OpenStuffArmorItem) {
            ((OpenStuffArmorItem) chestStack.getItem()).setColor(chestStack,color);
            return new Object[]{true};
        }
        return new Object[]{false};
    }

    @Callback(doc = "function():boolean -- Return whether the player has nanomachines.")
    public Object[] hasNanomachines(Context context, Arguments args) {
        return new Object[]{Nanomachines.hasController(player)};
    }
}
