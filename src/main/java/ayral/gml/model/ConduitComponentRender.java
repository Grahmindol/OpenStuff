package ayral.gml.model;

import ayral.gml.item.OpenArmorItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.ConduitTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ConduitTileEntity;
import net.minecraft.util.ResourceLocation;

public class ConduitComponentRender<T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {
    private final ConduitTileEntityRenderer conduitRenderer;

    public ConduitComponentRender(IEntityRenderer<T, M> renderer) {
        super(renderer);
        this.conduitRenderer = new ConduitTileEntityRenderer(TileEntityRendererDispatcher.instance);

    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ConduitTileEntity conduit_te = new FakeConduitTE(entity);
        matrixStack.pushPose();

        // Segmentation exacte : applique la position + rotation du torse du modèle joueur
        this.getParentModel().body.translateAndRotate(matrixStack);
        matrixStack.translate(-0.15, 0.1, -0.35);
        matrixStack.scale(0.3f, 0.3f, 0.3f);


        // Animation – identical to tile entity: use tickCount + partialTicks for continuity
        float animationTime = entity.tickCount + partialTicks;

        // Appel direct au rendu vanilla
        this.conduitRenderer.render(
                conduit_te,
                0.0f,
                matrixStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        matrixStack.popPose();
    }

    public static class FakeConduitTE extends ConduitTileEntity {
        private final LivingEntity holder;

        public FakeConduitTE(LivingEntity entity) {
            super();
            this.holder = entity;
            this.level = entity.level;
        }

        @Override
        public boolean isActive() {
            ItemStack chest = holder.getItemBySlot(EquipmentSlotType.CHEST);
            return OpenArmorItem.isSwimmingEnable(chest) && holder.isInWater();
        }
    }
}


