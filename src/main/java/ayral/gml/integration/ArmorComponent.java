package ayral.gml.integration;

import ayral.gml.item.OpenArmorItem;
import li.cil.oc.Settings;
import li.cil.oc.api.Nanomachines;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DeviceInfo;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ArmorComponent extends AbstractManagedEnvironment implements DeviceInfo {
    private final ArmorHost host;

    public ArmorComponent(ArmorHost host) {
        this.host = host;
        setNode(Network.newNode(this, Visibility.Network)
                .withComponent("armor") // Your component name visible to Lua
                .withConnector(Settings.get().bufferTablet())
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
        ItemStack chestStack = host.getHolder().getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.getItem() instanceof OpenArmorItem) {
            return new Object[]{((OpenArmorItem) chestStack.getItem()).getColor(chestStack)};
        }
        return new Object[]{null};
    }

    @Callback(doc = "function(color:number) -- Set the RGB light color.")
    public Object[] setLightColor(Context context, Arguments args) {
        int color = args.checkInteger(0);
        ItemStack chestStack = host.getHolder().getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.getItem() instanceof OpenArmorItem) {
            ((OpenArmorItem) chestStack.getItem()).setColor(chestStack,color);
            return new Object[]{true};
        }
        return new Object[]{false};
    }

    @Callback(doc = "function():boolean -- Return whether the player has nanomachines.")
    public Object[] hasNanomachines(Context context, Arguments args) {
        if (! (host.getHolder() instanceof PlayerEntity)) return new Object[]{false};
        return new Object[]{Nanomachines.hasController((PlayerEntity) host.getHolder())};
    }

    @Callback(doc = "function():boolean -- Return whether the armor is held by a player.")
    public Object[] isHeld(Context context, Arguments args) {
        return new Object[]{host.getHolder() instanceof PlayerEntity};
    }
}
