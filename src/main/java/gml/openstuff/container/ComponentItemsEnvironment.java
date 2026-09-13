package gml.openstuff.container;

import gml.openstuff.OpenStuff;
import li.cil.oc.api.Driver;
import li.cil.oc.api.UnrecoverablePersistanceException;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Memory;
import li.cil.oc.api.network.*;
import li.cil.oc.api.util.Lifecycle;
import li.cil.oc.common.datacomponents.OCComponents;
import li.cil.oc.integration.opencomputers.DriverTablet;
import li.cil.oc.integration.opencomputers.DriverTablet$;
import li.cil.oc.internal.scalalib.Function1;
import li.cil.oc.internal.scalalib.runtime.AbstractFunction1;
import li.cil.oc.internal.scalalib.runtime.BoxedUnit;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// TODO: burk.....
public interface ComponentItemsEnvironment extends Environment{

    List<ManagedEnvironment> updatingComponents();

    ManagedEnvironment[] componentSlots();

    // ----------------------------------------------------------------------- //

    EnvironmentHost host();

    Node node();

    ItemStack[] items();

    // ----------------------------------------------------------------------- //


    void onConnect(Node node);

    void onDisconnect(Node node);

    void onMessage(Message message);

    // ----------------------------------------------------------------------- //

    default int getContainerSize(){
        return items().length;
    }

    default void updateComponents() {
        if (!this.updatingComponents().isEmpty()) {
            int i = 0;
            // ArrayBuffer.foreach caches the size for performance reasons, but that
            // will cause issues if the list changed during iteration (e.g. because
            // a component removed itself / another component, such as the self-destruct card from Computronics). Also, this list will generally be
            // quite short, so it won't have any noticeable impact, anyway.
            while (i < this.updatingComponents().size()) {
                this.updatingComponents().get(i).update();
                i += 1;
            }
        }
    }

    default void connectComponents() {
        // Managed component environments are allowed to query their host's world
        // during construction/load (e.g. TextBuffer, Trading Upgrade). Block
        // entities are deserialized before Minecraft attaches their Level, so
        // defer component construction until the host is actually live.
        if (this.host().getEnvironmentLevel() == null) return;


        // Make sure our node is connected.
        li.cil.oc.api.Network.joinNewNetwork(this.node());

        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (slot >= 0 && slot < this.componentSlots().length) {
                ItemStack stack = this.items()[slot];
                if (!stack.isEmpty() && this.componentSlots()[slot] == null && isComponentSlot(slot, stack)) {
                    DriverItem driver = Driver.driverFor(stack);
                    if (driver != null) {
                        ManagedEnvironment component = driver.createEnvironment(stack, this.host());
                        if (component != null) {
                            applyLifecycleState(component, Lifecycle.LifecycleState.Constructing);
                            try {
                                // Restore node identity first. Old OC treated oc:node as part
                                // of the component inventory contract; in the Data Component
                                // port the equivalent is OCComponents.ADDRESS on the stack.
                                // Do this explicitly so a component override cannot
                                // accidentally regenerate its address by omitting super.
                                load(component, driver, stack);
                            } catch (Throwable e) {
                                OpenStuff.LOGGER.warn(
                                        String.format("An item component of type '%s' (provided by driver '%s') threw an error while loading.",
                                                component.getClass().getName(),
                                                driver.getClass().getName()),
                                        e
                                );
                            }
                            if (component.canUpdate()) {
                                assert !this.updatingComponents().contains(component);
                                this.updatingComponents().add(component);
                            }
                            this.componentSlots()[slot] = component;


                            applyLifecycleState(component, Lifecycle.LifecycleState.Initializing);
                            connectItemNode(component.node());
                            applyLifecycleState(component, Lifecycle.LifecycleState.Initialized);
                        } else {
                            this.componentSlots()[slot] = null;
                        }
                    } else {
                        this.componentSlots()[slot] = null;
                    }
                } else if (!stack.isEmpty() && this.componentSlots()[slot] != null) {
                    ManagedEnvironment env = this.componentSlots()[slot];
                    try {
                        env.loadData(stack);
                        env.node().loadData(stack);
                    } catch (UnrecoverablePersistanceException e) {
                        OpenStuff.LOGGER.warn("can't reload a component !!");
                    }
                } else if (stack.isEmpty() && this.componentSlots()[slot] != null) {
                    ManagedEnvironment component = this.componentSlots()[slot];
                    applyLifecycleState(component, Lifecycle.LifecycleState.Disposing);
                    if (component.node() != null) component.node().remove();
                    applyLifecycleState(component, Lifecycle.LifecycleState.Disposed);
                    this.componentSlots()[slot] = null;
                }
            }
        }
    }

    default void disconnectComponents() {
        for(ManagedEnvironment component : this.componentSlots()){
            if(component != null){
                applyLifecycleState(component, Lifecycle.LifecycleState.Disposing);
                if (component.node() != null) component.node().remove();
                applyLifecycleState(component, Lifecycle.LifecycleState.Disposed);
            }
        }
    }

    default void saveComponents() {
        for (int slot = 0;  slot < this.getContainerSize(); slot++) {
            ItemStack stack = this.items()[slot];
            if (!stack.isEmpty()) {
                if (slot >= this.componentSlots().length) {
                    // isSizeInventoryReady was added to resolve issues where an inventory was used before its
                    // nbt data had been parsed. See https://github.com/MightyPirates/OpenComputers/issues/2522
                    // If this error is hit again, perhaps another subtype needs to handle nbt loading like Case does
                    OpenStuff.LOGGER.error("ComponentItemsEnvironment components length {} does not accommodate inventory size {}", this.componentSlots().length, this.getContainerSize());
                    return;
                } else {
                    ManagedEnvironment component = this.componentSlots()[slot];
                    if(component != null){
                        save(component, Driver.driverFor(stack), stack);
                    }
                }
            }
        }
    }

    default boolean isComponentSlot(int slot, ItemStack stack) {
        return true;
    }

    default void connectItemNode(Node node) {
        if (this.node() != null && node != null) {
            this.node().connect(node);
        }
    }

    default void load(ManagedEnvironment component, DriverItem driver, ItemStack stack) {
        Function1<ItemStack, BoxedUnit> loadFrom = new AbstractFunction1<>() {
            @Override
            public BoxedUnit apply(ItemStack persistenceStack) {
                // Restore node identity first. Old OC treated oc:node as part of the
                // component inventory contract; in the Data Component port the
                // equivalent is OCComponents.ADDRESS on the persistence stack.
                try {
                    if (component.node() != null) {
                        component.node().loadData(persistenceStack);
                    }
                    component.loadData(persistenceStack);
                }catch (li.cil.oc.api.UnrecoverablePersistanceException e){
                    OpenStuff.LOGGER.error("failled to load component !");
                }

                return BoxedUnit.UNIT;
            }
        };

        if (driver == DriverTablet$.MODULE$) {
            // A tablet exposes its embedded filesystem as the component. Loading
            // from the outer tablet stack would make the filesystem consume the
            // tablet's own data components (including its battery state).
            DriverTablet.withFileSystemStack(stack, loadFrom);

            // The embedded drive is normally a neighbor-visible component inside
            // the tablet. In a charger it is a proxy component for the charger's
            // whole network, so restore the driver's visibility override after
            // loading the drive's persisted component state.
            if (component.node() instanceof Component nodeComponent) {
                nodeComponent.setVisibility(Visibility.Network);
            }
        } else {
            loadFrom.apply(stack);
        }
    }

    default void save(ManagedEnvironment component, DriverItem driver, ItemStack stack) {
        try {
            Function1<ItemStack, BoxedUnit> saveTo = new AbstractFunction1<>() {
                @Override
                public BoxedUnit apply(ItemStack persistenceStack) {
                    if (driver instanceof Memory) {
                        // RAM has no persistent per-item state. Its environment gets a temporary
                        // network node while installed, but persisting that node address makes
                        // otherwise identical RAM sticks differ as ItemStacks and prevents them
                        // from stacking again after use. Also clean up addresses written by older
                        // builds of the Data Component port.
                        persistenceStack.remove(OCComponents.ADDRESS().get());
                        persistenceStack.remove(OCComponents.VISIBILITY().get());
                    } else {
                        component.saveData(persistenceStack);

                        // Enforce node persistence at the inventory boundary. This is the modern
                        // equivalent of old OC's per-component oc:node tag: if an environment
                        // forgot to call super.saveData(), its address must still survive.
                        if (component.node() != null) {
                            component.node().saveData(persistenceStack);

                            String persisted = persistenceStack.get(OCComponents.ADDRESS().get());
                            if (component.node().address() != null &&
                                    (persisted == null || !persisted.equals(component.node().address()))) {
                                OpenStuff.LOGGER.error(
                                        String.format("Failed to persist component node address for %s: node=%s, stack=%s",
                                                component.getClass().getName(),
                                                component.node().address(),
                                                persisted
                                        )
                                );
                            }
                        }
                    }
                    return BoxedUnit.UNIT;
                }
            };

            if (driver == DriverTablet$.MODULE$) {
                // The environment represented by a tablet in a component slot is its
                // embedded filesystem. Persist it into that nested disk ItemStack and
                // then write the updated contents back to the tablet. Never serialize
                // the filesystem node directly onto the outer tablet stack, because
                // connector persistence uses OCComponents.CHARGE and would overwrite
                // the tablet's battery.
                DriverTablet.withFileSystemStack(stack, saveTo);
            } else {
                saveTo.apply(stack);
            }
        } catch (Throwable e) {
            OpenStuff.LOGGER.warn("An item component of type '{}' (provided by driver '{}') threw an error while saving.",
                            component.getClass().getName(),
                            driver.getClass().getName(),
                    e
            );
        }
    }

    default void applyLifecycleState(Object component, Lifecycle.LifecycleState state) {
        if(component instanceof Lifecycle lifecycle){
            lifecycle.onLifecycleStateChange(state);
        }
    }

}