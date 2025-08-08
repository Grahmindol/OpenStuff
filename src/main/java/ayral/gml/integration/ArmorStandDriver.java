package ayral.gml.integration;

import ayral.gml.integration.component.DriverArmor;
import ayral.gml.item.OpenArmorItem;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.common.item.data.TabletData;
import li.cil.oc.common.tileentity.Adapter;
import li.cil.oc.integration.opencomputers.DriverEEPROM;
import li.cil.oc.integration.opencomputers.DriverFileSystem;
import li.cil.oc.integration.opencomputers.DriverTablet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

public class ArmorStandDriver implements DriverBlock {

    public static void updateNeighbors(ArmorStandEntity stand) { // to call after armor get changed
        World world = stand.level;
        BlockPos pos = stand.blockPosition(); // ou un BlockPos que tu veux

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Mise à jour des voisins dans les 6 directions
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            world.neighborChanged(neighborPos, block, pos);
            world.updateNeighborsAt(neighborPos, block);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) { // called befor armor stand loss the armor
        if (!(event.getTarget() instanceof ArmorStandEntity)) return;
        ArmorStandEntity stand = (ArmorStandEntity) event.getTarget();
        World world = stand.level;

        BlockPos pos = stand.blockPosition(); // ou un BlockPos que tu veux

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Mise à jour des voisins dans les 6 directions
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            TileEntity n_te = world.getBlockEntity(neighborPos);
            if (!(n_te instanceof Adapter)) break;
            ((Adapter) n_te).saveForServer(new CompoundNBT());
        }
    }

    @Override
    public boolean worksWith(World world, BlockPos blockPos, Direction direction) {
        AxisAlignedBB aabb = new AxisAlignedBB(blockPos);
        List<ArmorStandEntity> stands = world.getEntitiesOfClass(ArmorStandEntity.class, aabb);
        if(stands.isEmpty()) return false;
        ArmorStandEntity stand = stands.stream().findFirst().get();
        return OpenArmorItem.isWearingFullSet(stand);
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos blockPos, Direction direction) {
        AxisAlignedBB aabb = new AxisAlignedBB(blockPos);
        List<ArmorStandEntity> stands = world.getEntitiesOfClass(ArmorStandEntity.class, aabb);

        ArmorStandEntity stand = stands.stream().findFirst().get();
        return new Environment(stand);
    }

    public static final class Environment extends AbstractManagedEnvironment {
        ArmorStandEntity stand;
        ManagedEnvironment fsenv;
        ManagedEnvironment armorenv;
        ManagedEnvironment eepromenv;

        public Environment(ArmorStandEntity stand) {
            this.stand = stand;

            ItemStack chest = stand.getItemBySlot(EquipmentSlotType.CHEST);
            CompoundNBT tag = chest.getOrCreateTag();
            OpenArmorItem.ensureTablet(tag);
            ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
            TabletData data = new TabletData(tabletStack);

            EnvironmentHost host = new ArmorHost(stand);
            // Crée le filesystem associé
            ItemStack fsStack = Arrays.stream(data.items())
                    .filter(fs -> !fs.isEmpty() && DriverFileSystem.worksWith(fs))
                    .findFirst().orElse(ItemStack.EMPTY);
            fsenv = DriverFileSystem.createEnvironment(fsStack, host);

            ItemStack eepromStack = Arrays.stream(data.items())
                    .filter(eeprom -> !eeprom.isEmpty() && DriverEEPROM.worksWith(eeprom))
                    .findFirst().orElse(ItemStack.EMPTY);
            eepromenv = DriverEEPROM.createEnvironment(eepromStack, host);

            armorenv = new DriverArmor.Environment(host);

            setNode(Network.newNode(this, Visibility.None).withConnector().create());
        }

        @Override
        public void onConnect(final Node node) {
            if (node.host() instanceof Context) {
                node.connect(fsenv.node());
                node.connect(eepromenv.node());
                node.connect(armorenv.node());
            }
        }

        @Override
        public void onDisconnect(final Node node) {
            if (node.host() instanceof Context) {
                node.disconnect(fsenv.node());
                node.disconnect(eepromenv.node());
                node.disconnect(armorenv.node());
            } else if (node == this.node()) {
                fsenv.node().remove();
                eepromenv.node().remove();
                armorenv.node().remove();
            }
        }

        @Override
        public void loadData(CompoundNBT nbt) {
            ItemStack chest = stand.getItemBySlot(EquipmentSlotType.CHEST);
            CompoundNBT tag = chest.getOrCreateTag();
            OpenArmorItem.ensureTablet(tag);
            ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));

            fsenv.loadData(DriverTablet.dataTag(tabletStack));

            TabletData data = new TabletData(tabletStack);
            eepromenv.loadData(Arrays.stream(data.items())
                    .filter(eeprom -> !eeprom.isEmpty() && DriverEEPROM.worksWith(eeprom))
                    .findFirst().orElse(ItemStack.EMPTY).getOrCreateTag());


            armorenv.loadData(data.items()[30].getOrCreateTag());

            super.loadData(nbt);
        }

        @Override
        public void saveData(CompoundNBT nbt) {
            ItemStack chest = stand.getItemBySlot(EquipmentSlotType.CHEST);
            if (!(chest.getItem() instanceof OpenArmorItem)) return; // no more armor....

            CompoundNBT tag = chest.getOrCreateTag();
            OpenArmorItem.ensureTablet(tag);
            ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));

            fsenv.saveData(DriverTablet.dataTag(tabletStack));

            TabletData data = new TabletData(tabletStack);

            eepromenv.saveData(Arrays.stream(data.items())
                    .filter(eeprom -> !eeprom.isEmpty() && DriverEEPROM.worksWith(eeprom))
                    .findFirst().orElse(ItemStack.EMPTY).getOrCreateTag());

            armorenv.saveData(data.items()[30].getOrCreateTag());

            tag.put("Tablet", data.createItemStack().save(new CompoundNBT()));
            super.saveData(nbt);
        }
    }
}
