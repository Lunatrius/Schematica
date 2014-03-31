package com.github.lunatrius.schematica.client;

import com.github.lunatrius.schematica.CommonProxy;
import com.github.lunatrius.schematica.SchematicWorld;
import net.minecraft.entity.player.EntityPlayer;

public class ClientProxy extends CommonProxy {
	private SchematicWorld schematicWorld = null;

	@Override
	public void setActiveSchematic(SchematicWorld world) {
		this.schematicWorld = world;
	}

	@Override
	public void setActiveSchematic(SchematicWorld world, EntityPlayer player) {
		setActiveSchematic(world);
	}

	@Override
	public SchematicWorld getActiveSchematic() {
		return this.schematicWorld;
	}

	@Override
	public SchematicWorld getActiveSchematic(EntityPlayer player) {
		return getActiveSchematic();
	}
}
