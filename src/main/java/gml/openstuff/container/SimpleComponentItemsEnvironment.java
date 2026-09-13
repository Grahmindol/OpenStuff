package gml.openstuff.container;

import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleComponentItemsEnvironment implements ComponentItemsEnvironment{
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

    public void onConnect(Node node) {};

    public void onDisconnect(Node node) {}

    public void onMessage(Message message) {}
}
