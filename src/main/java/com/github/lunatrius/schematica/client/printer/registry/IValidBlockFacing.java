package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import java.util.List;

public interface IValidBlockFacing {
    List<EnumFacing> getValidBlockFacings(List<EnumFacing> solidSides, IBlockState blockState);
}
