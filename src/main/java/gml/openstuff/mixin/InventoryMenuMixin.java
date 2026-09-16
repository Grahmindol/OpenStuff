package gml.openstuff.mixin;

import gml.openstuff.container.ArmorPieceContainer;
import gml.openstuff.container.CustomArmorComponentSlot;
import gml.openstuff.container.InventoryMenuAccess;
import gml.openstuff.data.MachineData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu implements InventoryMenuAccess {

    @Unique
    private DataSlot openstuff$isRunningDataSlot;
    @Unique
    private Slot openstuff$armorContainerSlot;


    protected InventoryMenuMixin(@Nullable MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addCustomArmorSlots(Inventory playerInventory, boolean active, Player owner, CallbackInfo ci) {
        // Direct instantiation removes the nested anonymous class error from Mixin
        this.openstuff$armorContainerSlot = this.addSlot(new CustomArmorComponentSlot(
                new ArmorPieceContainer(owner, EquipmentSlot.CHEST),
                31,
                146,
                62,
                playerInventory,
                owner
        ));

        this.openstuff$isRunningDataSlot = this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                ItemStack chest = owner.getItemBySlot(EquipmentSlot.CHEST);
                if (chest.isEmpty()) return 0;
                MachineData data = new MachineData(chest, owner.level().registryAccess());
                return data.isRunning ? 1 : 0;
            }

            @Override
            public void set(int i) {

            }
        });
    }

    @Override
    public DataSlot openstuff$getIsRunningDataSlot() {
        return this.openstuff$isRunningDataSlot;
    }

    @Override
    public Slot openstuff$getArmorContainerSlot() {
        return this.openstuff$armorContainerSlot;
    }

}