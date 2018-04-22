package com.github.lunatrius.schematica.client.util;

import com.github.lunatrius.core.entity.EntityHelper;
import com.github.lunatrius.core.util.math.BlockPosHelper;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class BlockList {
    public List<WrappedItemStack> getList(final EntityPlayer player, final SchematicWorld world, final World mcWorld) {
        final List<WrappedItemStack> blockList = new ArrayList<WrappedItemStack>();

        if (world == null) {
            return blockList;
        }

        final RayTraceResult rtr = new RayTraceResult(player);
        final MBlockPos mcPos = new MBlockPos();

        for (final MBlockPos pos : BlockPosHelper.getAllInBox(BlockPos.ORIGIN, new BlockPos(world.getWidth() - 1, world.getHeight() - 1, world.getLength() - 1))) {
            if (!world.layerMode.shouldUseLayer(world, pos.getY())) {
                continue;
            }

            final IBlockState blockState = world.getBlockState(pos);
            final Block block = blockState.getBlock();

            if (block == Blocks.AIR || world.isAirBlock(pos)) {
                continue;
            }

            mcPos.set(world.position.add(pos));

            final IBlockState mcBlockState = mcWorld.getBlockState(mcPos);
            final boolean isPlaced = BlockStateHelper.areBlockStatesEqual(blockState, mcBlockState);

            ItemStack stack = ItemStack.EMPTY;

            try {
                stack = block.getPickBlock(blockState, rtr, world, pos, player);
            } catch (final Exception e) {
                Reference.logger.warn("Could not get the pick block for: {}", blockState, e);
            }

            if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
                final IFluidHandler fluidHandler = FluidUtil.getFluidHandler(world, pos, null);
                final FluidActionResult fluidActionResult = FluidUtil.tryFillContainer(new ItemStack(Items.BUCKET), fluidHandler, 1000, null, false);
                if (fluidActionResult.isSuccess()) {
                    final ItemStack result = fluidActionResult.getResult();
                    if (!result.isEmpty()) {
                        stack = result;
                    }
                }
            }

            if (stack == null) {
                Reference.logger.error("Could not find the item for: {} (getPickBlock() returned null, this is a bug)", blockState);
                continue;
            }

            if (stack.isEmpty()) {
                Reference.logger.warn("Could not find the item for: {}", blockState);
                continue;
            }

            int count = 1;

            // TODO: this has to be generalized for all blocks; just a temporary "fix"
            if (block instanceof BlockSlab) {
                if (((BlockSlab) block).isDouble()) {
                    count = 2;
                }
            }

            final WrappedItemStack wrappedItemStack = findOrCreateWrappedItemStackFor(blockList, stack);
            if (isPlaced) {
                wrappedItemStack.placed += count;
            }
            wrappedItemStack.total += count;
        }

        for (WrappedItemStack wrappedItemStack : blockList) {
            if (player.capabilities.isCreativeMode) {
                wrappedItemStack.inventory = -1;
            } else {
                wrappedItemStack.inventory = EntityHelper.getItemCountInInventory(player.inventory, wrappedItemStack.itemStack.getItem(), wrappedItemStack.itemStack.getItemDamage());
            }
        }

        return blockList;
    }

    private WrappedItemStack findOrCreateWrappedItemStackFor(final List<WrappedItemStack> blockList, final ItemStack itemStack) {
        for (final WrappedItemStack wrappedItemStack : blockList) {
            if (wrappedItemStack.itemStack.isItemEqual(itemStack)) {
                return wrappedItemStack;
            }
        }

        final WrappedItemStack wrappedItemStack = new WrappedItemStack(itemStack.copy());
        blockList.add(wrappedItemStack);
        return wrappedItemStack;
    }

    public static class WrappedItemStack {
        public ItemStack itemStack;
        public int placed;
        public int total;
        public int inventory;

        public WrappedItemStack(final ItemStack itemStack) {
            this(itemStack, 0, 0);
        }

        public WrappedItemStack(final ItemStack itemStack, final int placed, final int total) {
            this.itemStack = itemStack;
            this.placed = placed;
            this.total = total;
        }

        public String getItemStackDisplayName() {
            return this.itemStack.getItem().getItemStackDisplayName(this.itemStack);
        }

        public String getFormattedAmount() {
            final char color = this.placed < this.total ? 'c' : 'a';
            return String.format("\u00a7%c%s\u00a7r/%s", color, getFormattedStackAmount(this.itemStack, this.placed), getFormattedStackAmount(itemStack, this.total));
        }

        public String getFormattedAmountMissing(final String strAvailable, final String strMissing) {
            final int need = this.total - (this.inventory + this.placed);
            if (this.inventory != -1 && need > 0) {
                return String.format("\u00a7c%s: %s", strMissing, getFormattedStackAmount(this.itemStack, need));
            } else {
                return String.format("\u00a7a%s", strAvailable);
            }
        }

        private static String getFormattedStackAmount(final ItemStack itemStack, final int amount) {
            final int stackSize = itemStack.getMaxStackSize();
            if (true /* amount < stackSize */) {
                return String.format("%d", amount);
            } else {
                final int amountStack = amount / stackSize;
                final int amountRemainder = amount % stackSize;
                return String.format("%d(%d:%d)", amount, amountStack, amountRemainder);
            }
        }
    }
}
