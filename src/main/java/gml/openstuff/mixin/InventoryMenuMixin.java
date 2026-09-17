package gml.openstuff.mixin;

import gml.openstuff.container.ArmorPieceContainer;
import gml.openstuff.container.CustomArmorComponentSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu  {
    protected InventoryMenuMixin(@Nullable MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void openstuff$onInit(Inventory playerInventory, boolean active, Player owner, CallbackInfo ci) {
        this.addSlot(new CustomArmorComponentSlot(
                new ArmorPieceContainer(owner, EquipmentSlot.CHEST),
                31,
                146,
                62,
                playerInventory,
                owner
        ));
    }
}