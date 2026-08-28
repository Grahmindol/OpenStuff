package gml.openstuff.integration.opencomputers;

import gml.openstuff.ItemMachineWrapper;
import gml.openstuff.OpenStuff;
import gml.openstuff.container.ManagedComponentInventory;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverItem;
import li.cil.oc.common.item.data.TabletData;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ArmorDriver extends DriverItem {
    @Override
    public boolean worksWith(final ItemStack stack){
        return stack.is(holder ->
                Set.of(OpenStuff.OPEN_HELMET.get(), OpenStuff.OPEN_CHEST.get(), OpenStuff.OPEN_LEGS.get(), OpenStuff.OPEN_BOOTS.get())
                        .contains(holder.value()));
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        if(host instanceof ItemMachineWrapper wrapper) return new Armor(stack, wrapper);
        return null;
    }

    @Override
    public String slot(ItemStack stack) {
        return "tablet";
    }

    public static class Armor extends ManagedComponentInventory {
        public ItemStack stack;
        public ItemMachineWrapper wrapper;
        public TabletData data = new TabletData();

        public Armor(ItemStack stack, ItemMachineWrapper wrapper){
            this.stack = stack;
            this.wrapper = wrapper;

            setNode(li.cil.oc.api.Network.newNode(this, li.cil.oc.api.network.Visibility.Network).
                    withComponent("armor").
                    create());
        }

        @Override
        public EnvironmentHost host() { return wrapper;}


        @Override
        public void setChanged() {
            this.saveData(stack);
            wrapper.setChanged();
        }

        @Override
        public ItemStack[] items() { return data.items();}

        @Override
        public boolean stillValid(@NotNull Player player) {
            return wrapper.stillValid(player);
        }

        @Override
        public void loadData(DataComponentHolder holder) {
            data.loadData(holder);
            super.loadData(holder);
        }

        @Override
        public void saveData(MutableDataComponentHolder holder) {
            data.saveData(holder);
            super.saveData(holder);
        }

        // connect the component directly to the wrapper.
        @Override
        public void connectItemNode(Node node) {
            this.wrapper.connectItemNode(node);
        }
    }
}
