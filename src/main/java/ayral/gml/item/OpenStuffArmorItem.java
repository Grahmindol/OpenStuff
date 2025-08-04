package ayral.gml.item;

import ayral.gml.NetworkHandler;
import ayral.gml.OpenStuffMod;
import ayral.gml.integration.ArmorComponent;
import ayral.gml.model.OpenArmorModel;
import ayral.gml.network.OpenTabletGuiPacket;
import li.cil.oc.Settings;
import li.cil.oc.api.CreativeTab;
import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.client.gui.Screen;
import li.cil.oc.common.Tier;
import li.cil.oc.common.item.Tablet;
import li.cil.oc.common.item.TabletWrapper;
import li.cil.oc.common.item.data.TabletData;
import li.cil.oc.common.item.traits.Chargeable;
import li.cil.oc.server.machine.luac.LuaStateFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OpenStuffArmorItem extends DyeableArmorItem implements Chargeable {
    public int light_color = 0xFFFFFF;
    public boolean is_armor_running = false;
    private ArmorComponent armorComponent = null;

    public OpenStuffArmorItem(EquipmentSlotType slot) {
        super(OpenStuffArmorMaterial.OPEN_ARMOR_MATERIAL, slot, new Item.Properties().tab(CreativeTab.instance));
    }

    private static boolean isWearingFullSet(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!(stack.getItem() instanceof OpenStuffArmorItem)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClientSide || slot != EquipmentSlotType.CHEST) return;

        CompoundNBT tag = stack.getOrCreateTag();

        ensureTablet(tag);
        ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
        rechargeTabletFromArmor(tag, tabletStack);

        TabletWrapper tablet = Tablet.get(tabletStack, player);
        initializeArmorComponent(tag, tablet, player);

        tablet.connectComponents();
        tablet.update(world, player, -1, false);
        this.is_armor_running = tablet.machine().isRunning();
        tag.put("Tablet", tabletStack.save(new CompoundNBT()));


        CompoundNBT armorTag = new CompoundNBT();
        this.armorComponent.node().saveData(armorTag);
        tag.put("Armor", armorTag);

        uniformizeArmorEnergy(player);
    }

    private void ensureTablet(CompoundNBT tag) {
        if (!tag.contains("Tablet")) {
            ItemStack tabletStack = createConfiguredTablet();
            CompoundNBT tabletTag = new CompoundNBT();
            tabletStack.save(tabletTag);
            tag.put("Tablet", tabletTag);
        }
    }

    private void rechargeTabletFromArmor(CompoundNBT tag, ItemStack tabletStack) {
        if (!(tabletStack.getItem() instanceof Chargeable)) return;

        Chargeable tabletItem = (Chargeable) tabletStack.getItem();
        double max = tabletItem.maxCharge(tabletStack);
        double charge = tabletItem.getCharge(tabletStack);

        if (charge < max) {
            double missing = max - charge;

            if (!tag.contains("Energy")) {
                tabletItem.setCharge(tabletStack, max); // 💡 charge créative
            } else {
                double chestEnergy = tag.getDouble("Energy");
                double transfer = Math.min(chestEnergy, missing);

                tabletItem.setCharge(tabletStack, charge + transfer);
                tag.putDouble("Energy", chestEnergy - transfer);
            }
        }
    }

    private void initializeArmorComponent(CompoundNBT tag, TabletWrapper tablet, PlayerEntity player) {
        if (this.armorComponent == null)
            this.armorComponent = new ArmorComponent(player);

        if (tag.contains("Armor")) {
            CompoundNBT armorTag = tag.getCompound("Armor");
            this.armorComponent.node().loadData(armorTag);
        }

        if (!this.armorComponent.node().canBeReachedFrom(tablet.node()))
            tablet.connectItemNode(this.armorComponent.node());
    }

    private void uniformizeArmorEnergy(PlayerEntity player) {
        List<ItemStack> openStuffPieces = new ArrayList<>();
        double totalEnergy = 0;
        double totalMax = 0;

        for (ItemStack piece : player.getArmorSlots()) {
            if (piece.getItem() instanceof Chargeable) {
                Chargeable c = (Chargeable) piece.getItem();
                double max = c.maxCharge(piece);
                double charge = c.getCharge(piece);
                totalMax += max;
                totalEnergy += charge;
                openStuffPieces.add(piece);
            }
        }

        if (openStuffPieces.size() > 1 && totalMax > 0) {
            double uniformRatio = totalEnergy / totalMax;
            for (ItemStack piece : openStuffPieces) {
                Chargeable c = (Chargeable) piece.getItem();
                double max = c.maxCharge(piece);
                c.setCharge(piece, max * uniformRatio);
            }
        }
    }

    // ------------------------ Rendering logic  ------------------------

    @Override
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        int color = 0x202020;

        if (isWearingFullSet(entityLiving)) {
            if (armorSlot == EquipmentSlotType.CHEST && this.is_armor_running) {
                color = this.light_color;
            } else {
                ItemStack chestStack = entityLiving.getItemBySlot(EquipmentSlotType.CHEST);
                if (((OpenStuffArmorItem) chestStack.getItem()).is_armor_running ) {
                    color = ((OpenStuffArmorItem) chestStack.getItem()).light_color;
                    this.is_armor_running = true;
                }else {
                    this.is_armor_running = false;
                }
            }
        }

        return new OpenArmorModel(1.0F, armorSlot, entityLiving, color);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        if(type == "overlay"){
            if(slot == EquipmentSlotType.LEGS){
                return OpenStuffMod.MOD_ID + ":textures/models/armor/open_armor_layer_2_overlay.png";
            }
            return OpenStuffMod.MOD_ID + ":textures/models/armor/open_armor_layer_1_overlay.png";
        }
        if(slot == EquipmentSlotType.LEGS){
            return OpenStuffMod.MOD_ID + ":textures/models/armor/open_armor_layer_2.png";
        }
        return OpenStuffMod.MOD_ID + ":textures/models/armor/open_armor_layer_1.png";
    }

    // ------------------------ GUI logic  ------------------------

    public static void openTabletGuiFromArmor(PlayerEntity player) {
        if (!(player.level.isClientSide)) return;

        ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.isEmpty() || !(chestStack.getItem() instanceof OpenStuffArmorItem)) return;

        NetworkHandler.INSTANCE.sendToServer(new OpenTabletGuiPacket());

        CompoundNBT tag = chestStack.getOrCreateTag();
        if (!tag.contains("Tablet")) return;
        ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
        if (!(tabletStack.getItem() instanceof Tablet)) return;

        TabletWrapper tablet = Tablet.get(tabletStack, player);

        tablet.connectComponents();
        tablet.update(player.level, player, -1, false); // -1 ticks, not from NBT

        tabletStack.getItem().releaseUsing(tabletStack,player.level,player,72000);

        tag.put("Tablet", tabletStack.save(new CompoundNBT()));
        CompoundNBT armorTag = new CompoundNBT();
        ((OpenStuffArmorItem) chestStack.getItem()).armorComponent.node().saveData(armorTag);
        chestStack.getOrCreateTag().put("Armor", armorTag);
    }


    // ------------------------ Creative fake tablet generator ------------------------

    private static ItemStack safeGetStack(String name) {
        ItemStack stack = li.cil.oc.common.init.Items.get(name).createItemStack(1);
        if (stack == null) return ItemStack.EMPTY;
        return stack;
    }

    private static ItemStack createConfiguredTablet() {
        TabletData data = new TabletData();

        data.tier_$eq(Tier.Four());
        data.energy_$eq(Settings.get().bufferTablet());
        data.maxEnergy_$eq(data.energy());

        ItemStack[] items = new ItemStack[32];

        for (int i = 0; i < 32; i++) {
            items[i] = ItemStack.EMPTY;
        }

        items[0] = safeGetStack("screen1");
        items[1] = safeGetStack("keyboard");

        items[7] = safeGetStack("graphicscard3");

        items[10] = LuaStateFactory.setDefaultArch(safeGetStack("cpu3"));
        items[11] = safeGetStack("ram6");
        items[12] = safeGetStack("ram6");

        items[13] = safeGetStack("luabios");
        items[14] = safeGetStack("hdd3");

        // Padding slots restants à ItemStack.EMPTY

        data.container_$eq(safeGetStack("diskdrive"));

        // Override slot 31 (dernier) par OpenOS
        items[31] = safeGetStack("openos");

        data.items_$eq(items);

        return data.createItemStack();
    }

    // ------------------------ Chargeable logic------------------------

    @Override
    public double maxCharge(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        return tag.contains("maxEnergy") ? tag.getDouble("maxEnergy") : 10000; // default max
    }

    @Override
    public double getCharge(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains("Energy")) return maxCharge(stack); // créatif = toujours plein
        return tag.getDouble("Energy");
    }

    @Override
    public void setCharge(ItemStack stack, double amount) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains("Energy")) return; // créatif = ignorer
        double max = maxCharge(stack);
        tag.putDouble("Energy", Math.max(0, Math.min(amount, max)));
    }

    @Override
    public boolean canExtract(ItemStack stack) {
        return true; // créatif = toujours possible
    }

    @Override
    public boolean canCharge(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        return tag.contains("Energy") && getCharge(stack) < maxCharge(stack);
    }

    @Override
    public double charge(ItemStack stack, double amount, boolean simulate) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains("Energy")) return 0; // créatif = pas besoin
        double energy = getCharge(stack);
        double max = maxCharge(stack);
        double toAdd = Math.min(amount, max - energy);
        if (!simulate && toAdd > 0) {
            setCharge(stack, energy + toAdd);
        }
        return toAdd;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        return tag.contains("Energy"); // créatif = pas de barre
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        double energy = getCharge(stack);
        double max = maxCharge(stack);
        return 1d - energy/max;
    }
}

