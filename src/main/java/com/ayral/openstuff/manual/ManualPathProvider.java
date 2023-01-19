package com.ayral.openstuff.manual;

import li.cil.oc.api.manual.PathProvider;
import li.cil.oc.api.prefab.TextureTabIconRenderer;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

public class ManualPathProvider implements PathProvider {
    void initialize(ResourceLocation iconResourceLocation, String tooltip, String path) {
        if(FMLCommonHandler.instance().getEffectiveSide().equals(Side.CLIENT)) {
            li.cil.oc.api.Manual.addProvider(new ManualPathProvider());
            li.cil.oc.api.Manual.addProvider(new ManualContentProvider());
            li.cil.oc.api.Manual.addTab(new TextureTabIconRenderer(iconResourceLocation), tooltip, path);
        }
    }

    @Nullable
    public String pathFor(ItemStack stack) {
        if(stack == null) return null;

        if(stack.getItem() instanceof IItemWithDocumentation) {
            return ((IItemWithDocumentation) stack.getItem()).getDocumentationName(stack);
        }
        if(stack.getItem() instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(stack.getItem());
            if(block instanceof IBlockWithDocumentation) {
                return ((IBlockWithDocumentation) block).getDocumentationName(stack);
            }
        }
        return null;
    }

    @Nullable
    public String pathFor(World world, BlockPos pos) {
        if(world == null) return null;

        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof IBlockWithDocumentation) {
            return ((IBlockWithDocumentation) block).getDocumentationName(world, pos);
        }
        return null;
    }
}
