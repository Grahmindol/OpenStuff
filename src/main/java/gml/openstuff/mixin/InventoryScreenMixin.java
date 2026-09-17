package gml.openstuff.mixin;

import gml.openstuff.Networking;
import gml.openstuff.OpenStuff;
import gml.openstuff.data.MachineData;
import li.cil.oc.Localization;
import li.cil.oc.client.Textures;
import li.cil.oc.client.gui.ImageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    @Unique
    private ImageButton openstuff$powerButton;

    public InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void openstuff$onInit(CallbackInfo ci) {
        this.openstuff$powerButton = this.addRenderableWidget(new ImageButton(
                this.leftPos + 125, this.topPos + 61, 18, 18,
                (Button b) -> this.openstuff$onPress(),
                Textures.GUISprites$.MODULE$.ButtonPower(),
                Component.empty(),
                true, 0xE0E0E0, 0xA0A0A0, 0xFFFFA0, -1, -1, -1
        ));
    }

    @Unique
    private void openstuff$onPress() {
        if(this.minecraft == null || this.minecraft.player == null) return;
        ItemStack chest = this.minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
        MachineData data = new MachineData(chest, VanillaRegistries.createLookup());
        Networking.setServerState(chest, !data.isRunning);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void openstuff$onRenderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null || this.openstuff$powerButton == null) return;

        ItemStack chest = this.minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !chest.is(OpenStuff.OPEN_CHEST)) {
            this.openstuff$powerButton.visible = false;
            return;
        }


        graphics.blit(Textures.GUI$.MODULE$.Slot(), leftPos + 145, topPos + 61, 0.0F, 0.0F, 18, 18, 18, 18);

        // Power button
        MachineData data = new MachineData(chest, VanillaRegistries.createLookup());
        this.openstuff$powerButton.visible = true;
        this.openstuff$powerButton.toggled_$eq(data.isRunning);
        this.openstuff$powerButton.setTooltip(Tooltip.create(Component.literal(
                data.isRunning
                        ? Localization.localizeImmediately("gui.Robot.TurnOff")
                        : Localization.localizeImmediately("gui.Robot.TurnOn")
        )));
    }
}