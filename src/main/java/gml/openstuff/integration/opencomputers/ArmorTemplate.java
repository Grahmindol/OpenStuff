package gml.openstuff.integration.opencomputers;

import gml.openstuff.OpenStuff;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.common.item.data.TabletData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collections;

public class ArmorTemplate {

    public static boolean selectHelmet(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.NETHERITE_HELMET);
    }

    public static boolean selectChestplate(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.NETHERITE_CHESTPLATE);
    }

    public static boolean  selectLeggings(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.NETHERITE_LEGGINGS);
    }

    public static boolean  selectBoots(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.NETHERITE_BOOTS);
    }


    public static Object[] validateHelmet(Container inventory) {
        boolean validate = hasTrim(inventory);

        MutableComponent progress = (!validate) ? Component.translatable("gui.Assembler.InsertTrim") : null;

        MutableComponent[] warning = new MutableComponent[]{};

        //TODO: add suggestions for Screen and KeyBoard

        return new Object[]{validate, progress, warning};
    }

    public static Object[] validateChestplate(Container inventory) {
        boolean hasCPU = hasCPU(inventory);
        boolean hasRAM = hasRAM(inventory);
        boolean hasTrim = hasTrim(inventory);
        boolean requireRAM = requireRAM(inventory);

        boolean validate = hasTrim && hasCPU && (!requireRAM || hasRAM);

        MutableComponent progress = (!hasCPU) ? Component.translatable("gui.Assembler.InsertCPU") :
                (!hasRAM && requireRAM) ? Component.translatable("gui.Assembler.InsertRAM") :
                        (!hasTrim) ? Component.translatable("gui.Assembler.InsertTrim") : null;

        MutableComponent[] warning = new MutableComponent[]{};

        //TODO: add suggestions for BIOS, HDD, FileSystem, GPU

        return new Object[]{true};
    }

    public static Object[] validateLeggings(Container inventory) {
        boolean validate = hasTrim(inventory);
        MutableComponent progress = (!validate) ? Component.translatable("gui.Assembler.InsertTrim") : null;
        MutableComponent[] warning = new MutableComponent[]{};
        return new Object[]{validate, progress, warning};
    }

    public static Object[] validateBoots(Container inventory) {
        boolean validate = hasTrim(inventory);
        MutableComponent progress = (!validate) ? Component.translatable("gui.Assembler.InsertTrim") : null;
        MutableComponent[] warning = new MutableComponent[]{};
        return new Object[]{validate, progress, warning};
    }

    public static Object[] assemble(Container inventory) {
        ItemStack result = ItemStack.EMPTY;
        if(inventory.getItem(0).is(Items.NETHERITE_HELMET)){
            result = new ItemStack(OpenStuff.OPEN_HELMET);
        } else if(inventory.getItem(0).is(Items.NETHERITE_CHESTPLATE)){
            result = new ItemStack(OpenStuff.OPEN_CHEST);
        } else if(inventory.getItem(0).is(Items.NETHERITE_LEGGINGS)){
            result = new ItemStack(OpenStuff.OPEN_LEGS);
        } else if(inventory.getItem(0).is(Items.NETHERITE_BOOTS)){
            result = new ItemStack(OpenStuff.OPEN_BOOTS);
        }

        TabletData data = new TabletData();
        for (int i = 1; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            data.items()[i] = stack;
        }
        data.container_$eq(inventory.getItem(1));
        data.saveData(result);

        return new Object[]{result, 0};
    }

    public static void register(){
        // Helmet
        li.cil.oc.api.IMC.registerAssemblerTemplate(
                "Helmet Upgrade",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.selectHelmet",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.validateHelmet",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.assemble",
                ArmorHost.class,
                null,
                new int[]{
                        3,
                        2,
                        1
                },
                Collections.singletonList(
                        Pair.of("trim", Integer.MAX_VALUE)
                )
        );


        li.cil.oc.api.IMC.registerAssemblerTemplate(
                "Chestplate Upgrade",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.selectChestplate",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.validateChestplate",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.assemble",
                ArmorHost.class,
                new int[]{
                        3
                },
                new int[]{
                        3,
                        2,
                        1
                },
                Arrays.asList(
                        Pair.of("trim",Integer.MAX_VALUE),
                        Pair.of(Slot.Card,2),
                        null,
                        Pair.of(Slot.CPU,2),
                        Pair.of(Slot.Memory,2),
                        Pair.of(Slot.Memory,2),

                        Pair.of("eeprom",2),
                        Pair.of(Slot.HDD,2)
                )
        );

        li.cil.oc.api.IMC.registerAssemblerTemplate(
                "Leggings Upgrade",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.selectLeggings",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.validateLeggings",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.assemble",
                ArmorHost.class,
                null,
                new int[]{
                        3,
                        2,
                        1
                },
                Collections.singletonList(
                        Pair.of("trim", Integer.MAX_VALUE)
                )
        );

        li.cil.oc.api.IMC.registerAssemblerTemplate(
                "Boots Upgrade",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.selectBoots",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.validateBoots",
                "gml.openstuff.integration.opencomputers.ArmorTemplate.assemble",
                ArmorHost.class,
                null,
                new int[]{
                        3,
                        2,
                        1
                },
                Collections.singletonList(
                        Pair.of("trim", Integer.MAX_VALUE)
                )
        );
    }

    private static boolean hasCPU(Container inventory){
        for (int i = 0; i < inventory.getContainerSize(); i++){
            ItemStack stack = inventory.getItem(i);
            if (li.cil.oc.api.Driver.driverFor(stack, ArmorHost.class) instanceof li.cil.oc.api.driver.item.Processor)
                return true;
        }
        return false;
    }

    private static boolean hasRAM(Container inventory){
        for (int i = 0; i < inventory.getContainerSize(); i++){
            ItemStack stack = inventory.getItem(i);
            if (li.cil.oc.api.Driver.driverFor(stack, ArmorHost.class) instanceof li.cil.oc.api.driver.item.Memory)
                return true;
        }
        return false;
    }

    private static boolean hasTrim(Container inventory){
        for (int i = 0; i < inventory.getContainerSize(); i++){
            ItemStack stack = inventory.getItem(i);
            if (li.cil.oc.api.Driver.driverFor(stack, ArmorHost.class) instanceof TrimDriver)
                return true;
        }
        return false;
    }

    private static boolean requireRAM(Container inventory){
        for (int i = 0; i < inventory.getContainerSize(); i++){
            ItemStack stack = inventory.getItem(i);
            if (li.cil.oc.api.Driver.driverFor(stack, ArmorHost.class) instanceof li.cil.oc.api.driver.item.Processor driver){
                Class<? extends Architecture> architecture = driver.architecture(stack);
                return architecture == null || architecture.isAnnotationPresent(Architecture.NoMemoryRequirements.class);
            }
        }
        return false;
    }


}
