package ayral.gml.integration;

import ayral.gml.item.OpenStuffItems;
import li.cil.oc.common.item.data.TabletData;
import li.cil.oc.common.item.traits.Chargeable;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;

public class AssemblerCallbacks {

    public static boolean chest_select(ItemStack stack) {
        if (stack == null) return false;
        String itemName = stack.getItem().getRegistryName().toString();
        return itemName.equals("minecraft:netherite_chestplate");
    }

    public static Object[] chest_validate(IInventory inventory) {
        if (!containsItem(inventory, "opencomputers:tablet")) return new Object[]{false,new StringTextComponent( "Missing Tablet")};
        if (!containsItem(inventory, "opencomputers:batteryupgrade3")) return new Object[]{false, new StringTextComponent("Missing Battery Upgrade III")};
        return new Object[]{true};
    }

    public static Object[] chest_assemble(IInventory inventory) {
        ItemStack result = new ItemStack(OpenStuffItems.OPEN_CHESTPLATE.get()); // ex output

        TabletData data = new TabletData(inventory.getItem(17));

        ItemStack[] items = data.items();
        for (int i = 4; i <= 12; i++) {
            ItemStack stack = inventory.getItem(i);
            stack.getOrCreateTag().putBoolean("is_on_armor",true);
            items[i+16] = stack;
        }
        data.items_$eq(items);

        CompoundNBT tabletTag = new CompoundNBT();
        data.createItemStack().save(tabletTag);
        result.getOrCreateTag().put("Tablet", tabletTag);

        CompoundNBT tag = result.getOrCreateTag();


        //---------------------- Chargeable management ---------------------

        double max = 0;
        double current = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!(stack.getItem() instanceof Chargeable)) continue;

            Chargeable item = (Chargeable) stack.getItem();
            max += item.maxCharge(stack);
            current += item.getCharge(stack);
        }

        tag.putDouble("maxEnergy", max);
        tag.putDouble("Energy", current); // facultatif, tu peux initialiser à 0 si tu veux forcer la recharge
        result.setTag(tag);

        int cost = (int)(max - current); // coût = charge manquante
        return new Object[]{result, cost};
    }


    public static boolean select(ItemStack stack) {
        if (stack == null) return false;
        String itemName = stack.getItem().getRegistryName().toString();
        return itemName.equals("minecraft:netherite_helmet") ||
                itemName.equals("minecraft:netherite_leggings") ||
                itemName.equals("minecraft:netherite_boots");
    }


    public static Object[] validate(IInventory inventory) {
        if (!containsItem(inventory, "opencomputers:cpu3")) return new Object[]{false, new StringTextComponent("Missing CPU III")};
        if (!containsItem(inventory, "opencomputers:batteryupgrade3")) return new Object[]{false, new StringTextComponent("Missing Battery Upgrade III")};
        return new Object[]{true};
    }

    // Assemblage : retourne l’item résultant (ta custom armor)
    public static Object[] assemble(IInventory inventory) {
        String name = inventory.getItem(0).getItem().getRegistryName().toString();
        ItemStack result = null;
        if (name.equals("minecraft:netherite_helmet")) {
            result = new ItemStack(OpenStuffItems.OPEN_HELMET.get());
        } else if (name.equals("minecraft:netherite_leggings")) {
            result = new ItemStack(OpenStuffItems.OPEN_LEGGINGS.get());
        } else if (name.equals("minecraft:netherite_boots")) {
            result = new ItemStack(OpenStuffItems.OPEN_BOOTS.get());
        }
        double max = 0;
        double current = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!(stack.getItem() instanceof Chargeable)) continue;

            Chargeable item = (Chargeable) stack.getItem();
            max += item.maxCharge(stack);
            current += item.getCharge(stack);
        }

        CompoundNBT tag = result.getOrCreateTag();
        tag.putDouble("maxEnergy", max);
        tag.putDouble("Energy", current); // facultatif, tu peux initialiser à 0 si tu veux forcer la recharge
        result.setTag(tag);

        int cost = (int)(max - current); // coût = charge manquante
        return new Object[]{result, cost};
    }

    private static boolean containsItem(IInventory inv, String registryName) {
        for (int i = 1; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && registryName.equals(stack.getItem().getRegistryName().toString())) {
                return true;
            }
        }
        return false;
    }
}
