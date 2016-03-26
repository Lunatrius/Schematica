package com.github.lunatrius.schematica.client.world.chunk;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkProviderSchematic implements IChunkProvider {
    private final SchematicWorld world;
    private final Chunk emptyChunk;
    private final Map<Long, ChunkSchematic> chunks = new ConcurrentHashMap<Long, ChunkSchematic>();

    public ChunkProviderSchematic(final SchematicWorld world) {
        this.world = world;
        this.emptyChunk = new EmptyChunk(world, 0, 0) {
            @Override
            public boolean isEmpty() {
                return false;
            }
        };
    }

    private boolean chunkExists(final int x, final int z) {
        return x >= 0 && z >= 0 && x < this.world.getWidth() && z < this.world.getLength();
    }

    @Override
    public Chunk getLoadedChunk(final int x, final int z) {
        if (!chunkExists(x, z)) {
            return this.emptyChunk;
        }

        final long key = ChunkCoordIntPair.chunkXZ2Int(x, z);

        ChunkSchematic chunk = this.chunks.get(key);
        if (chunk == null) {
            chunk = new ChunkSchematic(this.world, x, z);
            this.chunks.put(key, chunk);
        }

        return chunk;
    }

    @Override
    public Chunk provideChunk(final int x, final int z) {
        return getLoadedChunk(x, z);
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public String makeString() {
        return "SchematicChunkCache";
    }
}
