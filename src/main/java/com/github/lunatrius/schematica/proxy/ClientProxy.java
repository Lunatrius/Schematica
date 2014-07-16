package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.handler.client.ChatEventHandler;
import com.github.lunatrius.schematica.handler.client.KeyInputHandler;
import com.github.lunatrius.schematica.handler.client.TickHandler;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

public class ClientProxy extends CommonProxy {
	private SchematicWorld schematicWorld = null;
	public static boolean isPendingReset = false;
	public static RendererSchematicGlobal rendererSchematicGlobal = null;

	@Override
	public void setConfigEntryClasses() {
		ConfigurationHandler.propAlpha.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
		ConfigurationHandler.propBlockDelta.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
		ConfigurationHandler.propPlaceDelay.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
		ConfigurationHandler.propTimeout.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
	}

	@Override
	public void registerKeybindings() {
		for (KeyBinding keyBinding : KeyInputHandler.KEY_BINDINGS) {
			ClientRegistry.registerKeyBinding(keyBinding);
		}
	}

	@Override
	public void registerEvents() {
		FMLCommonHandler.instance().bus().register(new KeyInputHandler());
		FMLCommonHandler.instance().bus().register(new TickHandler());
		FMLCommonHandler.instance().bus().register(new ConfigurationHandler());

		rendererSchematicGlobal = new RendererSchematicGlobal();
		MinecraftForge.EVENT_BUS.register(rendererSchematicGlobal);
		MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
	}

	@Override
	public File getDataDirectory() {
		return Minecraft.getMinecraft().mcDataDir;
	}

	@Override
	public void resetSettings() {
		super.resetSettings();

		ChatEventHandler.chatLines = 0;

		SchematicPrinter.INSTANCE.setEnabled(true);
		SchematicPrinter.INSTANCE.setSchematic(null);

		ClientProxy.rendererSchematicGlobal.destroyRendererSchematicChunks();

		setActiveSchematic(null);
	}

	@Override
	public boolean saveSchematic(EntityPlayer player, File directory, String filename, World world, Vector3f from, Vector3f to) {
		try {
			String iconName = "";

			try {
				String[] parts = filename.split(";");
				if (parts.length == 2) {
					iconName = parts[0];
					filename = parts[1];
				}
			} catch (Exception e) {
				Reference.logger.error("Failed to parse icon data!", e);
			}

			SchematicWorld schematic = getSchematicFromWorld(world, from, to);
			schematic.setIcon(SchematicWorld.getIconFromName(iconName));
			SchematicFormat.writeToFile(directory, filename, schematic);

			return true;
		} catch (Exception e) {
			Reference.logger.error("Failed to save schematic!", e);
		}
		return false;
	}

	@Override
	public boolean loadSchematic(EntityPlayer player, File directory, String filename) {
		SchematicWorld schematic = SchematicFormat.readFromFile(directory, filename);
		if (schematic == null) {
			return false;
		}

		Reference.logger.info(String.format("Loaded %s [w:%d,h:%d,l:%d]", filename, schematic.getWidth(), schematic.getHeight(), schematic.getLength()));

		Schematica.proxy.setActiveSchematic(schematic);
		rendererSchematicGlobal.createRendererSchematicChunks(schematic);
		SchematicPrinter.INSTANCE.setSchematic(schematic);
		schematic.setRendering(true);

		return true;
	}

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
