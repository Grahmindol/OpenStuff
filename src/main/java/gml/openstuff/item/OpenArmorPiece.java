package gml.openstuff.item;

import gml.openstuff.data.PieceData;
import li.cil.oc.common.datacomponents.OCComponents;
import li.cil.oc.common.datacomponents.OCComponents$;
import li.cil.oc.internal.scalalib.collection.immutable.Seq;
import li.cil.oc.internal.scalalib.collection.immutable.Seq$;
import li.cil.oc.util.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class OpenArmorPiece extends ArmorItem implements IItemExtension {
    public OpenArmorPiece(Type type, Properties properties) {
        super(ArmorMaterials.NETHERITE,
                type,
                properties.fireResistant()
                        .stacksTo(1)
                        .durability(type.getDurability(37))
        );
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        String path = stack.getItemHolder().getKey().location().getPath();
        Tooltip.add(tooltip, flag, path, Seq$.MODULE$.empty());

        if (Tooltip.showExtendedTooltip(flag)) {
            PieceData info = new PieceData(stack);
            ItemStack[] components = info.items;
            if (components.length > 1) {
                Tooltip.add(tooltip, flag, "server.Components", Seq$.MODULE$.empty());

                for(ItemStack component : components) {
                    if (component.isEmpty()) continue;
                    tooltip.add(Component.literal("- " + component.getHoverName().getString()).setStyle(Tooltip.DefaultStyle()));
                }
            }
        }

        String address = stack.get(OCComponents.ADDRESS());
        if(address != null) {
            String shortened = (address.length() > 13) ? address.substring(0, 13) + "..." : address;
            tooltip.add(Component.literal("§8" + shortened + "§7"));
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        PieceData info = new PieceData(stack);
        double ratio = info.energy / info.maxEnergy;
        return (int)Math.round(ratio * 13.0f);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return 0xff0000;
    }
}
