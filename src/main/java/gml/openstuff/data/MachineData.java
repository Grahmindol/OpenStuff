package gml.openstuff.data;

import gml.openstuff.integration.opencomputers.ArmorHost;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Container;
import li.cil.oc.api.driver.item.Slot;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

public class MachineData {

    public boolean isRunning = false;
    public ItemStack container = ItemStack.EMPTY;

    public MachineData() {
    }

    public MachineData(ItemStack stack, HolderLookup.Provider registries) {
        this();
        loadData(stack, registries);
    }

    public void loadData(DataComponentHolder holder, HolderLookup.Provider registries) {
        this.isRunning = false;
        this.container = ItemStack.EMPTY;

        CustomData customData = holder.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag mainTag = customData.copyTag();

            if (mainTag.contains("IsRunning", Tag.TAG_BYTE)) {
                this.isRunning = mainTag.getBoolean("IsRunning");
            }

            if (mainTag.contains("Container", Tag.TAG_COMPOUND)) {
                this.container = ItemStack.parseOptional(registries, mainTag.getCompound("Container"));
            }
        }
    }

    public void saveData(MutableDataComponentHolder holder, HolderLookup.Provider registries) {
        CustomData customData = holder.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag mainTag = customData.copyTag();

        mainTag.putBoolean("IsRunning", this.isRunning);

        if (!this.container.isEmpty()) {
            CompoundTag containerTag = new CompoundTag();
            mainTag.put("Container", this.container.save(registries, containerTag));
        } else {
            mainTag.remove("Container");
        }

        holder.set(DataComponents.CUSTOM_DATA, CustomData.of(mainTag));
    }

    public String containerSlotType(){
        if (this.container.isEmpty()) return Slot.None;
        DriverItem driver = Driver.driverFor(this.container, ArmorHost.class);
        if( driver instanceof Container cont)
            return cont.providedSlot(this.container);
        return Slot.None;
    }

    public int containerSlotTier() {
        if (this.container.isEmpty()) return -1;
        DriverItem driver = Driver.driverFor(this.container, ArmorHost.class);
        if( driver instanceof Container cont)
            return cont.providedTier(this.container);
        return -1;
    }
}