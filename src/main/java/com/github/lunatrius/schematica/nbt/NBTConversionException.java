package com.github.lunatrius.schematica.nbt;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public class NBTConversionException extends Exception {
    public NBTConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NBTConversionException(TileEntity tileEntity, Throwable cause) {
        super(String.valueOf(tileEntity), cause);
    }

    public NBTConversionException(Entity entity, Throwable cause) {
        super(String.valueOf(entity), cause);
    }
}
