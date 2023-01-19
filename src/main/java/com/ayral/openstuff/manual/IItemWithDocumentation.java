package com.ayral.openstuff.manual;

import net.minecraft.item.ItemStack;

public interface IItemWithDocumentation {
    public String getDocumentationName(ItemStack stack);
}
