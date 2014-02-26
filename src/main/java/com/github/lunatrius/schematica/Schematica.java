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
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

@Mod(modid = "Schematica")
public class Schematica {
	private static final FileFilterConfiguration FILE_FILTER_CONFIGURATION = new FileFilterConfiguration();
	private static final String DIR_ASSETS = "lunatrius/schematica/assets/";

	private final Settings settings = Settings.instance;
	private final Profiler profiler = this.settings.minecraft.mcProfiler;
	private final SchematicPrinter printer = new SchematicPrinter();
	private int ticks = -1;

	@Instance("Schematica")
	public static Schematica instance;

	private Field sortedWorldRenderers = null;
	private File configurationFolder = null;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		File suggestedConfigurationFile = event.getSuggestedConfigurationFile();

		Reference.logger = event.getModLog();

		Reference.config = new Config(suggestedConfigurationFile);
		Reference.config.save();

		this.configurationFolder = suggestedConfigurationFile.getParentFile();

		for (KeyBinding keyBinding : this.settings.keyBindings) {
			ClientRegistry.registerKeyBinding(keyBinding);
		}

		List<Block> blockListIgnoreID = SchematicWorld.blockListIgnoreID;
		blockListIgnoreID.add(Blocks.piston_extension);
		blockListIgnoreID.add(Blocks.piston_head);
		blockListIgnoreID.add(Blocks.portal);
		blockListIgnoreID.add(Blocks.end_portal);

		List<Block> blockListIgnoreMetadata = SchematicWorld.blockListIgnoreMetadata;
		blockListIgnoreMetadata.add(Blocks.water);
		blockListIgnoreMetadata.add(Blocks.flowing_water);
		blockListIgnoreMetadata.add(Blocks.lava);
		blockListIgnoreMetadata.add(Blocks.flowing_lava);
		blockListIgnoreMetadata.add(Blocks.dispenser);
		blockListIgnoreMetadata.add(Blocks.bed);
		blockListIgnoreMetadata.add(Blocks.golden_rail);
		blockListIgnoreMetadata.add(Blocks.detector_rail);
		blockListIgnoreMetadata.add(Blocks.sticky_piston);
		blockListIgnoreMetadata.add(Blocks.piston);
		blockListIgnoreMetadata.add(Blocks.stone_slab);
		blockListIgnoreMetadata.add(Blocks.torch);
		blockListIgnoreMetadata.add(Blocks.oak_stairs);
		blockListIgnoreMetadata.add(Blocks.chest);
		blockListIgnoreMetadata.add(Blocks.redstone_wire);
		blockListIgnoreMetadata.add(Blocks.wheat);
		blockListIgnoreMetadata.add(Blocks.farmland);
		blockListIgnoreMetadata.add(Blocks.furnace);
		blockListIgnoreMetadata.add(Blocks.lit_furnace);
		blockListIgnoreMetadata.add(Blocks.standing_sign);
		blockListIgnoreMetadata.add(Blocks.wooden_door);
		blockListIgnoreMetadata.add(Blocks.ladder);
		blockListIgnoreMetadata.add(Blocks.rail);
		blockListIgnoreMetadata.add(Blocks.stone_stairs);
		blockListIgnoreMetadata.add(Blocks.wall_sign);
		blockListIgnoreMetadata.add(Blocks.lever);
		blockListIgnoreMetadata.add(Blocks.stone_pressure_plate);
		blockListIgnoreMetadata.add(Blocks.iron_door);
		blockListIgnoreMetadata.add(Blocks.wooden_pressure_plate);
		blockListIgnoreMetadata.add(Blocks.redstone_torch);
		blockListIgnoreMetadata.add(Blocks.unlit_redstone_torch);
		blockListIgnoreMetadata.add(Blocks.stone_button);
		blockListIgnoreMetadata.add(Blocks.pumpkin);
		blockListIgnoreMetadata.add(Blocks.portal);
		blockListIgnoreMetadata.add(Blocks.lit_pumpkin);
		blockListIgnoreMetadata.add(Blocks.cake);
		blockListIgnoreMetadata.add(Blocks.unpowered_repeater);
		blockListIgnoreMetadata.add(Blocks.powered_repeater);
		blockListIgnoreMetadata.add(Blocks.trapdoor);
		blockListIgnoreMetadata.add(Blocks.vine);
		blockListIgnoreMetadata.add(Blocks.fence_gate);
		blockListIgnoreMetadata.add(Blocks.brick_stairs);
		blockListIgnoreMetadata.add(Blocks.stone_brick_stairs);
		blockListIgnoreMetadata.add(Blocks.waterlily);
		blockListIgnoreMetadata.add(Blocks.nether_brick_stairs);
		blockListIgnoreMetadata.add(Blocks.nether_wart);
		blockListIgnoreMetadata.add(Blocks.end_portal_frame);
		blockListIgnoreMetadata.add(Blocks.redstone_lamp);
		blockListIgnoreMetadata.add(Blocks.lit_redstone_lamp);
		blockListIgnoreMetadata.add(Blocks.wooden_slab);
		blockListIgnoreMetadata.add(Blocks.sandstone_stairs);
		blockListIgnoreMetadata.add(Blocks.ender_chest);
		blockListIgnoreMetadata.add(Blocks.tripwire_hook);
		blockListIgnoreMetadata.add(Blocks.tripwire);
		blockListIgnoreMetadata.add(Blocks.spruce_stairs);
		blockListIgnoreMetadata.add(Blocks.birch_stairs);
		blockListIgnoreMetadata.add(Blocks.jungle_stairs);
		blockListIgnoreMetadata.add(Blocks.flower_pot);
		blockListIgnoreMetadata.add(Blocks.carrots);
		blockListIgnoreMetadata.add(Blocks.potatoes);
		blockListIgnoreMetadata.add(Blocks.wooden_button);
		blockListIgnoreMetadata.add(Blocks.anvil);

		Map<Block, Object> blockListMapping = SchematicWorld.blockListMapping;
		blockListMapping.put(Blocks.flowing_water, Items.water_bucket);
		blockListMapping.put(Blocks.water, Items.water_bucket);
		blockListMapping.put(Blocks.flowing_lava, Items.lava_bucket);
		blockListMapping.put(Blocks.lava, Items.lava_bucket);
		blockListMapping.put(Blocks.bed, Items.bed);
		blockListMapping.put(Blocks.redstone_wire, Items.redstone);
		blockListMapping.put(Blocks.wheat, Items.wheat_seeds);
		blockListMapping.put(Blocks.lit_furnace, Blocks.furnace);
		blockListMapping.put(Blocks.standing_sign, Items.sign);
		blockListMapping.put(Blocks.wooden_door, Items.wooden_door);
		blockListMapping.put(Blocks.iron_door, Items.iron_door);
		blockListMapping.put(Blocks.wall_sign, Items.sign);
		blockListMapping.put(Blocks.unlit_redstone_torch, Blocks.redstone_torch);
		blockListMapping.put(Blocks.unpowered_repeater, Items.repeater);
		blockListMapping.put(Blocks.powered_repeater, Items.repeater);
		blockListMapping.put(Blocks.pumpkin_stem, Items.pumpkin_seeds);
		blockListMapping.put(Blocks.melon_stem, Items.melon_seeds);
		blockListMapping.put(Blocks.nether_wart, Items.nether_wart);
		blockListMapping.put(Blocks.brewing_stand, Items.brewing_stand);
		blockListMapping.put(Blocks.cauldron, Items.cauldron);
		blockListMapping.put(Blocks.lit_redstone_lamp, Blocks.redstone_lamp);
		blockListMapping.put(Blocks.cocoa, Items.dye);
		blockListMapping.put(Blocks.tripwire, Items.string);
		blockListMapping.put(Blocks.flower_pot, Items.flower_pot);
		blockListMapping.put(Blocks.carrots, Items.carrot);
		blockListMapping.put(Blocks.potatoes, Items.potato);
		blockListMapping.put(Blocks.skull, Items.skull);

		if (!Settings.schematicDirectory.exists()) {
			if (!Settings.schematicDirectory.mkdirs()) {
				Reference.logger.info("Could not create schematic directory!");
			}
		}

		if (!Settings.textureDirectory.exists()) {
			if (!Settings.textureDirectory.mkdirs()) {
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
			// KeyBindingRegistry.registerKeyBinding(new KeyBindingHandler(this.settings.keyBindings, new boolean[this.settings.keyBindings.length]));
			// TickRegistry.registerTickHandler(new Ticker(EnumSet.of(TickType.CLIENT)), Side.CLIENT);

			this.sortedWorldRenderers = ReflectionHelper.findField(RenderGlobal.class, "n", "field_72768_k", "sortedWorldRenderers");
		} catch (Exception e) {
			Reference.logger.fatal("Could not initialize the mod!", e);
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		String[] files = new String[] {
				"aliasVanilla", "flipVanilla", "rotationVanilla"
		};
		String mappingDir = DIR_ASSETS + "mapping/";
		ClassLoader classLoader = getClass().getClassLoader();

		for (String filename : files) {
			loadConfigurationFile(classLoader.getResource(mappingDir + filename + ".properties"), filename + ".properties");
		}

		File[] configurationFiles = this.configurationFolder.listFiles(FILE_FILTER_CONFIGURATION);

		for (File configurationFile : configurationFiles) {
			try {
				loadConfigurationFile(configurationFile.toURI().toURL(), configurationFile.getName());
			} catch (MalformedURLException e) {
				Settings.logger.error("Could not load properties file.", e);
			}
		}
	}

	private void loadConfigurationFile(URL configurationFile, String configurationFilename) {
		if (configurationFile == null) {
			Reference.logger.info("Skipping " + configurationFilename + "...");
			return;
		}

		Properties properties = new Properties();
		InputStream inputStream = null;

		Reference.logger.info("Reading " + configurationFilename + "...");

		try {
			inputStream = configurationFile.openStream();
			properties.load(inputStream);
		} catch (IOException e) {
			Reference.logger.error("Could not load properties file.", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				Reference.logger.error("Could not close properties file.", e);
			}
		}

		String filename = configurationFilename.toLowerCase();
		Set<Entry<Object, Object>> entrySet = properties.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof String)) {
				continue;
			}

			String key = (String) entry.getKey();
			String value = (String) entry.getValue();

			if (filename.startsWith("alias")) {
				if (!BlockInfo.addMappingAlias(key, value)) {
					Reference.logger.warn("Failed alias: " + key + " => " + value);
				}
			} else if (filename.startsWith("flip")) {
				if (!BlockInfo.addMappingFlip(key, value)) {
					Reference.logger.warn("Failed flip: " + key + " => " + value);
				}
			} else if (filename.startsWith("rotation")) {
				if (!BlockInfo.addMappingRotation(key, value)) {
					Reference.logger.warn("Failed rotation: " + key + " => " + value);
				}
			}
		}
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

				this.printer.print();
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
