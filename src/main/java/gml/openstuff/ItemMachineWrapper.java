package gml.openstuff;

import com.google.common.collect.Iterables;
import gml.openstuff.container.SimpleComponentItemsEnvironment;
import gml.openstuff.data.MachineData;
import gml.openstuff.data.PieceData;
import gml.openstuff.integration.opencomputers.ArmorDriver;
import li.cil.oc.api.UnrecoverablePersistanceException;
import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.MachineHost;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Node;
import li.cil.oc.util.RotationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.NotNull;

import static gml.openstuff.Networking.sendServerState;

public class ItemMachineWrapper extends SimpleComponentItemsEnvironment implements MachineHost, li.cil.oc.api.internal.Tablet {
    public ItemStack stack;
    public LivingEntity holder;

    public String checksum;


    private li.cil.oc.api.machine.Machine machine;

    public MachineData data = new MachineData();

    public boolean isInitialized = false;
    public boolean isDirty = true;
    public int timesChanged = 0;

    // Server side only
    private boolean lastRunning = false;
    public boolean autoSave = true;

    public ItemMachineWrapper(ItemStack _stack, LivingEntity _holder){
        stack = _stack;
        holder = _holder;

        readFromNBT(holder.registryAccess());
        if (!getEnvironmentLevel().isClientSide) {
            li.cil.oc.api.Network.joinNewNetwork(machine.node());
            writeToNBT(holder.registryAccess());
            isInitialized = true;
        } else {
            connectComponents();

            for (var slot : componentSlots()) {
                if (slot instanceof ArmorDriver.Armor piece) {
                    piece.connectComponents();
                }
            }
            isInitialized = true;
        }
    }

    public void readFromNBT(HolderLookup.Provider provider) {
        loadData(stack);
        if (!getEnvironmentLevel().isClientSide) {
            try {
                machine().loadData(stack);
            } catch (UnrecoverablePersistanceException e) {
                OpenStuff.LOGGER.error("Couldn't retrieve machine state !!");
            }
        }
    }

    public void writeToNBT(HolderLookup.Provider provider){
        saveData(stack);
        if (!getEnvironmentLevel().isClientSide) {
            machine().saveData(stack);
        }
    }

    @Override
    public EnvironmentHost host() { return this; }

    public boolean stillValid(@NotNull Player player) {
        return machine() != null && machine().canInteract(player.getName().getString());
    }

    public void setChanged() {
        saveData(stack);
    }

    @Override
    public Machine machine() {
        if (this.machine == null) this.machine = this.getEnvironmentLevel().isClientSide() ? null : li.cil.oc.api.Machine.create(this);
        return this.machine;
    }

    @Override
    public Node node() {
        return this.machine() != null ? this.machine().node() : null;
    }

    @Override
    public ItemStack[] items() {
        return Iterables.toArray(Iterables.concat(holder.getHandSlots(), holder.getArmorAndBodyArmorSlots()), ItemStack.class);
    }

    private static int getIndexForEquipment(EquipmentSlot slot){
        return slot.getIndex(slot.isArmor() ? 2 : 0);
    }

    @Override
    public int getContainerSize() { return this.items().length; }

    @Override
    public Iterable<ItemStack> internalComponents() {
        PieceData chest = new PieceData(stack);
        return chest.items.stream().toList();
    }

    @Override
    public int componentSlot(String address) {
        return -1;
    }

    @Override
    public void onConnect(Node node){
        if (node == this.node()) {
            connectComponents();

            /*if(!isInitialized){
                sendServerState((ServerPlayer) player, stack, machine().isRunning());
                isInitialized = true;
            }*/
        }
    }

    @Override
    public void connectItemNode(Node node){
        super.connectItemNode(node);
        if (node != null) {
            if(node.host() instanceof li.cil.oc.api.internal.TextBuffer buffer) {
                for(Node n : machine.node().reachableNodes()){
                    if(n.host() instanceof li.cil.oc.api.internal.Keyboard){
                        buffer.node().connect(n);
                    }
                }
            } else if (node.host() instanceof li.cil.oc.api.internal.Keyboard keyboard){
                for(Node n : machine.node().reachableNodes()){
                    if(n.host() instanceof li.cil.oc.api.internal.TextBuffer){
                        keyboard.node().connect(n);
                    }
                }
            }
        }
    }

    @Override
    public void onDisconnect(Node node){
        if (node == this.node()) {
            disconnectComponents();
        }
    }

    @Override
    public void onMachineConnect(Node node) { onConnect(node); }
    @Override
    public void onMachineDisconnect(Node node) { onDisconnect(node);}

    // ----------------------------------------------------------------------- //

    @Override
    public Level getEnvironmentLevel() { return holder.level(); }
    @Override
    public double xPosition() { return holder.getX(); }
    @Override
    public double yPosition() { return holder.getY() + holder.getEyeHeight(); }
    @Override
    public double zPosition() { return holder.getZ(); }
    @Override
    public void markChanged() {}

    // ----------------------------------------------------------------------- //

    public void  loadData(DataComponentHolder tag_holder){
        data.loadData(tag_holder, holder.registryAccess());
    }

    public void  saveData(MutableDataComponentHolder tag_holder){
        saveComponents();
        data.saveData(tag_holder, holder.registryAccess());
    }

    // ----------------------------------------------------------------------- //

    public void update(){
        Connector connector = ((Connector)this.machine().node());

        connector.changeBuffer(Double.POSITIVE_INFINITY);
        machine.update();
        updateComponents();

        data.isRunning = machine.isRunning();

        if (lastRunning != machine.isRunning()) {
            lastRunning = machine.isRunning();
            setChanged();

            if (this.holder instanceof ServerPlayer player){
                sendServerState(player, stack, machine.isRunning());
            }

            if (machine.isRunning()) {
                for(Node node : machine.node().reachableNodes()){
                    if(node.host() instanceof TextBuffer buffer){
                        buffer.setPowerState(true);
                        break;
                    }
                }
            }
        }
    }


    // --------------------------------------------------------- //

    public void interact(Level level, Player player){
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                if (player instanceof ServerPlayer){
                    // TODO: make a custon GUI
                    /*player.openMenu(this, buff -> {
                        ItemStack.STREAM_CODEC.encode(buff, this.stack);
                        buff.writeVarInt(this.getContainerSize());
                        buff.writeUtf(this.containerSlotType(), 32);
                        buff.writeVarInt(this.containerSlotTier());
                    });*/
                }
            }
        }
        else {
            if (!level.isClientSide) {
                machine().start();
                String msg = machine().lastError();
                if(msg != null) {
                    // TODO: fix translation.
                    //player.sendSystemMessage(Component.translatable("gui.Analyzer.LastError", Component.translatable(msg)));
                    player.sendSystemMessage(Component.translatable(msg));
                }
                sendServerState((ServerPlayer) player, stack, machine().isRunning());
            }
            else {
                tryOpenArmorScreen();
            }
        }
    }

    private void tryOpenArmorScreen() {
        for (var slot : this.componentSlots()) {
            if (slot instanceof TextBuffer buffer) {
                Minecraft.getInstance().pushGuiLayer(new li.cil.oc.client.gui.Screen(buffer, true, () -> true, buffer::isRenderingEnabled));
                return; // Stops execution immediately once found
            } else

            if (slot instanceof ArmorDriver.Armor piece) {
                for (var subSlot : piece.componentSlots()) {
                    if (subSlot instanceof TextBuffer buffer) {
                        Minecraft.getInstance().pushGuiLayer(new li.cil.oc.client.gui.Screen(buffer, true, () -> true, buffer::isRenderingEnabled));
                        return; // Stops execution immediately once found
                    }
                }
            }
        }
    }

    @Override
    public Direction facing() {
        return RotationHelper.fromYaw(holder.getYRot());
    }

    @Override
    public Direction toLocal(Direction value) {
        return RotationHelper.toLocal(Direction.NORTH, this.facing(), value);
    }

    @Override
    public Direction toGlobal(Direction value) {
        return RotationHelper.toGlobal(Direction.NORTH, this.facing(), value);
    }

    @Override
    public Player player() {
        return (Player) this.holder;
    }
}
