package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

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
	public boolean saveSchematic(EntityPlayer player, File directory, String filename, World world, Vector3i from, Vector3i to) {
		return false;
	}

	@Override
	public boolean loadSchematic(EntityPlayer player, File directory, String filename) {
		return false;
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
