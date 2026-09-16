package gml.openstuff.container;

import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;

public interface InventoryMenuAccess {
    DataSlot openstuff$getIsRunningDataSlot();
    Slot openstuff$getArmorContainerSlot();
}
