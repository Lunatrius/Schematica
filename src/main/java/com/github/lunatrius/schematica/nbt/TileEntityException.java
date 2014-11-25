package com.github.lunatrius.schematica.nbt;

import net.minecraft.tileentity.TileEntity;

public class TileEntityException extends Exception {
    public TileEntityException(TileEntity tileEntity, Throwable cause) {
        super(String.valueOf(tileEntity), cause);
    }
}
