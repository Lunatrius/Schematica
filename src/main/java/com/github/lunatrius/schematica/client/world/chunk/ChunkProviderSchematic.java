package com.github.lunatrius.schematica.client.world.chunk;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkProviderSchematic implements IChunkProvider {
    private final SchematicWorld world;
    private final Chunk emptyChunk;
    private final Map<Long, ChunkSchematic> chunks = new HashMap<Long, ChunkSchematic>();

    public ChunkProviderSchematic(SchematicWorld world) {
        this.world = world;
        this.emptyChunk = new EmptyChunk(world, 0, 0);
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return x >= 0 && z >= 0 && x < this.world.getWidth() && z < this.world.getLength();
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        if (chunkExists(x, z)) {
            final long key = ChunkCoordIntPair.chunkXZ2Int(x, z);

            ChunkSchematic chunk = this.chunks.get(key);
            if (chunk == null) {
                chunk = new ChunkSchematic(this.world, x, z);
                this.chunks.put(key, chunk);
            }

            return chunk;
        }

        return this.emptyChunk;
    }

    @Override
    public Chunk provideChunk(BlockPos pos) {
        return provideChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Override
    public void populate(IChunkProvider provider, int x, int z) {}

    @Override
    public boolean func_177460_a(IChunkProvider chunkProvider, Chunk chunk, int x, int z) {
        return false;
    }

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
    public List getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return null;
    }

    @Override
    public BlockPos getStrongholdGen(World world, String name, BlockPos pos) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return this.world.getWidth() * this.world.getLength();
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) { }

    @Override
    public void saveExtraData() {}
}
