package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.lib.Reference;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.io.IOException;

public class ConfigurationHandler {
	public static final String CATEGORY_RENDER = "render";
	public static final String CATEGORY_PRINTER = "printer";
	public static final String CATEGORY_GENERAL = "general";

	public static final String ALPHA_ENABLED = "alphaEnabled";
	public static final String ALPHA_ENABLED_DESC = "Enable transparent textures.";
	public static final String ALPHA = "alpha";
	public static final String ALPHA_DESC = "Alpha value used when rendering the schematic (1.0 = opaque, 0.5 = half transparent, 0.0 = transparent).";
	public static final String HIGHLIGHT = "highlight";
	public static final String HIGHLIGHT_DESC = "Highlight invalid placed blocks and to be placed blocks.";
	public static final String HIGHLIGHT_AIR = "highlightAir";
	public static final String HIGHLIGHT_AIR_DESC = "Highlight blocks that should be air.";
	public static final String BLOCK_DELTA = "blockDelta";
	public static final String BLOCK_DELTA_DESC = "Delta value used for highlighting (if you experience z-fighting increase this).";
	public static final String DRAW_QUADS = "drawQuads";
	public static final String DRAW_QUADS_DESC = "Draw surface areas.";
	public static final String DRAW_LINES = "drawLines";
	public static final String DRAW_LINES_DESC = "Draw outlines.";

	public static final String PLACE_DELAY = "placeDelay";
	public static final String PLACE_DELAY_DESC = "Delay between placement attempts (in ticks).";
	public static final String TIMEOUT = "timeout";
	public static final String TIMEOUT_DESC = "Timeout before re-trying failed blocks.";
	public static final String PLACE_INSTANTLY = "placeInstantly";
	public static final String PLACE_INSTANTLY_DESC = "Place all blocks that can be placed in one tick.";
	public static final String PLACE_ADJACENT = "placeAdjacent";
	public static final String PLACE_ADJACENT_DESC = "Place blocks only if there is an adjacent block next to them.";

	public static final String SCHEMATIC_DIRECTORY = "schematicDirectory";
	public static final String SCHEMATIC_DIRECTORY_DESC = "Schematic directory.";

	public static final String LANG_PREFIX = Reference.MODID.toLowerCase() + ".config";

	public static Configuration configuration;

	public static boolean enableAlpha = false;
	public static float alpha = 1.0f;
	public static boolean highlight = true;
	public static boolean highlightAir = true;
	public static float blockDelta = 0.005f;
	public static int placeDelay = 1;
	public static int timeout = 10;
	public static boolean placeInstantly = false;
	public static boolean placeAdjacent = true;
	public static boolean drawQuads = true;
	public static boolean drawLines = true;
	public static File schematicDirectory = new File(Schematica.proxy.getDataDirectory(), "schematics");

	public static Property propEnableAlpha = null;
	public static Property propAlpha = null;
	public static Property propHighlight = null;
	public static Property propHighlightAir = null;
	public static Property propBlockDelta = null;
	public static Property propPlaceDelay = null;
	public static Property propTimeout = null;
	public static Property propPlaceInstantly = null;
	public static Property propPlaceAdjacent = null;
	public static Property propDrawQuads = null;
	public static Property propDrawLines = null;
	public static Property propSchematicDirectory = null;

	public static void init(File configFile) {
		if (configuration == null) {
			configuration = new Configuration(configFile);
			loadConfiguration();
		}
	}

	private static void loadConfiguration() {
		propEnableAlpha = configuration.get(CATEGORY_RENDER, ALPHA_ENABLED, enableAlpha, ALPHA_ENABLED_DESC);
		propEnableAlpha.setLanguageKey(String.format("%s.%s", LANG_PREFIX, ALPHA_ENABLED));
		propEnableAlpha.setShowInGui(false);
		enableAlpha = propEnableAlpha.getBoolean(enableAlpha);

		propAlpha = configuration.get(CATEGORY_RENDER, ALPHA, alpha, ALPHA_DESC, 0.0, 1.0);
		propAlpha.setLanguageKey(String.format("%s.%s", LANG_PREFIX, ALPHA));
		propAlpha.setShowInGui(false);
		alpha = (float) propAlpha.getDouble(alpha);

		propHighlight = configuration.get(CATEGORY_RENDER, HIGHLIGHT, highlight, HIGHLIGHT_DESC);
		propHighlight.setLanguageKey(String.format("%s.%s", LANG_PREFIX, HIGHLIGHT));
		highlight = propHighlight.getBoolean(highlight);

		propHighlightAir = configuration.get(CATEGORY_RENDER, HIGHLIGHT_AIR, highlightAir, HIGHLIGHT_AIR_DESC);
		propHighlightAir.setLanguageKey(String.format("%s.%s", LANG_PREFIX, HIGHLIGHT_AIR));
		highlightAir = propHighlightAir.getBoolean(highlightAir);

		propBlockDelta = configuration.get(CATEGORY_RENDER, BLOCK_DELTA, blockDelta, BLOCK_DELTA_DESC, 0.0, 0.2);
		propBlockDelta.setLanguageKey(String.format("%s.%s", LANG_PREFIX, BLOCK_DELTA));
		blockDelta = (float) propBlockDelta.getDouble(blockDelta);

		propDrawQuads = configuration.get(CATEGORY_RENDER, DRAW_QUADS, drawQuads, DRAW_QUADS_DESC);
		propDrawQuads.setLanguageKey(String.format("%s.%s", LANG_PREFIX, DRAW_QUADS));
		drawQuads = propDrawQuads.getBoolean(drawQuads);

		propDrawLines = configuration.get(CATEGORY_RENDER, DRAW_LINES, drawLines, DRAW_LINES_DESC);
		propDrawLines.setLanguageKey(String.format("%s.%s", LANG_PREFIX, DRAW_LINES));
		drawLines = propDrawLines.getBoolean(drawLines);

		propPlaceDelay = configuration.get(CATEGORY_PRINTER, PLACE_DELAY, placeDelay, PLACE_DELAY_DESC, 0, 20);
		propPlaceDelay.setLanguageKey(String.format("%s.%s", LANG_PREFIX, PLACE_DELAY));
		placeDelay = propPlaceDelay.getInt(placeDelay);

		propTimeout = configuration.get(CATEGORY_PRINTER, TIMEOUT, timeout, TIMEOUT_DESC, 0, 100);
		propTimeout.setLanguageKey(String.format("%s.%s", LANG_PREFIX, TIMEOUT));
		timeout = propTimeout.getInt(timeout);

		propPlaceInstantly = configuration.get(CATEGORY_PRINTER, PLACE_INSTANTLY, placeInstantly, PLACE_INSTANTLY_DESC);
		propPlaceInstantly.setLanguageKey(String.format("%s.%s", LANG_PREFIX, PLACE_INSTANTLY));
		placeInstantly = propPlaceInstantly.getBoolean(placeInstantly);

		propPlaceAdjacent = configuration.get(CATEGORY_PRINTER, PLACE_ADJACENT, placeAdjacent, PLACE_ADJACENT_DESC);
		propPlaceAdjacent.setLanguageKey(String.format("%s.%s", LANG_PREFIX, PLACE_ADJACENT));
		placeAdjacent = propPlaceAdjacent.getBoolean(placeAdjacent);

		try {
			schematicDirectory = schematicDirectory.getCanonicalFile();
		} catch (IOException e) {
			Reference.logger.warn("Could not canonize file path!", e);
		}

		propSchematicDirectory = configuration.get(CATEGORY_GENERAL, SCHEMATIC_DIRECTORY, schematicDirectory.getAbsolutePath().replace("\\", "/"), SCHEMATIC_DIRECTORY_DESC);
		propSchematicDirectory.setLanguageKey(String.format("%s.%s", LANG_PREFIX, SCHEMATIC_DIRECTORY));
		schematicDirectory = new File(propSchematicDirectory.getString());

		if (configuration.hasChanged()) {
			configuration.save();
		}
	}

	@SubscribeEvent
	public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equalsIgnoreCase(Reference.MODID)) {
			loadConfiguration();
		}
	}
}
