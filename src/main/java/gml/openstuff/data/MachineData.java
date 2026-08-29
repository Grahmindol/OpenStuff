package gml.openstuff.data;

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
    public double energy = 0.0;
    public double maxEnergy = 0.0;
    public ItemStack container = ItemStack.EMPTY;

    public MachineData() {
    }

    public MachineData(ItemStack stack, HolderLookup.Provider registries) {
        this();
        loadData(stack, registries);
    }

    public void loadData(DataComponentHolder holder, HolderLookup.Provider registries) {
        this.isRunning = false;
        this.energy = 0.0;
        this.maxEnergy = 0.0;
        this.container = ItemStack.EMPTY;

        CustomData customData = holder.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag mainTag = customData.copyTag();

            if (mainTag.contains("IsRunning", Tag.TAG_BYTE)) {
                this.isRunning = mainTag.getBoolean("IsRunning");
            }
            this.energy = mainTag.getDouble("Energy");
            this.maxEnergy = mainTag.getDouble("MaxEnergy");

            if (mainTag.contains("Container", Tag.TAG_COMPOUND)) {
                this.container = ItemStack.parseOptional(registries, mainTag.getCompound("Container"));
            }
        }
    }

    public void saveData(MutableDataComponentHolder holder, HolderLookup.Provider registries) {
        CustomData customData = holder.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag mainTag = customData.copyTag();

        mainTag.putBoolean("IsRunning", this.isRunning);
        mainTag.putDouble("Energy", this.energy);
        mainTag.putDouble("MaxEnergy", this.maxEnergy);

        if (!this.container.isEmpty()) {
            CompoundTag containerTag = new CompoundTag();
            mainTag.put("Container", this.container.save(registries, containerTag));
        } else {
            mainTag.remove("Container");
        }

        holder.set(DataComponents.CUSTOM_DATA, CustomData.of(mainTag));
    }
}