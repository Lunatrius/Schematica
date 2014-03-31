package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.client.Events;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicChunk;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicGlobal;
import com.github.lunatrius.schematica.config.Config;
import com.github.lunatrius.schematica.lib.Reference;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.lang.reflect.Field;

@Mod(modid = "Schematica")
public class Schematica {
	private final Settings settings = Settings.instance;
	private final Profiler profiler = this.settings.minecraft.mcProfiler;
	private int ticks = -1;

	@Instance("Schematica")
	public static Schematica instance;

	private Field sortedWorldRenderers = null;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		File suggestedConfigurationFile = event.getSuggestedConfigurationFile();

		Reference.logger = event.getModLog();

		Reference.config = new Config(suggestedConfigurationFile);
		Reference.config.save();

		for (KeyBinding keyBinding : this.settings.keyBindings) {
			ClientRegistry.registerKeyBinding(keyBinding);
		}

		if (!Settings.SCHEMATIC_DIRECTORY.exists()) {
			if (!Settings.SCHEMATIC_DIRECTORY.mkdirs()) {
				Reference.logger.info("Could not create schematic directory!");
			}
		}

		if (!Settings.TEXTURE_DIRECTORY.exists()) {
			if (!Settings.TEXTURE_DIRECTORY.mkdirs()) {
				Reference.logger.info("Could not create texture directory!");
			}
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		try {
			MinecraftForge.EVENT_BUS.register(new RendererSchematicGlobal());
			MinecraftForge.EVENT_BUS.register(new ChatEventHandler());

			FMLCommonHandler.instance().bus().register(new Events());

			this.sortedWorldRenderers = ReflectionHelper.findField(RenderGlobal.class, "n", "field_72768_k", "sortedWorldRenderers");
		} catch (Exception e) {
			Reference.logger.fatal("Could not initialize the mod!", e);
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	public void keyboardEvent(KeyBinding keyBinding) {
		this.settings.keyboardEvent(keyBinding);
	}

	public boolean onTick() {
		this.profiler.startSection("schematica");
		if (this.settings.minecraft.thePlayer != null && this.settings.isRenderingSchematic && this.settings.schematic != null) {
			this.profiler.startSection("printer");
			if (this.settings.isPrinterEnabled && this.settings.isPrinting && this.ticks-- < 0) {
				this.ticks = Reference.config.placeDelay;

				SchematicPrinter.INSTANCE.print();
			}

			this.profiler.endStartSection("checkDirty");
			checkDirty();

			this.profiler.endStartSection("canUpdate");
			RendererSchematicChunk.setCanUpdate(true);

			this.profiler.endSection();
		} else if (this.settings.minecraft.thePlayer == null) {
			this.settings.reset();
		}
		this.profiler.endSection();

		return true;
	}

	private void checkDirty() {
		if (this.sortedWorldRenderers != null) {
			try {
				WorldRenderer[] renderers = (WorldRenderer[]) this.sortedWorldRenderers.get(this.settings.minecraft.renderGlobal);
				if (renderers != null) {
					int count = 0;
					for (WorldRenderer worldRenderer : renderers) {
						if (worldRenderer != null && worldRenderer.needsUpdate && count++ < 125) {
							AxisAlignedBB worldRendererBoundingBox = worldRenderer.rendererBoundingBox.getOffsetBoundingBox(-this.settings.offset.x, -this.settings.offset.y, -this.settings.offset.z);
							for (RendererSchematicChunk renderer : this.settings.sortedRendererSchematicChunk) {
								if (!renderer.getDirty() && renderer.getBoundingBox().intersectsWith(worldRendererBoundingBox)) {
									renderer.setDirty();
								}
							}
						}
					}
				}
			} catch (Exception e) {
				Reference.logger.error("Dirty check failed!", e);
			}
		}
	}
}
