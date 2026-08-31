package gml.openstuff.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.oc.api.driver.DriverItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ArmorDriverRenderer<T extends LivingEntity> {

    boolean workWith(DriverItem driver);

    void render(
            DriverItem driver,
            ItemStack driverStack,
            ItemStack armorStack,
            EquipmentSlot slot,
            HumanoidModel<T> model,
            boolean isInnerModel,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    );
}