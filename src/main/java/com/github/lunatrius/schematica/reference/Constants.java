package com.github.lunatrius.schematica.reference;

public final class Constants {
    public static final class Inventory {
        public static final class Size {
            public static final int CRAFTING_OUT = 1;
            public static final int CRAFTING_IN = 4;
            public static final int ARMOR = 4;
            public static final int INVENTORY = 3 * 9;
            public static final int HOTBAR = 9;
        }

        public static final class SlotOffset {
            public static final int CRAFTING_OUT = 0;
            public static final int CRAFTING_IN = CRAFTING_OUT + Size.CRAFTING_OUT;
            public static final int ARMOR = CRAFTING_IN + Size.CRAFTING_IN;
            public static final int INVENTORY = ARMOR + Size.ARMOR;
            public static final int HOTBAR = INVENTORY + Size.INVENTORY;
        }

        public static final class InventoryOffset {
            public static final int HOTBAR = 0;
            public static final int INVENTORY = HOTBAR + 9;
        }
    }

    public static final class Network {
        public static final int TIMEOUT = 15 * 20;
        public static final int RETRIES = 5;
    }

    public static final class SchematicChunk {
        public static final int WIDTH = 16;
        public static final int HEIGHT = 16;
        public static final int LENGTH = 16;
    }

    public static final class World {
        public static final int MINIMUM_COORD = -30000000;
        public static final int MAXIMUM_COORD = +30000000;
    }
}
