package com.ayral.openstuff.manual;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockWithDocumentation {
    String getDocumentationName(World world, BlockPos pos);
    String getDocumentationName(ItemStack stack);
}
