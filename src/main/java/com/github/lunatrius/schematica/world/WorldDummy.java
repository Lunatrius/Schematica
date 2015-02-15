package com.github.lunatrius.schematica.world;

import com.github.lunatrius.schematica.world.storage.SaveHandlerSchematic;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;

public class WorldDummy extends World {
    private static WorldDummy instance;

    public WorldDummy(ISaveHandler saveHandler, String name, WorldSettings worldSettings, WorldProvider worldProvider, Profiler profiler) {
        super(saveHandler, name, worldSettings, worldProvider, profiler);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected int func_152379_p() {
        return 0;
    }

    @Override
    public Entity getEntityByID(int id) {
        return null;
    }

    public static WorldDummy instance() {
        if (instance == null) {
            final WorldSettings worldSettings = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);
            instance = new WorldDummy(new SaveHandlerSchematic(), "Schematica", worldSettings, null, new Profiler());
        }

        return instance;
    }
}
