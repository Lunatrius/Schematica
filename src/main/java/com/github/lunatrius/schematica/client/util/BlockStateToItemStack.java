package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class BlockStateToItemStack {
    public static ItemStack getItemStack(final IBlockState blockState, final MovingObjectPosition movingObjectPosition, final SchematicWorld world, final BlockPos pos) {
        final Block block = blockState.getBlock();

        try {
            final ItemStack itemStack = block.getPickBlock(movingObjectPosition, world, pos);
            if (itemStack != null) {
                return itemStack;
            }
        } catch (final Exception e) {
            Reference.logger.debug("Could not get the pick block for: {}", blockState, e);
        }

        return null;
    }
}
