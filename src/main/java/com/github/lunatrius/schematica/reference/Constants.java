package com.github.lunatrius.schematica.reference;

public final class Constants {
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
