package gml.openstuff.container;


import gml.openstuff.OpenStuff;
import gml.openstuff.data.MachineData;
import gml.openstuff.integration.opencomputers.ArmorHost;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.client.Textures;
import li.cil.oc.common.menu.AbstractMenu;
import li.cil.oc.common.menu.ComponentSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class CustomArmorComponentSlot extends ComponentSlot {

    private final Player owner;
    private final ArmorPieceContainer ctn;
    private final Inventory playerInventory;

    public CustomArmorComponentSlot(ArmorPieceContainer ctn, int index, int x, int y,
                                    Inventory playerInventory, Player owner) {
        super(ctn, index, x, y, ArmorHost.class);
        this.ctn = ctn;
        this.playerInventory = playerInventory;
        this.owner = owner;
    }

    @Override
    public AbstractMenu agentContainer() {
        return new ArmorAbstractMenu(this.playerInventory, this.ctn);
    }

    @Override
    public String slot() {
        if (!isActive()) return "none";
        MachineData data = new MachineData(owner.getItemBySlot(EquipmentSlot.CHEST), owner.level().registryAccess());
        return data.containerSlotType();
    }

    @Override
    public int tier() {
        if (!isActive()) return -1;
        MachineData data = new MachineData(owner.getItemBySlot(EquipmentSlot.CHEST), owner.level().registryAccess());
        return data.containerSlotTier();
    }

    @Override
    public ResourceLocation tierIcon() {
        return Textures.Icons$.MODULE$.get(this.tier());
    }

    @Override
    public int getMaxStackSize() {
        return switch (this.slot()) {
            case "tool", "any", "filtered" -> super.getMaxStackSize();
            case "none" -> 0;
            default -> 1;
        };
    }

    @Override
    public boolean isActive() {
        return owner.getItemBySlot(EquipmentSlot.CHEST).is(OpenStuff.OPEN_CHEST);
    }

    // Helper static class to avoid nested anonymous inner classes inside AbstractMenu
    private static class ArmorAbstractMenu extends AbstractMenu {
        public ArmorAbstractMenu(Inventory playerInventory, ArmorPieceContainer ctn) {
            super(null, 0, playerInventory, ctn);
        }

        @Override
        public Class<? extends EnvironmentHost> getHostClass() {
            return ArmorHost.class;
        }
    }
}
