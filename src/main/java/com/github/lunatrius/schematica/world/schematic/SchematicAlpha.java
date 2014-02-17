package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.SchematicWorld;
import net.minecraft.nbt.NBTTagCompound;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SchematicAlpha extends SchematicFormat {
	@Override
	public SchematicWorld readFromNBT(NBTTagCompound tagCompound) {
		throw new NotImplementedException();
	}

	@Override
	public boolean writeToNBT(NBTTagCompound tagCompound, SchematicWorld world) {
		throw new NotImplementedException();
	}
}
