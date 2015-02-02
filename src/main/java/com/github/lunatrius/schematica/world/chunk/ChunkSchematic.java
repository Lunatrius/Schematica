package com.github.lunatrius.schematica.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ChunkSchematic extends Chunk {
    private final World world;
    private final int x;
    private final int z;

    public ChunkSchematic(World world, int x, int z) {
        super(world, x, z);
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override
    protected void generateHeightMap() {
    }

    @Override
    public void generateSkylightMap() {
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return this.world.getBlockState(pos);
    }

    @Override
    public boolean getAreLevelsEmpty(int startY, int endY) {
        return false;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType createEntityType) {
        return this.world.getTileEntity(pos);
    }
}
