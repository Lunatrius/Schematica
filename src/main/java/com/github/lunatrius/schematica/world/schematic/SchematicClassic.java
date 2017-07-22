package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.reference.Names;

import net.minecraft.nbt.NBTTagCompound;

// TODO: http://minecraft.gamepedia.com/Data_values_%28Classic%29
public class SchematicClassic extends SchematicFormat {
    @Override
    public ISchematic readFromNBT(final NBTTagCompound tagCompound) {
        // TODO
        return null;
    }

    @Override
    public boolean writeToNBT(final NBTTagCompound tagCompound, final ISchematic schematic) {
        // TODO
        return false;
    }

    @Override
    public String getName() {
        return Names.Formats.CLASSIC;
    }

    @Override
    public String getExtension() {
        return Names.Extensions.SCHEMATIC;
    }
}
