package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BlockList {
    private static final Comparator<ItemStack> BLOCK_COMPARATOR = new Comparator<ItemStack>() {
        @Override
        public int compare(final ItemStack itemStackA, final ItemStack itemStackB) {
            final String nameA = itemStackA.getItem().getItemStackDisplayName(itemStackA);
            final String nameB = itemStackB.getItem().getItemStackDisplayName(itemStackB);

            return nameA.compareTo(nameB);
        }
    };

    private final Minecraft minecraft = Minecraft.getMinecraft();

    public List<ItemStack> getList(final SchematicWorld world) {
        final List<ItemStack> blockList = new ArrayList<ItemStack>();

        if (world == null) {
            return blockList;
        }

        final MovingObjectPosition movingObjectPosition = new MovingObjectPosition(this.minecraft.thePlayer);

        for (final MBlockPos pos : MBlockPos.getAllInRange(BlockPos.ORIGIN, new BlockPos(world.getWidth() - 1, world.getHeight() - 1, world.getLength() - 1))) {
            if (world.isRenderingLayer && pos.getY() != world.renderingLayer) {
                continue;
            }

            final IBlockState blockState = world.getBlockState(pos);
            final Block block = blockState.getBlock();

            if (block == Blocks.air || world.isAirBlock(pos)) {
                continue;
            }

            ItemStack stack = null;

            try {
                stack = block.getPickBlock(movingObjectPosition, world, pos);
            } catch (final Exception e) {
                Reference.logger.debug("Could not get the pick block for: {}", blockState, e);
            }

            if (stack != null && stack.getItem() != null) {
                final ItemStack itemStack = findItemStack(blockList, stack);
                if (itemStack != null) {
                    itemStack.stackSize++;
                } else {
                    final ItemStack stackCopy = stack.copy();
                    stackCopy.stackSize = 1;
                    blockList.add(stackCopy);
                }
            } else {
                Reference.logger.debug("Could not find the item for: {}", blockState);
            }
        }

        try {
            Collections.sort(blockList, BLOCK_COMPARATOR);
        } catch (final Exception e) {
            Reference.logger.error("Could not sort the block list", e);
        }

        return blockList;
    }

    private ItemStack findItemStack(final List<ItemStack> blockList, final ItemStack pickBlock) {
        for (final ItemStack itemStack : blockList) {
            if (itemStack.isItemEqual(pickBlock)) {
                return itemStack;
            }
        }

        return null;
    }
}
