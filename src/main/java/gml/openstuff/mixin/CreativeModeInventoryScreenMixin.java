package gml.openstuff.mixin;

import gml.openstuff.Networking;
import gml.openstuff.OpenStuff;
import gml.openstuff.container.ArmorPieceContainer;
import gml.openstuff.data.MachineData;
import li.cil.oc.Localization;
import li.cil.oc.client.Textures;
import li.cil.oc.client.gui.ImageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Unique
    private ImageButton openstuff$powerButton;

    @Accessor("selectedTab")
    static CreativeModeTab getSelectedTab() {
        throw new AssertionError();
    }

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void openstuff$onInit(CallbackInfo ci) {
        this.openstuff$powerButton = this.addRenderableWidget(new ImageButton(
                this.leftPos + 126, this.topPos + 18, 18, 18,
                (Button b) -> this.openstuff$onPress(),
                Textures.GUISprites$.MODULE$.ButtonPower(),
                Component.empty(),
                true, 0xE0E0E0, 0xA0A0A0, 0xFFFFA0, -1, -1, -1
        ));
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void openstuff$onSelectTab(CreativeModeTab tab, CallbackInfo ci) {
        for (int i = 0; i < this.getMenu().slots.size(); i++) {
            Slot currentSlot = this.getMenu().slots.get(i);
            if(!(currentSlot.container instanceof  ArmorPieceContainer)) continue;

            int targetX = (tab.getType() == CreativeModeTab.Type.INVENTORY) ? 146 : -1000;
            int targetY = (tab.getType() == CreativeModeTab.Type.INVENTORY) ? 19 : -1000;

            Slot newSlot = openstuff$createSlotWrapper(((SlotWrapperAccessor)currentSlot).openstuff$getTarget(), 0, targetX, targetY) ;
            this.getMenu().slots.set(i, newSlot);
        }
    }

    @Inject(method = "slotClicked",at = @At("HEAD"),cancellable = true)
    private void openstuff$onSlotClicked(@Nullable Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        if(slot == null || !(slot.container instanceof ArmorPieceContainer)) return;
        super.slotClicked(((SlotWrapperAccessor)slot).openstuff$getTarget(), slotId, mouseButton, type);
        ci.cancel();
    }

    @Unique
    private void openstuff$onPress() {
        ItemStack chest = this.minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
        MachineData data = new MachineData(chest, VanillaRegistries.createLookup());
        Networking.setServerState(chest, !data.isRunning);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void openstuff$onRenderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null || this.openstuff$powerButton == null) return;

        ItemStack chest = this.minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.is(OpenStuff.OPEN_CHEST) || getSelectedTab().getType() != CreativeModeTab.Type.INVENTORY) {
            this.openstuff$powerButton.visible = false;
            return;
        }

        graphics.blit(Textures.GUI$.MODULE$.Slot(), leftPos + 145, topPos + 18, 0.0F, 0.0F, 18, 18, 18, 18);

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

    @Unique
    private static Slot openstuff$createSlotWrapper(Slot target, int index, int x, int y) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper");
            java.lang.reflect.Constructor<?> ctor = clazz.getDeclaredConstructors()[0];
            ctor.setAccessible(true);
            return (Slot) ctor.newInstance(target, index, x, y);
        } catch (Exception e) {
            OpenStuff.LOGGER.error("Can't create a SlotWrapper: {}", e.getMessage());
            return target;
        }
    }
}
