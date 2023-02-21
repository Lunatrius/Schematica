package com.github.lunatrius.schematica.handler;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class ConfigurationHandler {
    public static final ConfigurationHandler INSTANCE = new ConfigurationHandler();

    public static final String VERSION = "2";

    public static Configuration configuration;

    public static final boolean DUMP_BLOCK_LIST_DEFAULT = false;
    public static final boolean SHOW_DEBUG_INFO_DEFAULT = true;
    public static final boolean ENABLE_ALPHA_DEFAULT = false;
    public static final double ALPHA_DEFAULT = 1.0;
    public static final boolean HIGHLIGHT_DEFAULT = true;
    public static final boolean HIGHLIGHT_AIR_DEFAULT = true;
    public static final double BLOCK_DELTA_DEFAULT = 0.005;
    public static final int RENDER_DISTANCE_DEFAULT = 8;
    public static final double PLACE_DELAY_DEFAULT = 1.0;
    public static final int TIMEOUT_DEFAULT = 5;
    public static final int PLACE_DISTANCE_DEFAULT = 5;
    public static final boolean PLACE_INSTANTLY_DEFAULT = false;
    public static final boolean DESTROY_BLOCKS_DEFAULT = false;
    public static final boolean DESTROY_INSTANTLY_DEFAULT = false;
    public static final boolean PLACE_ADJACENT_DEFAULT = true;
    public static final boolean[] SWAP_SLOTS_DEFAULT = new boolean[] {
            false, false, false, false, false, true, true, true, true
    };
    public static final String SCHEMATIC_DIRECTORY_STR = "./schematics";
    public static final File SCHEMATIC_DIRECTORY_DEFAULT = new File(Schematica.proxy.getDataDirectory(), SCHEMATIC_DIRECTORY_STR);
    public static final String[] EXTRA_AIR_BLOCKS_DEFAULT = {};
    public static final String SORT_TYPE_DEFAULT = "";
    public static final boolean DEBUG_MODE_DEFAULT = false;
    public static final boolean STEALTH_MODE_DEFAULT = true;
    public static final int PRIORITY_DEFAULT = 1;
    public static final int DIRECTIONAL_PRIORITY_DEFAULT = 25;
    public static final boolean DISABLE_WHILE_MOVING_DEFAULT = false;
    public static final boolean PLACE_REPLACEABLE_DEFAULT = false;
    public static final boolean VIEW_ERRORS_TOGGLE_DEFAULT = false;

    public static boolean dumpBlockList = DUMP_BLOCK_LIST_DEFAULT;
    public static boolean showDebugInfo = SHOW_DEBUG_INFO_DEFAULT;
    public static boolean enableAlpha = ENABLE_ALPHA_DEFAULT;
    public static float alpha = (float) ALPHA_DEFAULT;
    public static boolean highlight = HIGHLIGHT_DEFAULT;
    public static boolean highlightAir = HIGHLIGHT_AIR_DEFAULT;
    public static double blockDelta = BLOCK_DELTA_DEFAULT;
    public static int renderDistance = RENDER_DISTANCE_DEFAULT;
    public static double placeDelay = PLACE_DELAY_DEFAULT;
    public static int timeout = TIMEOUT_DEFAULT;
    public static int placeDistance = PLACE_DISTANCE_DEFAULT;
    public static boolean placeInstantly = PLACE_INSTANTLY_DEFAULT;
    public static boolean destroyBlocks = DESTROY_BLOCKS_DEFAULT;
    public static boolean destroyInstantly = DESTROY_INSTANTLY_DEFAULT;
    public static boolean placeAdjacent = PLACE_ADJACENT_DEFAULT;
    public static boolean[] swapSlots = Arrays.copyOf(SWAP_SLOTS_DEFAULT, SWAP_SLOTS_DEFAULT.length);
    public static final Queue<Integer> swapSlotsQueue = new ArrayDeque<>();
    public static File schematicDirectory = SCHEMATIC_DIRECTORY_DEFAULT;
    public static String[] extraAirBlocks = Arrays.copyOf(EXTRA_AIR_BLOCKS_DEFAULT, EXTRA_AIR_BLOCKS_DEFAULT.length);
    public static String sortType = SORT_TYPE_DEFAULT;


    // ----------------------------------------------------------------------------------Psst, over here!
    public static boolean stealthMode = STEALTH_MODE_DEFAULT;
    public static boolean debugMode = DEBUG_MODE_DEFAULT;
    public static int priority = PRIORITY_DEFAULT;
    public static int directionalPriority = DIRECTIONAL_PRIORITY_DEFAULT;
    public static boolean disableWhileMoving = DISABLE_WHILE_MOVING_DEFAULT;
    public static boolean replace = PLACE_REPLACEABLE_DEFAULT;
    public static boolean viewErrorToggle = VIEW_ERRORS_TOGGLE_DEFAULT;

    public static Property propDumpBlockList = null;
    public static Property propShowDebugInfo = null;
    public static Property propEnableAlpha = null;
    public static Property propAlpha = null;
    public static Property propHighlight = null;
    public static Property propHighlightAir = null;
    public static Property propBlockDelta = null;
    public static Property propRenderDistance = null;
    public static Property propPlaceDelay = null;
    public static Property propTimeout = null;
    public static Property propPlaceDistance = null;
    public static Property propPlaceInstantly = null;
    public static Property propDestroyBlocks = null;
    public static Property propDestroyInstantly = null;
    public static Property propPlaceAdjacent = null;
    public static Property[] propSwapSlots = new Property[SWAP_SLOTS_DEFAULT.length];
    public static Property propSchematicDirectory = null;
    public static Property propExtraAirBlocks = null;
    public static Property propSortType = null;

    public static Property propStealthMode = null;
    public static Property propDebugMode = null;
    public static Property propPriority = null;
    public static Property propDirectionalPriority = null;
    public static Property propdisableWhileMoving = null;
    public static Property propReplace = null;
    public static Property propViewErrorToggle = null;


    private static final Set<Block> extraAirBlockList = new HashSet<>();

    public static void init(final File configFile) {
        if (configuration == null) {
            configuration = new Configuration(configFile, VERSION);
            loadConfiguration();
        }
    }

    public static void loadConfiguration() {
        loadConfigurationDebug();
        loadConfigurationRender();
        loadConfigurationPrinter();
        loadConfigurationSwapSlots();
        loadConfigurationGeneral();

        Schematica.proxy.createFolders();

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    private static void loadConfigurationDebug() {
        propDumpBlockList = configuration.get(Names.Config.Category.DEBUG, Names.Config.DUMP_BLOCK_LIST, DUMP_BLOCK_LIST_DEFAULT, Names.Config.DUMP_BLOCK_LIST_DESC);
        propDumpBlockList.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DUMP_BLOCK_LIST);
        propDumpBlockList.requiresMcRestart();
        dumpBlockList = propDumpBlockList.getBoolean(DUMP_BLOCK_LIST_DEFAULT);

        propShowDebugInfo = configuration.get(Names.Config.Category.DEBUG, Names.Config.SHOW_DEBUG_INFO, SHOW_DEBUG_INFO_DEFAULT, Names.Config.SHOW_DEBUG_INFO_DESC);
        propShowDebugInfo.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SHOW_DEBUG_INFO);
        showDebugInfo = propShowDebugInfo.getBoolean(SHOW_DEBUG_INFO_DEFAULT);
    }

    private static void loadConfigurationRender() {
        propEnableAlpha = configuration.get(Names.Config.Category.RENDER, Names.Config.ALPHA_ENABLED, ENABLE_ALPHA_DEFAULT, Names.Config.ALPHA_ENABLED_DESC);
        propEnableAlpha.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.ALPHA_ENABLED);
        enableAlpha = propEnableAlpha.getBoolean(ENABLE_ALPHA_DEFAULT);

        propAlpha = configuration.get(Names.Config.Category.RENDER, Names.Config.ALPHA, ALPHA_DEFAULT, Names.Config.ALPHA_DESC, 0.0, 1.0);
        propAlpha.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.ALPHA);
        alpha = (float) propAlpha.getDouble(ALPHA_DEFAULT);

        propHighlight = configuration.get(Names.Config.Category.RENDER, Names.Config.HIGHLIGHT, HIGHLIGHT_DEFAULT, Names.Config.HIGHLIGHT_DESC);
        propHighlight.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.HIGHLIGHT);
        highlight = propHighlight.getBoolean(HIGHLIGHT_DEFAULT);

        propHighlightAir = configuration.get(Names.Config.Category.RENDER, Names.Config.HIGHLIGHT_AIR, HIGHLIGHT_AIR_DEFAULT, Names.Config.HIGHLIGHT_AIR_DESC);
        propHighlightAir.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.HIGHLIGHT_AIR);
        highlightAir = propHighlightAir.getBoolean(HIGHLIGHT_AIR_DEFAULT);

        propBlockDelta = configuration.get(Names.Config.Category.RENDER, Names.Config.BLOCK_DELTA, BLOCK_DELTA_DEFAULT, Names.Config.BLOCK_DELTA_DESC, 0.0, 0.2);
        propBlockDelta.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.BLOCK_DELTA);
        blockDelta = propBlockDelta.getDouble(BLOCK_DELTA_DEFAULT);

        propRenderDistance = configuration.get(Names.Config.Category.RENDER, Names.Config.RENDER_DISTANCE, RENDER_DISTANCE_DEFAULT, Names.Config.RENDER_DISTANCE_DESC, 2, 16);
        propRenderDistance.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.RENDER_DISTANCE);
        renderDistance = propRenderDistance.getInt(RENDER_DISTANCE_DEFAULT);
    }

    private static void loadConfigurationPrinter() {
        propPlaceDelay = configuration.get(Names.Config.Category.PRINTER, Names.Config.PLACE_DELAY, PLACE_DELAY_DEFAULT, Names.Config.PLACE_DELAY_DESC, 1.0, 20.0);
        propPlaceDelay.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PLACE_DELAY);
        placeDelay = propPlaceDelay.getDouble(PLACE_DELAY_DEFAULT);

        propTimeout = configuration.get(Names.Config.Category.PRINTER, Names.Config.TIMEOUT, TIMEOUT_DEFAULT, Names.Config.TIMEOUT_DESC, 0, 100);
        propTimeout.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.TIMEOUT);
        timeout = propTimeout.getInt(TIMEOUT_DEFAULT);

        propPlaceDistance = configuration.get(Names.Config.Category.PRINTER, Names.Config.PLACE_DISTANCE, PLACE_DISTANCE_DEFAULT, Names.Config.PLACE_DISTANCE_DESC, 1, 5);
        propPlaceDistance.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PLACE_DISTANCE);
        placeDistance = propPlaceDistance.getInt(PLACE_DISTANCE_DEFAULT);

        propPlaceInstantly = configuration.get(Names.Config.Category.PRINTER, Names.Config.PLACE_INSTANTLY, PLACE_INSTANTLY_DEFAULT, Names.Config.PLACE_INSTANTLY_DESC);
        propPlaceInstantly.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PLACE_INSTANTLY);
        placeInstantly = propPlaceInstantly.getBoolean(PLACE_INSTANTLY_DEFAULT);

        propDestroyBlocks = configuration.get(Names.Config.Category.PRINTER, Names.Config.DESTROY_BLOCKS, DESTROY_BLOCKS_DEFAULT, Names.Config.DESTROY_BLOCKS_DESC);
        propDestroyBlocks.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DESTROY_BLOCKS);
        destroyBlocks = propDestroyBlocks.getBoolean(DESTROY_BLOCKS_DEFAULT);

        propDestroyInstantly = configuration.get(Names.Config.Category.PRINTER, Names.Config.DESTROY_INSTANTLY, DESTROY_INSTANTLY_DEFAULT, Names.Config.DESTROY_INSTANTLY_DESC);
        propDestroyInstantly.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DESTROY_INSTANTLY);
        destroyInstantly = propDestroyInstantly.getBoolean(DESTROY_INSTANTLY_DEFAULT);

        propPlaceAdjacent = configuration.get(Names.Config.Category.PRINTER, Names.Config.PLACE_ADJACENT, PLACE_ADJACENT_DEFAULT, Names.Config.PLACE_ADJACENT_DESC);
        propPlaceAdjacent.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PLACE_ADJACENT);
        placeAdjacent = propPlaceAdjacent.getBoolean(PLACE_ADJACENT_DEFAULT);

        propDebugMode = configuration.get(Names.Config.Category.PRINTER, Names.Config.DEBUG_MODE, DEBUG_MODE_DEFAULT, Names.Config.DEBUG_MODE_DESC);
        propDebugMode.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DEBUG_MODE);
        debugMode = propDebugMode.getBoolean(DEBUG_MODE_DEFAULT);

        propStealthMode = configuration.get(Names.Config.Category.PRINTER, Names.Config.STEALTH_MODE, STEALTH_MODE_DEFAULT, Names.Config.STEALTH_MODE_DESC);
        propStealthMode.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.STEALTH_MODE);
        stealthMode = propStealthMode.getBoolean(STEALTH_MODE_DEFAULT);

        propPriority = configuration.get(Names.Config.Category.PRINTER, Names.Config.PRIORITY, PRIORITY_DEFAULT, Names.Config.PRIORITY_DESC, 1, 3);
        propPriority.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.PRIORITY);
        priority = propPriority.getInt(PRIORITY_DEFAULT);

        propDirectionalPriority = configuration.get(Names.Config.Category.PRINTER, Names.Config.DIRECTIONAL_PRIORITY, DIRECTIONAL_PRIORITY_DEFAULT, Names.Config.DIRECTIONAL_PRIORITY_DESC, -1, 50);
        propDirectionalPriority.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DIRECTIONAL_PRIORITY);
        directionalPriority = propDirectionalPriority.getInt(DIRECTIONAL_PRIORITY_DEFAULT);

        propdisableWhileMoving = configuration.get(Names.Config.Category.PRINTER, Names.Config.DISABLE_WHILE_MOVING, DISABLE_WHILE_MOVING_DEFAULT, Names.Config.DISABLE_WHILE_MOVING_DESC);
        propdisableWhileMoving.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.DISABLE_WHILE_MOVING);
        disableWhileMoving = propdisableWhileMoving.getBoolean(DISABLE_WHILE_MOVING_DEFAULT);

        propReplace = configuration.get(Names.Config.Category.PRINTER, Names.Config.REPLACE, PLACE_REPLACEABLE_DEFAULT, Names.Config.REPLACE_DESC);
        propReplace.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.REPLACE);
        replace = propReplace.getBoolean(PLACE_REPLACEABLE_DEFAULT);
        

    }

    private static void loadConfigurationSwapSlots() {
        swapSlotsQueue.clear();
        for (int i = 0; i < SWAP_SLOTS_DEFAULT.length; i++) {
            propSwapSlots[i] = configuration.get(Names.Config.Category.PRINTER_SWAPSLOTS, Names.Config.SWAP_SLOT + i, SWAP_SLOTS_DEFAULT[i], Names.Config.SWAP_SLOT_DESC);
            propSwapSlots[i].setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SWAP_SLOT + i);
            swapSlots[i] = propSwapSlots[i].getBoolean(SWAP_SLOTS_DEFAULT[i]);

            if (swapSlots[i]) {
                swapSlotsQueue.offer(i);
            }
        }
    }

    private static void loadConfigurationGeneral() {
        propSchematicDirectory = configuration.get(Names.Config.Category.GENERAL, Names.Config.SCHEMATIC_DIRECTORY, SCHEMATIC_DIRECTORY_STR, Names.Config.SCHEMATIC_DIRECTORY_DESC);
        propSchematicDirectory.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.SCHEMATIC_DIRECTORY);
        schematicDirectory = getSchematicDirectoryFile(propSchematicDirectory.getString());

        propExtraAirBlocks = configuration.get(Names.Config.Category.GENERAL, Names.Config.EXTRA_AIR_BLOCKS, EXTRA_AIR_BLOCKS_DEFAULT, Names.Config.EXTRA_AIR_BLOCKS_DESC);
        propExtraAirBlocks.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.EXTRA_AIR_BLOCKS);
        extraAirBlocks = propExtraAirBlocks.getStringList();

        propViewErrorToggle = configuration.get(Names.Config.Category.GENERAL, Names.Config.VIEW_ERRORS_TOGGLE, VIEW_ERRORS_TOGGLE_DEFAULT, Names.Config.VIEW_ERRORS_TOGGLE_DESC);
        propViewErrorToggle.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.VIEW_ERRORS_TOGGLE);
        viewErrorToggle = propViewErrorToggle.getBoolean(VIEW_ERRORS_TOGGLE_DEFAULT);

        propSortType = configuration.get(Names.Config.Category.GENERAL, Names.Config.SORT_TYPE, SORT_TYPE_DEFAULT, Names.Config.SORT_TYPE_DESC);
        propSortType.setShowInGui(false);
        sortType = propSortType.getString();

        normalizeSchematicPath();
        populateExtraAirBlocks();
    }

    private static File getSchematicDirectoryFile(String path) {
        if (path.startsWith(".")) {
            return Schematica.proxy.getDirectory(path);
        }

        return new File(path);
    }

    private static void normalizeSchematicPath() {
        try {
            schematicDirectory = schematicDirectory.getCanonicalFile();
            final String schematicPath = schematicDirectory.getAbsolutePath();
            final String dataPath = Schematica.proxy.getDataDirectory().getAbsolutePath();
            final String newSchematicPath = mergePaths(schematicPath, dataPath);
            propSchematicDirectory.set(newSchematicPath);
            Reference.logger.debug("Schematic path: {}", schematicPath);
            Reference.logger.debug("Data path: {}", dataPath);
            Reference.logger.debug("New schematic path: {}", newSchematicPath);
        } catch (final IOException e) {
            Reference.logger.warn("Could not canonize path!", e);
        }
    }

    private static String mergePaths(final String schematicPath, final String dataPath) {
        final String newPath;
        if (schematicPath.startsWith(dataPath)) {
            newPath = "." + schematicPath.substring(dataPath.length());
        } else {
            newPath = schematicPath;
        }

        return newPath.replace("\\", "/");
    }

    private static void populateExtraAirBlocks() {
        extraAirBlockList.clear();
        for (final String name : extraAirBlocks) {
            final Block block = Block.REGISTRY.getObject(new ResourceLocation(name));
            if (block != Blocks.AIR) {
                extraAirBlockList.add(block);
            }
        }
    }


    private ConfigurationHandler() {}

    @SubscribeEvent
    public void onConfigurationChangedEvent(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(Reference.MODID)) {
            loadConfiguration();
        }
    }

    public static boolean isExtraAirBlock(final Block block) {
        return extraAirBlockList.contains(block);
    }
}
