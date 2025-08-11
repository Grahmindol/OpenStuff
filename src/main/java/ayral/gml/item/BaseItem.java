package ayral.gml.item;

import li.cil.oc.api.CreativeTab;
import li.cil.oc.client.KeyBindings;
import li.cil.oc.client.gui.Screen;
import li.cil.oc.util.Tooltip;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import java.util.List;

public class BaseItem extends Item {
    private final String name;
    public BaseItem(String name) {
        super(new Item.Properties().tab(CreativeTab.instance));
        this.name = name;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if(Screen.hasShiftDown()) {
            tooltip.add(new TranslationTextComponent("tooltip.openstuff." + name).withStyle(Tooltip.DefaultStyle()));
        } else {
            String txt = "§7"+new TranslationTextComponent("oc:tooltip.toolong", KeyBindings.getKeyBindingName(KeyBindings.extendedTooltip())).getString();
            tooltip.add(ITextComponent.nullToEmpty(txt));
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
