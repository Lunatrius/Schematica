package com.github.lunatrius.schematica.nbt;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public class NBTConversionException extends Exception {
    public NBTConversionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NBTConversionException(final TileEntity tileEntity, final Throwable cause) {
        super(String.valueOf(tileEntity), cause);
    }

    public NBTConversionException(final Entity entity, final Throwable cause) {
        super(String.valueOf(entity), cause);
    }
}
