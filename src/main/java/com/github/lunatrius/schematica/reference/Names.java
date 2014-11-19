package com.github.lunatrius.schematica.reference;

@SuppressWarnings("HardCodedStringLiteral")
public final class Names {
    public static final class Config {
        public static final class Category {
            public static final String RENDER = "render";
            public static final String PRINTER = "printer";
            public static final String TOOLTIP = "tooltip";
            public static final String GENERAL = "general";
            public static final String SERVER = "server";
        }

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
        public static final String DESTROY_BLOCKS = "destroyBlocks";
        public static final String DESTROY_BLOCKS_DESC = "The printer will destroy blocks (creative mode only).";
        public static final String DESTROY_INSTANTLY = "destroyInstantly";
        public static final String DESTROY_INSTANTLY_DESC = "Destroy all blocks that can be destroyed in one tick.";
        public static final String PLACE_ADJACENT = "placeAdjacent";
        public static final String PLACE_ADJACENT_DESC = "Place blocks only if there is an adjacent block next to them.";
        public static final String SWAP_SLOTS = "swapSlots";
        public static final String SWAP_SLOTS_DESC = "The printer will use these slots to swap out items in the inventory.";

        public static final String TOOLTIP_ENABLED = "tooltipEnabled";
        public static final String TOOLTIP_ENABLED_DESC = "Display a tooltip when hovering over blocks in a schematic.";
        public static final String TOOLTIP_X = "tooltipX";
        public static final String TOOLTIP_X_DESC = "Relative tooltip X.";
        public static final String TOOLTIP_Y = "tooltipY";
        public static final String TOOLTIP_Y_DESC = "Relative tooltip Y.";

        public static final String SCHEMATIC_DIRECTORY = "schematicDirectory";
        public static final String SCHEMATIC_DIRECTORY_DESC = "Schematic directory.";

        public static final String PRINTER_ENABLED = "printerEnabled";
        public static final String PRINTER_ENABLED_DESC = "printerEnabled";
        public static final String SAVE_ENABLED = "saveEnabled";
        public static final String SAVE_ENABLED_DESC = "printerEnabled";
        public static final String LOAD_ENABLED = "loadEnabled";
        public static final String LOAD_ENABLED_DESC = "printerEnabled";

        public static final String LANG_PREFIX = Reference.MODID.toLowerCase() + ".config";
    }

    public static final class ModId {
        public static final String MINECRAFT = "minecraft";
    }

    public static final class NBT {
        public static final String ROOT = "Schematic";

        public static final String MATERIALS = "Materials";
        public static final String FORMAT_CLASSIC = "Classic";
        public static final String FORMAT_ALPHA = "Alpha";

        public static final String ICON = "Icon";
        public static final String BLOCKS = "Blocks";
        public static final String DATA = "Data";
        public static final String ADD_BLOCKS = "AddBlocks";
        public static final String ADD_BLOCKS_SCHEMATICA = "Add";
        public static final String WIDTH = "Width";
        public static final String LENGTH = "Length";
        public static final String HEIGHT = "Height";
        public static final String MAPPING = "..."; // TODO: use this once MCEdit adds support for it
        public static final String MAPPING_SCHEMATICA = "SchematicaMapping";
        public static final String TILE_ENTITIES = "TileEntities";
        public static final String ENTITIES = "Entities";
    }

    public static final class SBC {
        public static final String DISABLE_PRINTER = "\u00a70\u00a72\u00a70\u00a70\u00a7e\u00a7f";
        public static final String DISABLE_SAVE = "\u00a70\u00a72\u00a71\u00a70\u00a7e\u00a7f";
        public static final String DISABLE_LOAD = "\u00a70\u00a72\u00a71\u00a71\u00a7e\u00a7f";
    }
}
