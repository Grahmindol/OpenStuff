package ayral.gml.item;

import ayral.gml.OpenStuffMod;
import ayral.gml.model.OpenArmorModel;
import li.cil.oc.Settings;
import li.cil.oc.api.CreativeTab;
import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.client.gui.Screen;
import li.cil.oc.common.Tier;
import li.cil.oc.common.item.Tablet;
import li.cil.oc.common.item.TabletWrapper;
import li.cil.oc.common.item.data.TabletData;
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
import java.util.Arrays;

public class OpenStuffArmorItem extends DyeableArmorItem {
    public int light_color = 0xFFFFFF;


    public OpenStuffArmorItem(EquipmentSlotType slot) {
        super(OpenStuffArmorMaterial.OPEN_ARMOR_MATERIAL, slot, new Item.Properties().tab(CreativeTab.instance));
    }

    private boolean isWearingFullSet(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!(stack.getItem() instanceof OpenStuffArmorItem)) {
                return false;
            }
        }
        return true;
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
        TabletWrapper tablet = Tablet.get(tabletStack, player);

        tablet.connectComponents();
        if (tablet.machine() != null){
            if (!tablet.machine().isRunning()) tablet.machine().start();
        }

        tablet.update(world, player, -1, false);
        tag.put("Tablet", tabletStack.save(new CompoundNBT())); // maj après update
    }




    @Nullable
    @Override
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        int color = isWearingFullSet(entityLiving) ? this.light_color : 0;
        return new OpenArmorModel(1.0F, armorSlot, entityLiving,color);
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



        if (tablet != null) {
            Object[] comps = (Object[]) tablet.components(); // scala.Array<Object>
            System.out.println("Composants : " + Arrays.toString(comps));
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



    private static ItemStack safeGetStack(String name) {
        ItemStack stack = li.cil.oc.common.init.Items.get(name).createItemStack(1);
        if (stack == null) return ItemStack.EMPTY;
        return stack;
    }

    public static ItemStack createConfiguredTablet() {
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
}

