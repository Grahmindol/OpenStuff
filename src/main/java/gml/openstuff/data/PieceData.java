package gml.openstuff.data;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

import java.util.Arrays;

public class PieceData {
    public static final int SIZE = 32;
    public NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    public double energy = 0.0;
    public double maxEnergy = 0.0;

    public PieceData() {
        // NonNullList.withSize already pre-fills slots with ItemStack.EMPTY
    }

    public PieceData(ItemStack stack) {
        this();
        loadData(stack);
    }

    public PieceData(ItemStack[] inv, double energy, double maxEnergy){
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        for (int i = 0; i < SIZE; i++) {
            if (i < inv.length && inv[i] != null && !inv[i].isEmpty()) {
                this.items.set(i, inv[i]);
            } else {
                this.items.set(i, ItemStack.EMPTY);
            }
        }
    }

    public void loadData(DataComponentHolder holder) {
        // Reset list contents while preserving the NonNullList instance reference
        for (int i = 0; i < SIZE; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        this.energy = 0.0;
        this.maxEnergy = 0.0;

        ItemContainerContents container = holder.get(DataComponents.CONTAINER);
        if (container != null) {
            // Copy contents directly into our fixed NonNullList instance
            container.copyInto(this.items);
        }

        CustomData customData = holder.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag mainTag = customData.copyTag();
            this.energy = mainTag.getDouble("Energy");
            this.maxEnergy = mainTag.getDouble("MaxEnergy");
        }
    }

    public void saveData(MutableDataComponentHolder holder) {
        // ItemContainerContents natively accepts a NonNullList<ItemStack> directly
        ItemContainerContents container = ItemContainerContents.fromItems(this.items);
        holder.set(DataComponents.CONTAINER, container);

        CustomData customData = holder.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag mainTag = customData.copyTag();

        mainTag.putDouble("Energy", this.energy);
        mainTag.putDouble("MaxEnergy", this.maxEnergy);

        holder.set(DataComponents.CUSTOM_DATA, CustomData.of(mainTag));
    }
}