package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.state.IBlockState;

public interface IExtraClick {
    int getExtraClicks(IBlockState blockState);
}
