package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.handler.client.ChatEventHandler;
import com.github.lunatrius.schematica.handler.client.KeyInputHandler;
import com.github.lunatrius.schematica.handler.client.TickHandler;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
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

		setActiveSchematic(null);
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
