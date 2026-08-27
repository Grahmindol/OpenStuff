package gml.openstuff.container;

import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.util.Lifecycle;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import scala.collection.mutable.ArrayBuffer;

// TODO: burk.....
public abstract class ComponentInventory implements li.cil.oc.common.container.ComponentInventory {
    private boolean sizeInventoryReady = true;
    private ArrayBuffer<ManagedEnvironment> updatingComponentsBuffer = new ArrayBuffer<>();

    // ----------------------------------------------------------------------- //

    @Override
    public boolean isSizeInventoryReady() {
        return this.sizeInventoryReady;
    }

    @Override
    public void isSizeInventoryReady_$eq(boolean isSizeInventoryReady) {
        this.sizeInventoryReady = isSizeInventoryReady;
    }

    @Override
    public ArrayBuffer<ManagedEnvironment> updatingComponents() {
        return this.updatingComponentsBuffer;
    }

    // ----------------------------------------------------------------------- //

    @Override
    public abstract EnvironmentHost host();

    @Override
    public abstract Node node();

    @Override
    public abstract ItemStack[] items();

    @Override
    public abstract int getContainerSize();

    // ----------------------------------------------------------------------- //

    @SuppressWarnings("rawtypes")
    private scala.Option[] _components;

    @SuppressWarnings("rawtypes")
    public scala.Option[] li$cil$oc$common$container$ComponentInventory$$_components() {
        return this._components;
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public void li$cil$oc$common$container$ComponentInventory$$_components_$eq(scala.Option[] _components) {
        this._components = _components;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void li$cil$oc$common$container$ComponentInventory$_setter_$updatingComponents_$eq(scala.collection.mutable.ArrayBuffer updatingComponents) {
        this.updatingComponentsBuffer = updatingComponents;
    }

    public void li$cil$oc$common$container$ComponentInventory$_setter_$isSizeInventoryReady_$eq(boolean isSizeInventoryReady) {
        this.sizeInventoryReady = isSizeInventoryReady;
    }

    @Override
    public scala.Option<ManagedEnvironment>[] componentSlots() {
        return li.cil.oc.common.container.ComponentInventory.super.componentSlots();
    }

    @Override
    public void updateComponents() {
        li.cil.oc.common.container.ComponentInventory.super.updateComponents();
    }

    @Override
    public void connectComponents() {
        li.cil.oc.common.container.ComponentInventory.super.connectComponents();
    }

    @Override
    public void disconnectComponents() {
        li.cil.oc.common.container.ComponentInventory.super.disconnectComponents();
    }

    @Override
    public void saveComponents() {
        li.cil.oc.common.container.ComponentInventory.super.saveComponents();
    }

    @Override
    public boolean isComponentSlot(int slot, ItemStack stack) {
        return li.cil.oc.common.container.ComponentInventory.super.isComponentSlot(slot, stack);
    }

    @Override
    public void connectItemNode(Node node) {
        li.cil.oc.common.container.ComponentInventory.super.connectItemNode(node);
    }

    @Override
    public void load(ManagedEnvironment component, DriverItem driver, ItemStack stack) {
        li.cil.oc.common.container.ComponentInventory.super.load(component, driver, stack);
    }

    @Override
    public void save(ManagedEnvironment component, DriverItem driver, ItemStack stack) {
        li.cil.oc.common.container.ComponentInventory.super.save(component, driver, stack);
    }

    @Override
    public void applyLifecycleState(Object component, Lifecycle.LifecycleState state) {
        li.cil.oc.common.container.ComponentInventory.super.applyLifecycleState(component, state);
    }

    @Override
    public void loadData(DataComponentHolder holder) {
        li.cil.oc.common.container.ComponentInventory.super.loadData(holder);
    }

    @Override
    public void saveData(MutableDataComponentHolder holder) {
        li.cil.oc.common.container.ComponentInventory.super.saveData(holder);
    }

    @Override
    public void onItemAdded(int slot, ItemStack stack) {
        li.cil.oc.common.container.ComponentInventory.super.onItemAdded(slot, stack);
    }

    @Override
    public void onItemRemoved(int slot, ItemStack stack) {
        li.cil.oc.common.container.ComponentInventory.super.onItemRemoved(slot, stack);
    }

    // ----------------------------------------------------------------------- //

    @Override
    public void onConnect(Node node) {}

    @Override
    public void onDisconnect(Node node) {}

    @Override
    public void onMessage(Message message) {}
}