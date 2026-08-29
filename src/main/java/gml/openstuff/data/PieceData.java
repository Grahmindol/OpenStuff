package gml.openstuff.data;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

import java.util.Arrays;

public class PieceData {
    public ItemStack[] items = new ItemStack[32];

    public PieceData() {
        Arrays.fill(this.items, ItemStack.EMPTY);
    }

    public PieceData(ItemStack stack) {
        this();
        loadData(stack);
    }

    public void loadData(DataComponentHolder holder) {
        Arrays.fill(this.items, ItemStack.EMPTY);

        ItemContainerContents container = holder.get(DataComponents.CONTAINER);
        if (container != null) {
            NonNullList<ItemStack> list = NonNullList.withSize(this.items.length, ItemStack.EMPTY);
            container.copyInto(list);
            for (int i = 0; i < this.items.length; i++) {
                this.items[i] = list.get(i).copy();
            }
        }
    }

    public void saveData(MutableDataComponentHolder holder) {
        ItemContainerContents container = ItemContainerContents.fromItems(Arrays.asList(this.items));
        holder.set(DataComponents.CONTAINER, container);
    }
}