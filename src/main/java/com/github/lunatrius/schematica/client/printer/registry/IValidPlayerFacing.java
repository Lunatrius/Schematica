package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IValidPlayerFacing {
    boolean isValid(IBlockState blockState, EntityPlayer player, BlockPos pos, World world);
}
