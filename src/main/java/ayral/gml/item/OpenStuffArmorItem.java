package ayral.gml.item;

import ayral.gml.OpenStuffMod;
import ayral.gml.model.OpenArmorModel;
import li.cil.oc.api.CreativeTab;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class OpenStuffArmorItem extends DyeableArmorItem {
    public int light_color = 0x00FF00;

    public OpenStuffArmorItem(EquipmentSlotType slot) {
        super(OpenStuffArmorMaterial.OPEN_ARMOR_MATERIAL, slot, new Item.Properties().tab(CreativeTab.instance));
    }

    private boolean isWearingFullSet(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!(stack.getItem() instanceof OpenStuffArmorItem)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClientSide) return;

        if (this.slot == EquipmentSlotType.CHEST){

        }
    }



    @Nullable
    @Override
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        int color = isWearingFullSet(entityLiving) ? 0x00FF00 : this.light_color;
        return new OpenArmorModel(1.0F, armorSlot, entityLiving,color);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        if(type == "overlay"){
            if(slot == EquipmentSlotType.LEGS){
                return OpenStuffMod.MOD_ID + ":textures/models/armor/open_armor_layer_2_overlay.png";
            }
            return OpenStuffMod.MOD_ID + ":textures/models/armor/open_armor_layer_1_overlay.png";
        }
        if(slot == EquipmentSlotType.LEGS){
            return OpenStuffMod.MOD_ID + ":textures/models/armor/open_armor_layer_2.png";
        }
        return OpenStuffMod.MOD_ID + ":textures/models/armor/open_armor_layer_1.png";
    }


    public void onArmorKeyPress(PlayerEntity player, ItemStack chestStack) {
        OpenStuffArmorItem armor = (OpenStuffArmorItem) chestStack.getItem();

    }
}

