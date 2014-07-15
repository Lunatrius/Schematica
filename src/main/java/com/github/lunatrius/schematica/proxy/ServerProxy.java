package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.File;

public class ServerProxy extends CommonProxy {
	@Override
	public void setConfigEntryClasses() {
	}

	@Override
	public void registerKeybindings() {
	}

	@Override
	public void registerEvents() {
	}

	@Override
	public File getDataDirectory() {
		return MinecraftServer.getServer().getFile(".");
	}

	@Override
	public void setActiveSchematic(SchematicWorld world) {
	}

	@Override
	public void setActiveSchematic(SchematicWorld world, EntityPlayer player) {
	}

	@Override
	public SchematicWorld getActiveSchematic() {
		return null;
	}

	@Override
	public SchematicWorld getActiveSchematic(EntityPlayer player) {
		return null;
	}
}
