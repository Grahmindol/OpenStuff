package ayral.gml.model;

import ayral.gml.item.OpenArmorItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class ArmorComponentLayer <T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {
    private final ElytraModel<T> elytraModel = new ElytraModel<>();

    public ArmorComponentLayer(IEntityRenderer<T, M> p_i50926_1_) {
        super(p_i50926_1_);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!OpenArmorItem.isWearingFullSet(entity)) return;
        ItemStack chest = entity.getItemBySlot(EquipmentSlotType.CHEST);
        if (!chest.getOrCreateTag().contains("feature")) return;
        CompoundNBT feature = chest.getOrCreateTag().getCompound("feature");

        int lightColor = ((OpenArmorItem)(chest.getItem())).getColor(chest);
        float red = ((lightColor >>> 16) & 0xFF)/255f;
        float green = ((lightColor >>> 8) & 0xFF)/255f;
        float blue = ((lightColor >>> 0) & 0xFF)/255f;

        if (!OpenArmorItem.isRunning(chest)){
            red /= 4.0f;
            green /= 4.0f;
            blue /= 4.0f;
        }


        if (feature.contains("flying")) {
            matrixStack.pushPose();
            this.getParentModel().copyPropertiesTo(this.elytraModel);
            this.elytraModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            IVertexBuilder ivertexbuilder = buffer.getBuffer(RenderType.armorCutoutNoCull(new ResourceLocation("openstuff:textures/models/armor/elytra_upgrade.png")));
            this.elytraModel.renderToBuffer(matrixStack, ivertexbuilder, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, 1);
            matrixStack.popPose();
        }

    }
}
