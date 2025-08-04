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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class OpenStuffArmorItem extends DyeableArmorItem implements Chargeable {
    public int light_color = 0xFFFFFF;
    public boolean should_running = false;
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

    public static void start_computer(ItemStack stack, PlayerEntity player){
        if (!(stack.getItem() instanceof OpenStuffArmorItem)) return;
        ((OpenStuffArmorItem) stack.getItem()).should_running = isWearingFullSet(player);
    }

    public static void stop_computer(ItemStack stack, PlayerEntity player){
        if (!(stack.getItem() instanceof OpenStuffArmorItem)) return;
        ((OpenStuffArmorItem) stack.getItem()).should_running = false;
    }

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClientSide) return;
        if (slot != EquipmentSlotType.CHEST) return;

        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains("Tablet")) {
            ItemStack tabletStack = createConfiguredTablet();
            CompoundNBT tabletTag = new CompoundNBT();
            tabletStack.save(tabletTag);
            tag.put("Tablet", tabletTag);
            stack.setTag(tag); // 🧠 obligatoire pour que les modifs soient prises
        }

        ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));

        // Recharge de la tablette à partir de la plastron
        if (tabletStack.getItem() instanceof Chargeable) {
            Chargeable tabletItem = (Chargeable) tabletStack.getItem();
            double tabletMax = tabletItem.maxCharge(tabletStack);
            double tabletCharge = tabletItem.getCharge(tabletStack);

            if (tabletCharge < tabletMax) {
                double missing = tabletMax - tabletCharge;

                if (!tag.contains("Energy")) {
                    tabletItem.setCharge(tabletStack, tabletMax); // full charge gratuite
                } else {
                    double chestEnergy = tag.getDouble("Energy");
                    double transfer = Math.min(chestEnergy, missing);

                    tabletItem.setCharge(tabletStack, tabletCharge + transfer);
                    tag.putDouble("Energy", chestEnergy - transfer); // maj énergie plastron
                }
            }
        }

        TabletWrapper tablet = Tablet.get(tabletStack, player);

        if (this.armorComponent == null) {
            this.armorComponent = new ArmorComponent(player);
            tablet.connectItemNode(this.armorComponent.node());
        }

        tablet.update(world, player, -1, false);
        tag.put("Tablet", tabletStack.save(new CompoundNBT())); // maj après update

        // Uniformisation de l'énergie entre les pièces d'armure OpenStuff
        Iterator<ItemStack> armor = player.getArmorSlots().iterator();

        List<ItemStack> openStuffPieces = new ArrayList<>();
        double totalEnergy = 0;
        double totalMax = 0;

        for (Iterator<ItemStack> it = armor; it.hasNext(); ) {
            ItemStack piece = it.next();
            if (piece.getItem() instanceof Chargeable) {
                Chargeable c = (Chargeable) piece.getItem();
                double max = c.maxCharge(piece);
                double charge = c.getCharge(piece);
                totalEnergy += charge;
                totalMax += max;
                openStuffPieces.add(piece);
            }
        }

        // Redistribution si au moins 2 pièces
        if (openStuffPieces.size() > 1 && totalMax > 0) {
            double uniformRatio = totalEnergy / totalMax;
            for (ItemStack piece : openStuffPieces) {
                Chargeable c = (Chargeable) piece.getItem();
                double max = c.maxCharge(piece);
                c.setCharge(piece, max * uniformRatio);
            }
        }
        // tablet logic ----------------------

        if (tablet.machine() != null) {
            if (!tablet.machine().isRunning() && should_running){
                tablet.connectComponents();
                tablet.machine().start();
            }
            else if (tablet.machine().isRunning() && !should_running)
                tablet.machine().stop();
        }
        if (!tablet.machine().isRunning()) return;
        tablet.update(world, player, -1, false);
        tag.put("Tablet", tabletStack.save(new CompoundNBT())); // maj après update
    }

    // ------------------------ Rendering logic  ------------------------

    @Override
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        int color = 0x010101;

        if (isWearingFullSet(entityLiving)) {
            if (armorSlot == EquipmentSlotType.CHEST && this.should_running) {
                color = this.light_color;
            } else {
                ItemStack chestStack = entityLiving.getItemBySlot(EquipmentSlotType.CHEST);
                if (((OpenStuffArmorItem) chestStack.getItem()).should_running ) {
                    color = ((OpenStuffArmorItem) chestStack.getItem()).light_color;
                    this.should_running = true;
                }
            }
        }else if (armorSlot == EquipmentSlotType.CHEST){
            //this.should_running = false;
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

        CompoundNBT tag = chestStack.getOrCreateTag();
        if (!tag.contains("Tablet")) return;

        ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
        if (!(tabletStack.getItem() instanceof Tablet)) return;

        // 📦 Récupère l'ordinateur virtuel
        TabletWrapper tablet = Tablet.get(tabletStack, player);
        tablet.connectComponents();
        if (tablet == null) return;

        if (player.isCrouching()){
            stop_computer(chestStack,player);
            NetworkHandler.INSTANCE.sendToServer(new OpenTabletGuiPacket());
        }else{
            start_computer(chestStack,player);
            Object[] comps = (Object[]) tablet.components();
            for (Object opt : comps) {
                if (opt instanceof scala.Option) {
                    scala.Option<?> some = (scala.Option<?>) opt;
                    if (some.isDefined()) {
                        Object value = some.get();
                        if (value instanceof TextBuffer) {
                            TextBuffer buffer = (TextBuffer) value;
                            // 👍 Appelle le GUI ici
                            showGui(buffer);
                            break;
                        }
                    }
                }
            }
        }
    }


    @OnlyIn(Dist.CLIENT)
    private static void showGui(TextBuffer buffer) {
        Minecraft.getInstance().pushGuiLayer(new Screen(buffer, true, () -> true, buffer::isRenderingEnabled));
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

