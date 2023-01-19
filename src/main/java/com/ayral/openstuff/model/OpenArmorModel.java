package com.ayral.openstuff.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class OpenArmorModel extends ModelBiped {

    public ModelRenderer bipedLeftArmOverlay;
    public ModelRenderer bipedRightArmOverlay;
    public ModelRenderer bipedLeftLegOverlay;
    public ModelRenderer bipedRightLegOverlay;
    public ModelRenderer bipedBodyOverlay;

    public final EntityEquipmentSlot slot ;

    public OpenArmorModel(float modelSize, EntityEquipmentSlot slotIn) {
        super(modelSize,0,64,64);
        this.slot = slotIn;
        this.bipedLeftArmOverlay = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmOverlay.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftArmOverlay.setRotationPoint(5.0F, 2.0F, 0.0F);

        this.bipedRightArmOverlay = new ModelRenderer(this, 40, 32);
        this.bipedRightArmOverlay.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightArmOverlay.setRotationPoint(-5.0F, 2.0F, 0.0F);

        this.bipedLeftLegOverlay = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegOverlay.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftLegOverlay.setRotationPoint(1.9F, 12.0F, 0.0F);

        this.bipedRightLegOverlay = new ModelRenderer(this, 0, 32);
        this.bipedRightLegOverlay.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightLegOverlay.setRotationPoint(-1.9F, 12.0F, 0.0F);

        this.bipedBodyOverlay = new ModelRenderer(this, 16, 32);
        this.bipedBodyOverlay.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        this.bipedBodyOverlay.setRotationPoint(0.0F, 0.0F, 0.0F);


    }

    public void fistPersonRender(){

    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.pushMatrix();
        if (entityIn.isSneaking())
        {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }
        switch (slot.getSlotIndex()){
            case 1:
            case 2:
                this.bipedLeftLegOverlay.render(scale);
                this.bipedRightLegOverlay.render(scale);
                break;
            case 3:
                this.bipedLeftArmOverlay.render(scale);
                this.bipedRightArmOverlay.render(scale);
                this.bipedBodyOverlay.render(scale);
                break;
        }
        GlStateManager.popMatrix();
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        this.bipedLeftArmOverlay.rotateAngleY = this.bipedLeftArm.rotateAngleY;
        this.bipedLeftArmOverlay.rotateAngleX = this.bipedLeftArm.rotateAngleX;
        this.bipedLeftArmOverlay.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;

        this.bipedRightArmOverlay.rotateAngleY = this.bipedRightArm.rotateAngleY;
        this.bipedRightArmOverlay.rotateAngleX = this.bipedRightArm.rotateAngleX;
        this.bipedRightArmOverlay.rotateAngleZ = this.bipedRightArm.rotateAngleZ;

        this.bipedLeftLegOverlay.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
        this.bipedLeftLegOverlay.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
        this.bipedLeftLegOverlay.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;

        this.bipedRightLegOverlay.rotateAngleY = this.bipedRightLeg.rotateAngleY;
        this.bipedRightLegOverlay.rotateAngleX = this.bipedRightLeg.rotateAngleX;
        this.bipedRightLegOverlay.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;

        this.bipedBodyOverlay.rotateAngleY = this.bipedBody.rotateAngleY;
        this.bipedBodyOverlay.rotateAngleX = this.bipedBody.rotateAngleX;
        this.bipedBodyOverlay.rotateAngleZ = this.bipedBody.rotateAngleZ;

        this.bipedRightLegOverlay.rotationPointZ = this.bipedRightLeg.rotationPointZ;
        this.bipedLeftLegOverlay.rotationPointZ = this.bipedLeftLeg.rotationPointZ;
        this.bipedRightLegOverlay.rotationPointY = this.bipedRightLeg.rotationPointY;
        this.bipedLeftLegOverlay.rotationPointY = this.bipedLeftLeg.rotationPointY;


    }
}
