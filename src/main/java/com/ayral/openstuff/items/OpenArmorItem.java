package com.ayral.openstuff.items;


import com.ayral.openstuff.OpenStuffMod;
import com.ayral.openstuff.model.OpenArmorModel;
import li.cil.oc.api.CreativeTab;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.List;

import static com.ayral.openstuff.items.OpenStuffItems.setItemName;

public class OpenArmorItem extends ItemArmor {
    public OpenArmorItem(String name, ItemArmor.ArmorMaterial material, int renderIndex, EntityEquipmentSlot slotIn) {
        super(material,renderIndex,slotIn);
        setItemName(this,name);
        setCreativeTab(CreativeTab.instance);
        setMaxStackSize(1);
        setMaxDamage(1);

    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        if(type == "overlay"){
            if(stack.hasTagCompound()){
                if(stack.getTagCompound().hasKey("color")){
                    NBTTagCompound tag = stack.getTagCompound();
                    if(slot == EntityEquipmentSlot.LEGS) {
                        return OpenStuffMod.MODID + ":textures/models/armor/open_armor_layer_2_overlay_color_"+ tag.getInteger("color") +".png";
                    }
                    return OpenStuffMod.MODID + ":textures/models/armor/open_armor_layer_1_overlay_color_"+ tag.getInteger("color") +".png";
                }
            }
            if(slot == EntityEquipmentSlot.LEGS) {
                return OpenStuffMod.MODID + ":textures/models/armor/open_armor_layer_2_overlay.png";
            }
            return OpenStuffMod.MODID + ":textures/models/armor/open_armor_layer_1_overlay.png";
        }else if(slot == EntityEquipmentSlot.LEGS){
            return OpenStuffMod.MODID + ":textures/models/armor/open_armor_layer_2.png";
        }
        return OpenStuffMod.MODID + ":textures/models/armor/open_armor_layer_1.png";
    }

    @Override
    public boolean hasOverlay(ItemStack stack){
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack , EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return new OpenArmorModel(1,armorSlot);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @ParametersAreNonnullByDefault
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(stack.hasTagCompound()){
            tooltip.add(String.format("§a%s/%s FE§7",  stack.getTagCompound().getInteger("Energy"), stack.getTagCompound().getInteger("EnergyCapacity")));
            tooltip.add("§2usage 10 FE/tick§7");

            if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                tooltip.add("hold [§fLSHIFT§7] to show upgrades");
            else{

            }
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        if(world.isRemote) return;
        if(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == OpenStuffItems.openHelmet && player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).hasTagCompound() &&
                player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == OpenStuffItems.openChestPlate && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).hasTagCompound() &&
                player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == OpenStuffItems.openLeggings && player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).hasTagCompound() &&
                player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == OpenStuffItems.openBoots && player.getItemStackFromSlot(EntityEquipmentSlot.FEET).hasTagCompound()){
            consumeEnergy(stack,10);
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack){
        return stack.hasTagCompound();
    }

    @Override
    @ParametersAreNonnullByDefault
    public double getDurabilityForDisplay(ItemStack stack){
        return 1 - ((double) stack.getTagCompound().getInteger("Energy") / stack.getTagCompound().getInteger("EnergyCapacity"));
    }

    public void consumeEnergy(ItemStack stack,int energy){
        if ((stack.getTagCompound().getInteger("Energy")-energy)>0)
            stack.getTagCompound().setInteger("Energy",stack.getTagCompound().getInteger("Energy")-energy);
    }

    @SuppressWarnings("unused")
    private static class EnergyCapabilityProvider implements ICapabilityProvider{
        final EnergyStorage storage;

        EnergyCapabilityProvider(final ItemStack stack){
            this.storage = new EnergyStorage(0, 1000, 1000){
                @Override
                public int getEnergyStored(){
                    assert stack.getTagCompound() != null;
                    return stack.getTagCompound().getInteger("Energy");
                }

                @Override
                public int getMaxEnergyStored(){
                    assert stack.getTagCompound() != null;
                    return stack.getTagCompound().getInteger("EnergyCapacity");
                }

                void setEnergyStored(int energy){
                    assert stack.getTagCompound() != null;
                    stack.getTagCompound().setInteger("Energy", energy);
                }

                @Override
                public int receiveEnergy(int receive, boolean simulate){
                    int energy = this.getEnergyStored();

                    int energyReceived = Math.min(this.getMaxEnergyStored()-energy, Math.min(this.maxReceive, receive));

                    if(!simulate) this.setEnergyStored(energy+energyReceived);

                    return energyReceived;
                }

                @Override
                public int extractEnergy(int extract, boolean simulate){
                    if(!this.canExtract()) return 0;

                    int energy = this.getEnergyStored();

                    int energyExtracted = Math.min(energy, Math.min(this.maxExtract, extract));
                    if(!simulate) this.setEnergyStored(energy-energyExtracted);

                    return energyExtracted;
                }
            };
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing){
            return this.getCapability(capability, facing) != null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing){
            if(capability == CapabilityEnergy.ENERGY){
                return (T) this.storage;
            }
            return null;
        }
    }

    public static ItemStack setDefaultEnergyTag(ItemStack container, int energy, IInventory inventory) {

        if (!container.hasTagCompound()) {
            container.setTagCompound(new NBTTagCompound());
        }
        assert container.getTagCompound() != null;
        container.getTagCompound().setInteger("Energy", energy);
        container.getTagCompound().setInteger("EnergyCapacity", energy);

        int size = inventory.getSizeInventory();
        for (int i = size-10; i > 3; i--) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                if (inventory.getStackInSlot(i).getItem() instanceof ArmorUpgrade){
                    container.getTagCompound().setTag(((ArmorUpgrade)inventory.getStackInSlot(i).getItem()).getUpgradeName(),new NBTTagList());
                }
            }
        }

        return container;
    }
}
