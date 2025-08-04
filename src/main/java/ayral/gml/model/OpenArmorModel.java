package ayral.gml.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

public class OpenArmorModel extends BipedModel {


    public ModelRenderer bipedLeftArmOverlay;
    public ModelRenderer bipedRightArmOverlay;
    public ModelRenderer bipedLeftLegOverlay;
    public ModelRenderer bipedRightLegOverlay;
    public ModelRenderer bipedBodyOverlay;

    public final EquipmentSlotType slot ;
    private LivingEntity entity;
    private boolean isOverlay = false;
    private boolean isRunning = false;
    private int lightColor;


    public OpenArmorModel(float modelSize, EquipmentSlotType slotIn, LivingEntity entity, int color, boolean isRunning){
        super(modelSize, 0, 64, 64);
        this.slot = slotIn;
        this.entity = entity;
        this.lightColor = color;
        this.isRunning = isRunning;

        // Bras gauche overlay
        this.bipedLeftArmOverlay = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmOverlay.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftArmOverlay.setPos(5.0f,2.0f,0.0f);

        // Bras droit overlay
        this.bipedRightArmOverlay = new ModelRenderer(this, 40, 32);
        this.bipedRightArmOverlay.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightArmOverlay.setPos(-5.0f,2.0f,0.0f);

        // Jambe gauche overlay
        this.bipedLeftLegOverlay = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegOverlay.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftLegOverlay.setPos(1.9f,12.0f,0.0f);

        // Jambe droite overlay
        this.bipedRightLegOverlay = new ModelRenderer(this, 0, 32);
        this.bipedRightLegOverlay.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightLegOverlay.setPos(-1.9f,12.0f,0.0f);
        // Torse overlay
        this.bipedBodyOverlay = new ModelRenderer(this, 16, 32);
        this.bipedBodyOverlay.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        this.bipedBodyOverlay.setPos(0f,0f,0f);
    }


    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {

        if (isOverlay){
            red = ((lightColor >>> 16) & 0xFF)/255f;
            green = ((lightColor >>> 8) & 0xFF)/255f;
            blue = ((lightColor >>> 0) & 0xFF)/255f;

            if (!isRunning){
                red /= 4.0f;
                green /= 4.0f;
                blue /= 4.0f;
            }
        }

        super.renderToBuffer(matrixStack,buffer,packedLight,packedOverlay,red,green,blue,alpha);

        this.bipedLeftArmOverlay.copyFrom(this.leftArm);
        this.bipedRightArmOverlay.copyFrom(this.rightArm);
        this.bipedLeftLegOverlay.copyFrom(this.leftLeg);
        this.bipedRightLegOverlay.copyFrom(this.rightLeg);
        this.bipedBodyOverlay.copyFrom(this.body);


        matrixStack.pushPose();

        switch (slot) {
            case FEET:
            case LEGS:
                this.bipedLeftLegOverlay.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                this.bipedRightLegOverlay.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                break;

            case CHEST: // chestplate
                this.bipedLeftArmOverlay.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                this.bipedRightArmOverlay.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                this.bipedBodyOverlay.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                break;
        }

        matrixStack.popPose();
        isOverlay = true;
    }
}
