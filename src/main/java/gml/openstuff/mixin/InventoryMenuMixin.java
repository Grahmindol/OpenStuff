package gml.openstuff.mixin;

import gml.openstuff.ItemMachineManager;
import gml.openstuff.ItemMachineWrapper;
import gml.openstuff.OpenStuff;
import gml.openstuff.container.ArmorPieceContainer;
import gml.openstuff.container.CustomArmorComponentSlot;
import gml.openstuff.data.MachineData;
import gml.openstuff.integration.opencomputers.ArmorHost;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.client.Textures;
import li.cil.oc.client.Textures$;
import li.cil.oc.common.menu.AbstractMenu;
import li.cil.oc.common.menu.ComponentSlot;
import li.cil.oc.common.menu.StaticComponentSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {

    public ArmorPieceContainer ctn;
    public Inventory playerInventory;

    protected InventoryMenuMixin(@Nullable MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    private static ItemMachineWrapper getContainer(Player owner){
        ItemStack stack = owner.getItemBySlot(EquipmentSlot.CHEST);
        if(!stack.is(OpenStuff.OPEN_CHEST)) return null;
        return ItemMachineManager.getWeak(stack, owner.level());
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addCustomArmorSlots(Inventory playerInventory, boolean active, Player owner, CallbackInfo ci) {
        this.ctn = new ArmorPieceContainer(owner, EquipmentSlot.CHEST);
        this.playerInventory = playerInventory;

        // Direct instantiation removes the nested anonymous class error from Mixin
        addSlot(new CustomArmorComponentSlot(
                this.ctn,
                31,
                146,
                62,
                this.playerInventory,
                owner
        ));
    }



}