package gml.openstuff.integration.openstuff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import gml.openstuff.client.renderer.ArmorDriverRenderer;
import gml.openstuff.integration.opencomputers.TrimDriver;
import li.cil.oc.api.driver.DriverItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimPattern;

public class TrimDriverRenderer implements ArmorDriverRenderer<LivingEntity> {
    @Override
    public boolean workWith(DriverItem driver) {
        return driver instanceof TrimDriver;
    }

    @Override
    public void render(DriverItem driver, ItemStack driverStack, ItemStack armorStack, EquipmentSlot slot, HumanoidModel<LivingEntity> model, boolean isInnerModel, PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        TrimDriver trim_driver = (TrimDriver) driver;

        ArmorTrim trim = trim_driver.getTrim(driverStack);
        int color = trim_driver.getColor(driverStack);
        if(trim != null) renderGrayscaleTrim(poseStack, buffer, packedLight, model, isInnerModel, trim.pattern(), color);
    }


    private void renderGrayscaleTrim(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            Model model,
            boolean innerTexture,
            Holder<TrimPattern> patternHolder,
            int argbColor
    ) {
        ResourceLocation patternId = patternHolder.value().assetId();

        // TODO: add a white custom material
        String ext = innerTexture ? "_leggings_iron" : "_iron";

        ResourceLocation spriteLocation = ResourceLocation.fromNamespaceAndPath(
                patternId.getNamespace(),
                "trims/models/armor/" + patternId.getPath() + ext
        );

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getModelManager()
                .getAtlas(Sheets.ARMOR_TRIMS_SHEET)
                .getSprite(spriteLocation);

        VertexConsumer vertexConsumer = sprite.wrap(
                bufferSource.getBuffer(RenderType.armorCutoutNoCull(Sheets.ARMOR_TRIMS_SHEET))
        );

        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, argbColor);
    }
}
