package ayral.gml;

import ayral.gml.item.OpenStuffArmorItem;
import li.cil.oc.common.item.Tablet;
import li.cil.oc.common.item.TabletWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpenStuffMod.MOD_ID)
public class OpenStuffEvents {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        ItemStack oldItem = event.getFrom();
        ItemStack newItem = event.getTo();
        if (!(oldItem.getItem() instanceof OpenStuffArmorItem)  || oldItem.sameItem(newItem)) return;
        oldItem.getOrCreateTag().putBoolean("is_armor_running", false);

        ItemStack chestItem = player.getItemBySlot(EquipmentSlotType.CHEST);
        if ((chestItem.getItem() instanceof OpenStuffArmorItem)) {
            CompoundNBT tag = chestItem.getOrCreateTag();
            tag.putBoolean("is_armor_running", false);
            if (!tag.contains("Tablet")) return;
            ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
            TabletWrapper tablet = Tablet.get(tabletStack, player);
            tablet.machine().stop();
        }

        if (((OpenStuffArmorItem) oldItem.getItem()).getSlot() == EquipmentSlotType.CHEST){
            CompoundNBT tag = oldItem.getOrCreateTag();
            if (!tag.contains("Tablet")) return;
            ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
            TabletWrapper tablet = Tablet.get(tabletStack, player);
            tablet.machine().stop();
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        ItemStack chest = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (!(chest.getItem() instanceof OpenStuffArmorItem)) return;

        CompoundNBT tag = chest.getOrCreateTag();
        if (!tag.contains("Tablet")) return;

        ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
        TabletWrapper tablet = Tablet.get(tabletStack, player);

        String addr = ((OpenStuffArmorItem) chest.getItem()).getArmorComponentAddress();

        if (tablet != null && tablet.machine() != null) {
            tablet.machine().signal("armor_hurt",addr, event.getSource().getMsgId());
        }
    }
}

