package ayral.gml.item;

import ayral.gml.NetworkHandler;
import ayral.gml.OpenStuffMod;
import ayral.gml.model.ConduitComponentRender;
import ayral.gml.model.OpenArmorModel;
import ayral.gml.network.OpenTabletGuiPacket;
import li.cil.oc.Settings;
import li.cil.oc.api.CreativeTab;
import li.cil.oc.common.Tier;
import li.cil.oc.common.item.Tablet;
import li.cil.oc.common.item.TabletWrapper;
import li.cil.oc.common.item.data.TabletData;
import li.cil.oc.common.item.traits.Chargeable;
import li.cil.oc.server.machine.luac.LuaStateFactory;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.ConduitTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OpenArmorItem extends DyeableArmorItem implements Chargeable {
    static final int DEFAULT_COLOR = 0x00FF00;

    public OpenArmorItem(EquipmentSlotType slot) {
        super(OpenArmorMaterial.OPEN_ARMOR_MATERIAL, slot, new Item.Properties().tab(CreativeTab.instance)
                .fireResistant());
    }

    public static boolean isWearingFullSet(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!(stack.getItem() instanceof OpenArmorItem)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isRunning(ItemStack itemStack){
        return itemStack.getOrCreateTag().getBoolean("is_armor_running");
    }

    // ------------------------ Armor Update  ---------------------------

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClientSide || slot != EquipmentSlotType.CHEST) return;
        if (player.isInWater()) this.armorSwamTick(stack,world,player);
        CompoundNBT tag = stack.getOrCreateTag();

        ensureTablet(tag);
        ItemStack tabletStack = ItemStack.of(tag.getCompound("Tablet"));
        rechargeTabletFromArmor(tag, tabletStack);

        TabletWrapper tablet = Tablet.get(tabletStack, player);

        tablet.connectComponents();
        tablet.update(world, player, -1, false);
        tag.putBoolean("is_armor_running",tablet.machine().isRunning());
        tag.put("Tablet", tabletStack.save(new CompoundNBT()));

        uniformizeArmorEnergy(player);
    }

    public static void ensureTablet(CompoundNBT tag) {
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

    // ------------------------ Disable Armor when not used -------------

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        stack.getOrCreateTag().putBoolean("is_armor_running",false);
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public void inventoryTick(ItemStack stack,World worldIn,Entity entity,int itemSlot,boolean isSelected) {
        if (!(entity instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) entity;
        if(player.getItemBySlot(((OpenArmorItem) stack.getItem()).slot) == stack) return;
        stack.getOrCreateTag().putBoolean("is_armor_running",false);
    }

    // ------------------------ Rendering logic  ------------------------

    @Override
    public int getColor(ItemStack stack) {
        CompoundNBT nbt = stack.getTagElement("display");
        if (nbt != null && nbt.contains("color", 99)) {
            return nbt.getInt("color");
        }
        return DEFAULT_COLOR;
    }

    @Override
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        int color = this.getColor(itemStack);

        if (isWearingFullSet(entityLiving)) {
            if (armorSlot != EquipmentSlotType.CHEST) {
                ItemStack chestStack = entityLiving.getItemBySlot(EquipmentSlotType.CHEST);
                itemStack.getOrCreateTag().putBoolean("is_armor_running",chestStack.getOrCreateTag().getBoolean("is_armor_running"));
                color = this.getColor(chestStack);
                this.setColor(itemStack,color);
            }
        }else{
            itemStack.getOrCreateTag().putBoolean("is_armor_running",false);
        }

        boolean runing = isRunning(itemStack);
        return new OpenArmorModel(1.0F, armorSlot, entityLiving, color, runing);
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

    // ------------------------ Fly logic -------------------------------

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return isFlyingEnable(stack) && isRunning(stack);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (entity.isCrouching()) {
            FireworkRocketEntity rocket = new FireworkRocketEntity(entity.level, stack, entity);
            rocket.tick();
        }
        return true;
    }


    public static boolean setFlightEnabled(ItemStack stack, boolean b) {
        CompoundNBT root = stack.getOrCreateTag();
        CompoundNBT feature = root.getCompound("feature");
        feature.putBoolean("flying", b);
        root.put("feature", feature);
        stack.setTag(root);
        return true;
    }

    public static boolean isFlyingEnable(ItemStack stack) {
        CompoundNBT nbt = stack.getTagElement("feature");
        if (nbt != null) {
            return nbt.getBoolean("flying");
        }
        return false;
    }

    // ------------------------ Swim logic -----------------------------

    private void armorSwamTick(ItemStack stack, World world, PlayerEntity player){
        if (! isSwimmingEnable(stack)) return;
        player.addEffect(new EffectInstance(Effects.CONDUIT_POWER, 260, 0, true, true));
    }


    public static boolean setSwamEnabled(ItemStack stack, boolean b) {
        CompoundNBT root = stack.getOrCreateTag();
        CompoundNBT feature = root.getCompound("feature");
        feature.putBoolean("swimming", b);
        root.put("feature", feature);
        stack.setTag(root);
        return true;
    }

    public static boolean isSwimmingEnable(ItemStack stack) {
        CompoundNBT nbt = stack.getTagElement("feature");
        if (nbt != null) {
            return nbt.getBoolean("swimming");
        }
        return false;
    }

    // ------------------------ GUI logic  ------------------------

    public static void openTabletGuiFromArmor(PlayerEntity player) {
        if (!(player.level.isClientSide)) return;

        ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chestStack.isEmpty() || !(chestStack.getItem() instanceof OpenArmorItem)) return;

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

        items[30] = new ItemStack(Items.NETHERITE_CHESTPLATE);
        items[20] = new ItemStack(OpenStuffItems.FLYING_UPGRADE.get());

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

