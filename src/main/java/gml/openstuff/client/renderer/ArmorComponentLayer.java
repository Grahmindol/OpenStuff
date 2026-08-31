package gml.openstuff.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import gml.openstuff.data.PieceData;
import gml.openstuff.item.OpenArmorPiece;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ArmorComponentLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private static final List<ArmorDriverRenderer<?>> RENDERERS = new ArrayList<>();

    private final HumanoidModel<T> innerModel;
    private final HumanoidModel<T> outerModel;

    public ArmorComponentLayer(RenderLayerParent<T, M> renderer, HumanoidModel<T> innerModel, HumanoidModel<T> outerModel) {
        super(renderer);
        this.innerModel = innerModel;
        this.outerModel = outerModel;
    }

    /**
     * Registers a new driver renderer
     */
    public static void add(ArmorDriverRenderer<?> renderer) {
        RENDERERS.add(renderer);
    }

    @Override
    public void render(
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            @NotNull T livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        this.renderArmorPiece(poseStack, buffer, livingEntity, EquipmentSlot.CHEST, packedLight, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        this.renderArmorPiece(poseStack, buffer, livingEntity, EquipmentSlot.LEGS, packedLight, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        this.renderArmorPiece(poseStack, buffer, livingEntity, EquipmentSlot.FEET, packedLight, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        this.renderArmorPiece(poseStack, buffer, livingEntity, EquipmentSlot.HEAD, packedLight, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    @SuppressWarnings("unchecked")
    private void renderArmorPiece(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            T livingEntity,
            EquipmentSlot slot,
            int packedLight,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        ItemStack piece_stack = livingEntity.getItemBySlot(slot);
        if (piece_stack.getItem() instanceof OpenArmorPiece armorPiece) {
            if (armorPiece.getEquipmentSlot() == slot) {
                PieceData data = new PieceData(piece_stack);

                boolean isInnerModel = (slot == EquipmentSlot.LEGS);
                HumanoidModel<T> model = isInnerModel ? this.innerModel : this.outerModel;

                this.getParentModel().copyPropertiesTo(model);
                this.setPartVisibility(model, slot);

                for (ItemStack stack : data.items) {
                    DriverItem driver = Driver.driverFor(stack);
                    if (driver == null) continue;

                    for (ArmorDriverRenderer<?> rawRenderer : RENDERERS) {
                        ArmorDriverRenderer<T> renderer = (ArmorDriverRenderer<T>) rawRenderer;
                        if (renderer.workWith(driver)) {
                            renderer.render(
                                    driver,
                                    stack,
                                    piece_stack,
                                    slot,
                                    model,
                                    isInnerModel,
                                    poseStack,
                                    bufferSource,
                                    packedLight,
                                    livingEntity,
                                    limbSwing,
                                    limbSwingAmount,
                                    partialTick,
                                    ageInTicks,
                                    netHeadYaw,
                                    headPitch
                            );
                        }
                    }
                }
            }
        }
    }

    protected void setPartVisibility(HumanoidModel<T> model, EquipmentSlot slot) {
        model.setAllVisible(false);
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
        }
    }
}