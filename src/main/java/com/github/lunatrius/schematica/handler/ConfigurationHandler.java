package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.google.common.primitives.Ints;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class ConfigurationHandler {
    public static final ConfigurationHandler INSTANCE = new ConfigurationHandler();

    public static final String VERSION = "1";

    public static Configuration configuration;

    public static final boolean ENABLEALPHA_DEFAULT = false;
    public static final double ALPHA_DEFAULT = 1.0;
    public static final boolean HIGHLIGHT_DEFAULT = true;
    public static final boolean HIGHLIGHTAIR_DEFAULT = true;
    public static final double BLOCKDELTA_DEFAULT = 0.005;
    public static final boolean DRAWQUADS_DEFAULT = true;
    public static final boolean DRAWLINES_DEFAULT = true;
    public static final int PLACEDELAY_DEFAULT = 1;
    public static final int TIMEOUT_DEFAULT = 10;
    public static final boolean PLACEINSTANTLY_DEFAULT = false;
    public static final boolean DESTROYBLOCKS_DEFAULT = false;
    public static final boolean DESTROYINSTANTLY_DEFAULT = false;
    public static final boolean PLACEADJACENT_DEFAULT = true;
    public static final int[] SWAPSLOTS_DEFAULT = new int[] { };
    public static final boolean TOOLTIPENABLED_DEFAULT = true;
    public static final double TOOLTIPX_DEFAULT = 100;
    public static final double TOOLTIPY_DEFAULT = 0;
    public static final String SCHEMATICDIRECTORY_STR = "schematics";
    public static final File SCHEMATICDIRECTORY_DEFAULT = new File(Schematica.proxy.getDataDirectory(), SCHEMATICDIRECTORY_STR);
    public static final boolean PRINTERENABLED_DEFAULT = true;
    public static final boolean SAVEENABLED_DEFAULT = true;
    public static final boolean LOADENABLED_DEFAULT = true;

    public static boolean enableAlpha = ENABLEALPHA_DEFAULT;
    public static float alpha = (float) ALPHA_DEFAULT;
    public static boolean highlight = HIGHLIGHT_DEFAULT;
    public static boolean highlightAir = HIGHLIGHTAIR_DEFAULT;
    public static float blockDelta = (float) BLOCKDELTA_DEFAULT;
    public static boolean drawQuads = DRAWQUADS_DEFAULT;
    public static boolean drawLines = DRAWLINES_DEFAULT;
    public static int placeDelay = PLACEDELAY_DEFAULT;
    public static int timeout = TIMEOUT_DEFAULT;
    public static boolean placeInstantly = PLACEINSTANTLY_DEFAULT;
    public static boolean destroyBlocks = DESTROYBLOCKS_DEFAULT;
    public static boolean destroyInstantly = DESTROYINSTANTLY_DEFAULT;
    public static boolean placeAdjacent = PLACEADJACENT_DEFAULT;
    public static int[] swapSlots = SWAPSLOTS_DEFAULT;
    public static Queue<Integer> swapSlotsQueue = new ArrayDeque<Integer>();
    public static boolean tooltipEnabled = TOOLTIPENABLED_DEFAULT;
    public static float tooltipX = (float) TOOLTIPX_DEFAULT;
    public static float tooltipY = (float) TOOLTIPY_DEFAULT;
    public static File schematicDirectory = SCHEMATICDIRECTORY_DEFAULT;
    public static boolean printerEnabled = PRINTERENABLED_DEFAULT;
    public static boolean saveEnabled = SAVEENABLED_DEFAULT;
    public static boolean loadEnabled = LOADENABLED_DEFAULT;

    public static Property propEnableAlpha = null;
    public static Property propAlpha = null;
    public static Property propHighlight = null;
    public static Property propHighlightAir = null;
    public static Property propBlockDelta = null;
    public static Property propDrawQuads = null;
    public static Property propDrawLines = null;
    public static Property propPlaceDelay = null;
    public static Property propTimeout = null;
    public static Property propPlaceInstantly = null;
    public static Property propDestroyBlocks = null;
    public static Property propDestroyInstantly = null;
    public static Property propPlaceAdjacent = null;
    public static Property propSwapSlots = null;
    public static Property propTooltipEnabled = null;
    public static Property propTooltipX = null;
    public static Property propTooltipY = null;
    public static Property propSchematicDirectory = null;
    public static Property propPrinterEnabled = null;
    public static Property propSaveEnabled = null;
    public static Property propLoadEnabled = null;

    public static void init(File configFile) {
        if (configuration == null) {
            configuration = new Configuration(configFile, VERSION);
            loadConfiguration();
        }
    }

    private static void loadConfiguration() {
        propEnableAlpha = configuration.get(Names.Config.Category.RENDER, Names.Config.ALPHA_ENABLED, ENABLEALPHA_DEFAULT, Names.Config.ALPHA_ENABLED_DESC);
        propEnableAlpha.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.ALPHA_ENABLED);
        enableAlpha = propEnableAlpha.getBoolean(ENABLEALPHA_DEFAULT);

        propAlpha = configuration.get(Names.Config.Category.RENDER, Names.Config.ALPHA, ALPHA_DEFAULT, Names.Config.ALPHA_DESC, 0.0, 1.0);
        propAlpha.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.ALPHA);
        alpha = (float) propAlpha.getDouble(ALPHA_DEFAULT);

        propHighlight = configuration.get(Names.Config.Category.RENDER, Names.Config.HIGHLIGHT, HIGHLIGHT_DEFAULT, Names.Config.HIGHLIGHT_DESC);
        propHighlight.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.HIGHLIGHT);
        highlight = propHighlight.getBoolean(HIGHLIGHT_DEFAULT);

        propHighlightAir = configuration.get(Names.Config.Category.RENDER, Names.Config.HIGHLIGHT_AIR, HIGHLIGHTAIR_DEFAULT, Names.Config.HIGHLIGHT_AIR_DESC);
        propHighlightAir.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.HIGHLIGHT_AIR);
        highlightAir = propHighlightAir.getBoolean(HIGHLIGHTAIR_DEFAULT);

        propBlockDelta = configuration.get(Names.Config.Category.RENDER, Names.Config.BLOCK_DELTA, BLOCKDELTA_DEFAULT, Names.Config.BLOCK_DELTA_DESC, 0.0, 0.2);
        propBlockDelta.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.BLOCK_DELTA);
        blockDelta = (float) propBlockDelta.getDouble(BLOCKDELTA_DEFAULT);

        propDrawQuads = configuration.get(Names.Config.Category.RENDER, Names.Config.DRAW_QUADS, DRAWQUADS_DEFAULT, Names.Config.DRAW_QUADS_DESC);
        propDrawQuads.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DRAW_QUADS);
        drawQuads = propDrawQuads.getBoolean(DRAWQUADS_DEFAULT);

        propDrawLines = configuration.get(Names.Config.Category.RENDER, Names.Config.DRAW_LINES, DRAWLINES_DEFAULT, Names.Config.DRAW_LINES_DESC);
        propDrawLines.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DRAW_LINES);
        drawLines = propDrawLines.getBoolean(DRAWLINES_DEFAULT);

        propPlaceDelay = configuration.get(Names.Config.Category.PRINTER, Names.Config.PLACE_DELAY, PLACEDELAY_DEFAULT, Names.Config.PLACE_DELAY_DESC, 0, 20);
        propPlaceDelay.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PLACE_DELAY);
        placeDelay = propPlaceDelay.getInt(PLACEDELAY_DEFAULT);

        propTimeout = configuration.get(Names.Config.Category.PRINTER, Names.Config.TIMEOUT, TIMEOUT_DEFAULT, Names.Config.TIMEOUT_DESC, 0, 100);
        propTimeout.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.TIMEOUT);
        timeout = propTimeout.getInt(TIMEOUT_DEFAULT);

        propPlaceInstantly = configuration.get(Names.Config.Category.PRINTER, Names.Config.PLACE_INSTANTLY, PLACEINSTANTLY_DEFAULT, Names.Config.PLACE_INSTANTLY_DESC);
        propPlaceInstantly.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PLACE_INSTANTLY);
        placeInstantly = propPlaceInstantly.getBoolean(PLACEINSTANTLY_DEFAULT);

        propDestroyBlocks = configuration.get(Names.Config.Category.PRINTER, Names.Config.DESTROY_BLOCKS, DESTROYBLOCKS_DEFAULT, Names.Config.DESTROY_BLOCKS_DESC);
        propDestroyBlocks.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DESTROY_BLOCKS);
        destroyBlocks = propDestroyBlocks.getBoolean(DESTROYBLOCKS_DEFAULT);

        propDestroyInstantly = configuration.get(Names.Config.Category.PRINTER, Names.Config.DESTROY_INSTANTLY, DESTROYINSTANTLY_DEFAULT, Names.Config.DESTROY_INSTANTLY_DESC);
        propDestroyInstantly.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DESTROY_INSTANTLY);
        destroyInstantly = propDestroyInstantly.getBoolean(DESTROYINSTANTLY_DEFAULT);

        propPlaceAdjacent = configuration.get(Names.Config.Category.PRINTER, Names.Config.PLACE_ADJACENT, PLACEADJACENT_DEFAULT, Names.Config.PLACE_ADJACENT_DESC);
        propPlaceAdjacent.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PLACE_ADJACENT);
        placeAdjacent = propPlaceAdjacent.getBoolean(PLACEADJACENT_DEFAULT);

        propSwapSlots = configuration.get(Names.Config.Category.PRINTER, Names.Config.SWAP_SLOTS, SWAPSLOTS_DEFAULT, Names.Config.SWAP_SLOTS_DESC, 0, 8);
        propSwapSlots.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SWAP_SLOTS);
        swapSlots = propSwapSlots.getIntList();
        swapSlotsQueue = new ArrayDeque<Integer>(Ints.asList(swapSlots));

        propTooltipEnabled = configuration.get(Names.Config.Category.TOOLTIP, Names.Config.TOOLTIP_ENABLED, TOOLTIPENABLED_DEFAULT, Names.Config.TOOLTIP_ENABLED_DESC);
        propTooltipEnabled.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.TOOLTIP_ENABLED);
        tooltipEnabled = propTooltipEnabled.getBoolean(TOOLTIPENABLED_DEFAULT);

        propTooltipX = configuration.get(Names.Config.Category.TOOLTIP, Names.Config.TOOLTIP_X, TOOLTIPX_DEFAULT, Names.Config.TOOLTIP_X_DESC, 0, 100);
        propTooltipX.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.TOOLTIP_X);
        tooltipX = (float) propTooltipX.getDouble(TOOLTIPX_DEFAULT);

        propTooltipY = configuration.get(Names.Config.Category.TOOLTIP, Names.Config.TOOLTIP_Y, TOOLTIPY_DEFAULT, Names.Config.TOOLTIP_Y_DESC, 0, 100);
        propTooltipY.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.TOOLTIP_Y);
        tooltipY = (float) propTooltipY.getDouble(TOOLTIPY_DEFAULT);

        propSchematicDirectory = configuration.get(Names.Config.Category.GENERAL, Names.Config.SCHEMATIC_DIRECTORY, SCHEMATICDIRECTORY_STR, Names.Config.SCHEMATIC_DIRECTORY_DESC);
        propSchematicDirectory.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SCHEMATIC_DIRECTORY);
        schematicDirectory = new File(propSchematicDirectory.getString());

        try {
            schematicDirectory = schematicDirectory.getCanonicalFile();
            final String schematicPath = schematicDirectory.getAbsolutePath();
            final String dataPath = Schematica.proxy.getDataDirectory().getAbsolutePath();
            if (schematicPath.contains(dataPath)) {
                propSchematicDirectory.set(schematicPath.substring(dataPath.length()).replace("\\", "/").replaceAll("^/+", ""));
            } else {
                propSchematicDirectory.set(schematicPath.replace("\\", "/"));
            }
        } catch (IOException e) {
            Reference.logger.warn("Could not canonize path!", e);
        }

        propPrinterEnabled = configuration.get(Names.Config.Category.SERVER, Names.Config.PRINTER_ENABLED, PRINTERENABLED_DEFAULT, Names.Config.PRINTER_ENABLED_DESC);
        propPrinterEnabled.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PRINTER_ENABLED);
        printerEnabled = propPrinterEnabled.getBoolean(PRINTERENABLED_DEFAULT);

        propSaveEnabled = configuration.get(Names.Config.Category.SERVER, Names.Config.SAVE_ENABLED, SAVEENABLED_DEFAULT, Names.Config.SAVE_ENABLED_DESC);
        propSaveEnabled.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SAVE_ENABLED);
        saveEnabled = propSaveEnabled.getBoolean(SAVEENABLED_DEFAULT);

        propLoadEnabled = configuration.get(Names.Config.Category.SERVER, Names.Config.LOAD_ENABLED, LOADENABLED_DEFAULT, Names.Config.LOAD_ENABLED_DESC);
        propLoadEnabled.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.LOAD_ENABLED);
        loadEnabled = propLoadEnabled.getBoolean(LOADENABLED_DEFAULT);

        Schematica.proxy.createFolders();

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    private ConfigurationHandler() {}

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(Reference.MODID)) {
            loadConfiguration();
        }
    }
}
