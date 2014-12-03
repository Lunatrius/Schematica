package com.github.lunatrius.schematica.world.chunk;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.io.File;

public class SchematicContainer {
    public final SchematicWorld schematic;
    public final EntityPlayer player;
    public final World world;
    public final File file;

    public final int minX;
    public final int maxX;
    public final int minY;
    public final int maxY;
    public final int minZ;
    public final int maxZ;

    public final int minChunkX;
    public final int maxChunkX;
    public final int minChunkZ;
    public final int maxChunkZ;

    public int curChunkX;
    public int curChunkZ;

    public final int chunkCount;
    public int processedChunks;

    public SchematicContainer(SchematicWorld schematic, EntityPlayer player, World world, File file, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.schematic = schematic;
        this.player = player;
        this.world = world;
        this.file = file;

        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;

        this.minChunkX = this.minX >> 4;
        this.maxChunkX = this.maxX >> 4;
        this.minChunkZ = this.minZ >> 4;
        this.maxChunkZ = this.maxZ >> 4;

        this.curChunkX = this.minChunkX;
        this.curChunkZ = this.minChunkZ;

        this.chunkCount = (this.maxChunkX - this.minChunkX + 1) * (this.maxChunkZ - this.minChunkZ + 1);
    }

    public void next() {
        if (!hasNext()) {
            return;
        }

        Reference.logger.debug(String.format("Copying chunk at [%d,%d] into %s", this.curChunkX, this.curChunkZ, this.file.getName()));
        Schematica.proxy.copyChunkToSchematic(this.schematic, this.world, this.curChunkX, this.curChunkZ, this.minX, this.maxX, this.minY, this.maxY, this.minZ, this.maxZ);

        this.processedChunks++;
        this.curChunkX++;
        if (this.curChunkX > this.maxChunkX) {
            this.curChunkX = this.minChunkX;
            this.curChunkZ++;
        }
    }

    public boolean isFirst() {
        return this.curChunkX == this.minChunkX && this.curChunkZ == this.minChunkZ;
    }

    public boolean hasNext() {
        return this.curChunkX <= this.maxChunkX && this.curChunkZ <= this.maxChunkZ;
    }
}
