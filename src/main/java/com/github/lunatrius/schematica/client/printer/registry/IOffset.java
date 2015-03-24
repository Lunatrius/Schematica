package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.state.IBlockState;

public interface IOffset {
    float getOffset(IBlockState blockState);
}
