package ayral.gml;

import ayral.gml.item.OpenStuffArmorItem;
import li.cil.oc.common.item.Tablet;
import li.cil.oc.common.item.TabletWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpenStuffMod.MOD_ID)
public class OpenStuffEvents {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        ItemStack oldItem = event.getFrom();
        ItemStack newItem = event.getTo();
        // Si l'ancien plastron est un OpenStuffArmor
        if (oldItem.getItem() instanceof OpenStuffArmorItem && !oldItem.sameItem(newItem)) {
            CompoundNBT tag = oldItem.getOrCreateTag();
            if (!tag.contains("Tablet")) return;
            ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
            TabletWrapper tablet = Tablet.get(tabletStack, (PlayerEntity) event.getEntityLiving());
            tablet.machine().stop();
        }
    }
}

