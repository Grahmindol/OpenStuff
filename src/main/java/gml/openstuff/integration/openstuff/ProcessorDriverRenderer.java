package gml.openstuff.integration.openstuff;

import com.mojang.blaze3d.vertex.PoseStack;
import gml.openstuff.client.renderer.ArmorDriverRenderer;
import li.cil.oc.api.driver.DriverItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ProcessorDriverRenderer implements ArmorDriverRenderer<LivingEntity> {
    @Override
    public boolean workWith(DriverItem driver) {
        return driver instanceof li.cil.oc.api.driver.item.Processor;
    }

    @Override
    public void render(DriverItem driver, ItemStack driverStack, ItemStack armorStack, EquipmentSlot slot, HumanoidModel<LivingEntity> model, boolean isInnerModel, PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (driverStack.isEmpty()) return;

        // 1. Cibler la partie du corps selon le slot d'armure
        ModelPart targetPart = model.body;

        poseStack.pushPose();

        // 2. Transférer les rotations et mouvements de l'entité (marche, tête qui tourne...) à la matrice
        targetPart.translateAndRotate(poseStack);

        // 3. Ajuster la position, la rotation et l'échelle de l'item sur la surface
        //positionItemOnSurface(poseStack, slot);
        poseStack.translate(0.0D, 0.2D, -0.2D); // Décalage Z négatif = avant du plastron
        poseStack.scale(0.2F, 0.2F, 0.2F);   // Réduire la taille de l'item

        // 4. Rendre l'item via le système de rendu standard de Minecraft
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(
                driverStack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                livingEntity.level(),
                0
        );

        poseStack.popPose();
    }
}
