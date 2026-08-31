package gml.openstuff.integration.opencomputers;

import gml.openstuff.OpenStuff;
import li.cil.oc.api.Network;
import li.cil.oc.api.UnrecoverablePersistanceException;
import li.cil.oc.api.driver.DeviceInfo;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.*;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

import java.util.Map;
import java.util.Optional;

public class TrimDriver extends DriverItem {

    @Override
    public boolean worksWith(final ItemStack stack){
        return stack.is(ItemTags.TRIM_TEMPLATES);
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        if(host instanceof ArmorHost armor)
            return new Trim(stack, armor);
        return null;
    }

    @Override
    public String slot(ItemStack stack) {
        return "trim";
    }

    public ArmorTrim getTrim(ItemStack holder){
        if (holder.has(DataComponents.TRIM)) {
            return holder.get(DataComponents.TRIM);
        }
        return null;
    }

    public int getColor(ItemStack holder){
        if (holder.has(DataComponents.DYED_COLOR)) {
            return holder.get(DataComponents.DYED_COLOR).rgb();
        }
        return 0xFFFFFFFF;
    }


    public static class Trim extends AbstractManagedEnvironment implements DeviceInfo {
        private final ArmorTrim armor_trim;
        private int color = 0xFFFFFFFF;
        private final ArmorHost host;
        private final ItemStack trim_stack;

        public Trim(ItemStack trim_stack, ArmorHost host) {
            RegistryAccess registries = host.getEnvironmentLevel().registryAccess();

            this.host = host;
            this.trim_stack = trim_stack;


            Optional<Holder.Reference<TrimPattern>> pattern = TrimPatterns.getFromTemplate(registries, trim_stack);
            Optional<Holder.Reference<TrimMaterial>> material = registries.lookupOrThrow(Registries.TRIM_MATERIAL).get(TrimMaterials.IRON);
            if (pattern.isPresent() && material.isPresent())
                armor_trim = new ArmorTrim(material.get(), pattern.get());
            else armor_trim = null;

            setNode(Network.newNode(this, Visibility.Neighbors)
                    .withComponent("trim").
                    create());
        }

        @Override
        public void loadData(DataComponentHolder holder) throws UnrecoverablePersistanceException {
            super.loadData(holder);

            if (holder.has(DataComponents.DYED_COLOR)) {
                this.color = holder.get(DataComponents.DYED_COLOR).rgb();
            }
        }

        @Override
        public void saveData(MutableDataComponentHolder holder) {
            super.saveData(holder);

            holder.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(this.color, true));
            holder.set(DataComponents.TRIM, this.armor_trim);

        }


        @Override
        public Map<String, String> getDeviceInfo() {
            return Map.of(
                    DeviceAttribute.Class, DeviceClass.Display,
                    DeviceAttribute.Description, "Keyboard",
                    DeviceAttribute.Vendor, "Grahmibdol's Forge & Ironworks",
                    DeviceAttribute.Product, armor_trim.pattern().getRegisteredName()
            );
        }

        @Callback(doc = "function():number -- Sets the color of the armor.")
        public Object[] setColor(Context context, Arguments args) {
            this.color = 0xff000000 | (0xffffff & args.checkInteger(0));
            this.host.markChanged();
            return new Object[]{this.color};
        }

        @Callback(doc = "function():number -- Gets the color of the armor.")
        public Object[] getColor(Context context, Arguments args) {
            return new Object[]{this.color};
        }
    }
}
