package com.github.lunatrius.schematica.world.chunk;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.List;

public class ChunkProviderSchematic implements IChunkProvider {
    private Chunk emptyChunk;

    public ChunkProviderSchematic(World world) {
        this.emptyChunk = new EmptyChunk(world, 0, 0);
    }

    @Override
    public boolean chunkExists(int x, int y) {
        return true;
    }

    @Override
    public Chunk provideChunk(int x, int y) {
        return this.emptyChunk;
    }

    @Override
    public Chunk loadChunk(int x, int y) {
        return this.emptyChunk;
    }

    @Override
    public void populate(IChunkProvider provider, int x, int y) {}

    @Override
    public boolean saveChunks(boolean saveExtra, IProgressUpdate progressUpdate) {
        return true;
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public String makeString() {
        return "SchematicChunkCache";
    }

    @Override
    public List getPossibleCreatures(EnumCreatureType creatureType, int x, int y, int z) {
        return null;
    }

    @Override
    public ChunkPosition func_147416_a(World world, String name, int x, int y, int z) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int x, int y) {}

    @Override
    public void saveExtraData() {}
}
