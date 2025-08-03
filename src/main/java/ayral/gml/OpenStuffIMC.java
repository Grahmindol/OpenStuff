package ayral.gml;

import ayral.gml.integration.ArmorHost;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.common.Tier;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fml.InterModComms;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

import static li.cil.oc.api.IMC.registerAssemblerFilter;
import static li.cil.oc.api.IMC.registerAssemblerTemplate;

public class OpenStuffIMC {
    private static final String MOD_ID = "opencomputers";

    public static void registerArmorAssembler() {

        registerAssemblerTemplate("Netherite Chest To OpenStuff Armor Template",
                "ayral.gml.integration.AssemblerCallbacks.chest_select",
                "ayral.gml.integration.AssemblerCallbacks.chest_validate",
                "ayral.gml.integration.AssemblerCallbacks.chest_assemble",
                ArmorHost.class,
                null,
                new int[]{3, 3, 3},
                Arrays.asList(
                        null,null,null,null,
                        Pair.of("tablet",Tier.Two())
                ));

        registerAssemblerTemplate("Netherite Armor To OpenStuff Armor Template",
                "ayral.gml.integration.AssemblerCallbacks.select",
                "ayral.gml.integration.AssemblerCallbacks.validate",
                "ayral.gml.integration.AssemblerCallbacks.assemble",
                ArmorHost.class,
                null,
                new int[]{3,3,3},
                Arrays.asList(
                        null,null,null,null,
                        Pair.of("cpu",Tier.Three())
                ));
    }
}
