package gml.openstuff.data;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

import java.util.Arrays;

public class PieceData {
    public ItemStack[] items = new ItemStack[32];
    public double energy = 0.0;
    public double maxEnergy = 0.0;

    public PieceData() {
        Arrays.fill(this.items, ItemStack.EMPTY);
    }

    public PieceData(ItemStack stack) {
        this();
        loadData(stack);
    }

    public void loadData(DataComponentHolder holder) {
        Arrays.fill(this.items, ItemStack.EMPTY);
        this.energy = 0.0;
        this.maxEnergy = 0.0;

        ItemContainerContents container = holder.get(DataComponents.CONTAINER);
        if (container != null) {
            NonNullList<ItemStack> list = NonNullList.withSize(this.items.length, ItemStack.EMPTY);
            container.copyInto(list);
            for (int i = 0; i < this.items.length; i++) {
                this.items[i] = list.get(i).copy();
            }
        }

        CustomData customData = holder.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag mainTag = customData.copyTag();

            this.energy = mainTag.getDouble("Energy");
            this.maxEnergy = mainTag.getDouble("MaxEnergy");
        }

    }

    public void saveData(MutableDataComponentHolder holder) {
        ItemContainerContents container = ItemContainerContents.fromItems(Arrays.asList(this.items));
        holder.set(DataComponents.CONTAINER, container);

        CustomData customData = holder.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag mainTag = customData.copyTag();

        mainTag.putDouble("Energy", this.energy);
        mainTag.putDouble("MaxEnergy", this.maxEnergy);

        holder.set(DataComponents.CUSTOM_DATA, CustomData.of(mainTag));
    }
}