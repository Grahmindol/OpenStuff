package gml.openstuff.container;

import gml.openstuff.OpenStuff;
import li.cil.oc.api.UnrecoverablePersistanceException;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.core.component.DataComponentHolder;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class ManagedComponentItemsEnvironment extends AbstractManagedEnvironment implements ComponentItemsEnvironment {
    private boolean sizeInventoryReady = true;
    private List<ManagedEnvironment> updatingComponentsBuffer = new ArrayList<>();
    private ManagedEnvironment[] _components;


    @Override
    public List<ManagedEnvironment> updatingComponents() {
        return this.updatingComponentsBuffer;
    }

    public ManagedEnvironment[] componentSlots() {
        if (_components == null && this.sizeInventoryReady) {
            ManagedEnvironment[] array = new ManagedEnvironment[getContainerSize()];
            java.util.Arrays.fill(array, null);
            _components = array;
        }

        return _components != null ? _components :  new ManagedEnvironment[0];
    }

    @Override
    public void onConnect(final Node node) {
        super.onConnect(node);
        if(node == this.node()){
            connectComponents();
        }
    }

    @Override
    public void onDisconnect(final Node node) {
        super.onDisconnect(node);
        if(node == this.node()){
            disconnectComponents();
        }
    }


    // ----------------------------------------------------------------------- //

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void update() {
        updateComponents();
    }

    @Override
    public abstract EnvironmentHost host();


    // ----------------------------------------------------------------------- //

    @Override
    public void loadData(DataComponentHolder holder) {
        try {
            super.loadData(holder);
        } catch (UnrecoverablePersistanceException e) {
            OpenStuff.LOGGER.error("Unrecoverable Persistance Exception !");
        }
    }

    @Override
    public void saveData(MutableDataComponentHolder holder) {
        super.saveData(holder);
    }
}
