package ayral.gml.model;

import ayral.gml.item.OpenArmorItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class ArmorComponentLayer <T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {
    private final  ElytraComponentRender elytra;
    private final  ConduitComponentRender conduit;

    public ArmorComponentLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
        this.conduit = new ConduitComponentRender<>(renderer);
        this.elytra = new ElytraComponentRender<>(renderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!OpenArmorItem.isWearingFullSet(entity)) return;
        ItemStack chest = entity.getItemBySlot(EquipmentSlotType.CHEST);
        if (!chest.getOrCreateTag().contains("feature")) return;
        CompoundNBT feature = chest.getOrCreateTag().getCompound("feature");


        if (feature.contains("flying")) {
            this.elytra.render(matrixStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }

        if (feature.contains("swimming")) {
            this.conduit.render(matrixStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
