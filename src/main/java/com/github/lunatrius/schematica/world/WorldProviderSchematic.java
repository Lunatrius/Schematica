package com.github.lunatrius.schematica.world;

import net.minecraft.world.WorldProvider;

public class WorldProviderSchematic extends WorldProvider {
    @Override
    public String getDimensionName() {
        return "Schematic";
    }

    @Override
    public String getInternalNameSuffix() {
        return "_schematic";
    }
}
