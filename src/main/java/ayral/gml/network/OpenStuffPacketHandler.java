package ayral.gml.network;

import ayral.gml.item.OpenStuffArmorItem;
import li.cil.oc.common.container.ContainerTypes;
import li.cil.oc.common.item.Tablet;
import li.cil.oc.common.item.TabletWrapper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class OpenStuffPacketHandler {
    public static void handleOpenTabletGui(ServerPlayerEntity player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.isEmpty() || !(chestStack.getItem() instanceof OpenStuffArmorItem)) return;

        CompoundNBT tag = chestStack.getOrCreateTag();
        if (!tag.contains("Tablet")) return;

        ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
        if (!(tabletStack.getItem() instanceof Tablet)) return;

        System.out.println(player.level.isClientSide);
        tabletStack.getItem().releaseUsing(tabletStack, player.level, player, 72000);

        //TabletWrapper tablet = Tablet.get(tabletStack, player);
        //ContainerTypes.openTabletGui(player, tablet);
    }

}