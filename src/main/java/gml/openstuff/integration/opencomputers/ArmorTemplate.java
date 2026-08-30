package gml.openstuff.integration.opencomputers;

import gml.openstuff.OpenStuff;
import gml.openstuff.data.MachineData;
import gml.openstuff.data.PieceData;
import li.cil.oc.Localization;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.common.item.data.TabletData;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Mainly inspired by
// https://github.com/PC-Logix/OpenComputers/blob/main-MC1.21.1/src/main/scala/li/cil/oc/common/template/AssemblerTemplates.scala
// https://github.com/PC-logix/OpenComputers/blob/main-MC1.21.1/src/main/scala/li/cil/oc/common/template/DroneTemplate.scala
// under MIT Licence

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

        int complexity = complexity(inventory);
        int maxComplexity = maxComplexity(inventory);

        MutableComponent progress = (!validate) ? Component.translatable("gui.Assembler.InsertTrim") :
                (MutableComponent) Localization.Assembler$.MODULE$.Complexity(complexity, maxComplexity);

        ArrayList<Component> warnings = new ArrayList<>();

        if(!hasScreen(inventory)){
            warnings.add(Localization.Assembler$.MODULE$.Warning("Screen"));
        }

        if(!hasKeyboard(inventory)){
            warnings.add(Localization.Assembler$.MODULE$.Warning("Keyboard"));
        }

        if(!warnings.isEmpty()) warnings.addFirst(Localization.Assembler$.MODULE$.Warnings());
        return new Object[]{validate, progress, warnings.toArray(new Component[0])};
    }

    public static Object[] validateChestplate(Container inventory) {
        boolean hasCPU = hasCPU(inventory);
        boolean hasRAM = hasRAM(inventory);
        boolean hasTrim = hasTrim(inventory);
        boolean requireRAM = requireRAM(inventory);

        int complexity = complexity(inventory);
        int maxComplexity = maxComplexity(inventory);

        boolean validate = hasTrim && hasCPU && (!requireRAM || hasRAM);

        Component progress = (!hasCPU) ? Localization.Assembler$.MODULE$.InsertCPU() :
                (!hasRAM && requireRAM) ? Localization.Assembler$.MODULE$.InsertRAM() :
                        (!hasTrim) ? Component.translatable("gui.Assembler.InsertTrim") :
                                Localization.Assembler$.MODULE$.Complexity(complexity, maxComplexity);

        ArrayList<Component> warnings = new ArrayList<>();

        //TODO: add suggestions for BIOS, HDD, FileSystem, GPU
        if(!warnings.isEmpty()) warnings.addFirst(Localization.Assembler$.MODULE$.Warnings());
        return new Object[]{validate, progress, warnings.toArray(new Component[0])};
    }

    public static Object[] validateLeggings(Container inventory) {
        boolean validate = hasTrim(inventory);
        int complexity = complexity(inventory);
        int maxComplexity = maxComplexity(inventory);
        Component progress = (!validate) ? Component.translatable("gui.Assembler.InsertTrim") :
                Localization.Assembler$.MODULE$.Complexity(complexity, maxComplexity);

        ArrayList<Component> warnings = new ArrayList<>();

        if(!warnings.isEmpty()) warnings.addFirst(Localization.Assembler$.MODULE$.Warnings());
        return new Object[]{validate, progress, warnings.toArray(new Component[0])};
    }

    public static Object[] validateBoots(Container inventory) {
        boolean validate = hasTrim(inventory);
        int complexity = complexity(inventory);
        int maxComplexity = maxComplexity(inventory);
        Component progress = (!validate) ? Component.translatable("gui.Assembler.InsertTrim") :
                Localization.Assembler$.MODULE$.Complexity(complexity, maxComplexity);

        ArrayList<Component> warnings = new ArrayList<>();

        if(!warnings.isEmpty()) warnings.addFirst(Localization.Assembler$.MODULE$.Warnings());
        return new Object[]{validate, progress, warnings.toArray(new Component[0])};
    }

    public static Object[] assemble(Container inventory) {
        assert ServerLifecycleHooks.getCurrentServer() != null;
        HolderLookup.Provider reg = ServerLifecycleHooks.getCurrentServer().registryAccess();

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

        PieceData data = new PieceData();
        for (int i = 1; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            data.items[i] = stack;
        }

        data.saveData(result);

        if(result.is(OpenStuff.OPEN_CHEST)){
            MachineData machine_data = new MachineData();

            machine_data.container = inventory.getItem(1);

            machine_data.saveData(result, reg);
        }

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

    private static <T> boolean hasDriver(Container inventory, Class<T> driverClass) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            Object driver = li.cil.oc.api.Driver.driverFor(stack, ArmorHost.class);
            if (driverClass.isInstance(driver)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCPU(Container inventory){
        return hasDriver(inventory, li.cil.oc.api.driver.item.Processor.class);
    }

    private static boolean hasRAM(Container inventory){
        return hasDriver(inventory, li.cil.oc.api.driver.item.Memory.class);
    }

    private static boolean hasTrim(Container inventory){
        return hasDriver(inventory, gml.openstuff.integration.opencomputers.TrimDriver.class);
    }

    private static boolean hasScreen(Container inventory){
        return hasDriver(inventory, li.cil.oc.integration.opencomputers.DriverScreen$.class);
    }

    private static boolean hasKeyboard(Container inventory){
        return hasDriver(inventory, li.cil.oc.integration.opencomputers.DriverKeyboard$.class);
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

    private static int complexity(Container inventory){
        int acc = 0;
        for (int slot = 1; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            DriverItem driver = Driver.driverFor(stack, ArmorHost.class);
            acc += (driver == null) ? 1 : 1 + driver.tier(stack);
        }
        return acc;
    }

    private static int maxComplexity(Container inventory){
        int acc = 100;
        // TODO : compute it
        return acc;
    }
}
