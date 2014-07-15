package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.File;

public abstract class CommonProxy {
	public abstract void setConfigEntryClasses();

	public abstract void registerKeybindings();

	public abstract void registerEvents();

	public void createFolders() {
		if (!ConfigurationHandler.schematicDirectory.exists()) {
			if (!ConfigurationHandler.schematicDirectory.mkdirs()) {
				Reference.logger.info("Could not create schematic directory [%s]!", ConfigurationHandler.schematicDirectory.getAbsolutePath());
			}
		}
	}

	public File getDataDirectory() {
		return MinecraftServer.getServer().getFile(".");
	}

	public abstract void setActiveSchematic(SchematicWorld world);

	public abstract void setActiveSchematic(SchematicWorld world, EntityPlayer player);

	public abstract SchematicWorld getActiveSchematic();

	public abstract SchematicWorld getActiveSchematic(EntityPlayer player);
}
