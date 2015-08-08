package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import java.util.ArrayList;
import java.util.List;

public class BlockList {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    public List<ItemStack> getList(final SchematicWorld world) {
        final List<ItemStack> blockList = new ArrayList<ItemStack>();

        if (world == null) {
            return blockList;
        }

        final MovingObjectPosition movingObjectPosition = new MovingObjectPosition(this.minecraft.thePlayer);

        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = 0; x < world.getWidth(); x++) {
                for (int z = 0; z < world.getLength(); z++) {
                    if (world.isRenderingLayer && y != world.renderingLayer) {
                        continue;
                    }

                    final Block block = world.getBlock(x, y, z);

                    if (block == Blocks.air || world.isAirBlock(x, y, z)) {
                        continue;
                    }

                    ItemStack stack = null;

                    try {
                        stack = block.getPickBlock(movingObjectPosition, world, x, y, z, this.minecraft.thePlayer);
                    } catch (final Exception e) {
                        Reference.logger.debug("Could not get the pick block for: {}", block, e);
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
                        Reference.logger.debug("Could not find the item for: {}", block);
                    }
                }
            }
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
