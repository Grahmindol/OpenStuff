package gml.openstuff.container;

import gml.openstuff.data.PieceData;
import gml.openstuff.item.OpenArmorPiece;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public class ArmorPieceContainer implements Container {
    public final Player holder;
    public final EquipmentSlot slot;

    public ArmorPieceContainer(Player holder, EquipmentSlot slot){
        this.holder = holder;
        this.slot = slot;
    }

    private PieceData getData(){
        ItemStack stack = holder.getItemBySlot(slot);
        if(!(stack.getItem() instanceof OpenArmorPiece)) return new PieceData();
        return new PieceData(stack);
    }

    private void setData(PieceData data){
        ItemStack stack = holder.getItemBySlot(slot);
        if(!(stack.getItem() instanceof OpenArmorPiece)) return;
        data.saveData(stack);
    }

    public @NotNull ItemStack getItem(int index) {
        PieceData data = this.getData();
        return index >= 0 && index < data.items.size() ? (ItemStack)data.items.get(index) : ItemStack.EMPTY;
    }

    public ItemStack removeItem(int index, int count) {
        PieceData data = this.getData();

        ItemStack itemstack = ContainerHelper.removeItem(data.items, index, count);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        this.setData(data);
        return itemstack;
    }

    public ItemStack removeItemNoUpdate(int index) {
        PieceData data = this.getData();

        ItemStack itemstack = (ItemStack)data.items.get(index);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            data.items.set(index, ItemStack.EMPTY);
            this.setData(data);
            return itemstack;
        }
    }

    public void setItem(int index, ItemStack stack) {
        PieceData data = this.getData();
        data.items.set(index, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setData(data);
        this.setChanged();
    }

    public int getContainerSize() {
        PieceData data = this.getData();
        return data.items.size();
    }

    public boolean isEmpty() {
        PieceData data = this.getData();
        for(ItemStack itemstack : data.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void setChanged() {
        holder.getInventory().setChanged();
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void clearContent() {
        PieceData data = this.getData();
        data.items.clear();
        this.setData(data);

        this.setChanged();
    }

    public String toString() {
        PieceData data = this.getData();
        return data.items.stream().filter((p_19194_) -> !p_19194_.isEmpty()).toList().toString();
    }


    public NonNullList<ItemStack> getItems() {
        PieceData data = this.getData();
        return data.items;
    }
}