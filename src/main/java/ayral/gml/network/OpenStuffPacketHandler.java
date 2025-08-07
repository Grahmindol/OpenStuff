package ayral.gml.network;

import ayral.gml.item.OpenArmorItem;
import li.cil.oc.common.item.Tablet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class OpenStuffPacketHandler {
    public static void handleOpenTabletGui(ServerPlayerEntity player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.isEmpty() || !(chestStack.getItem() instanceof OpenArmorItem)) return;

        CompoundNBT tag = chestStack.getOrCreateTag();
        if (!tag.contains("Tablet")) return;

        ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
        if (!(tabletStack.getItem() instanceof Tablet)) return;

        tabletStack.getItem().releaseUsing(tabletStack, player.level, player, 72000);
    }

}