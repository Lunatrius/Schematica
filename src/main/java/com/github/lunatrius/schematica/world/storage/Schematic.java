package com.github.lunatrius.schematica.world.storage;

import com.github.lunatrius.schematica.api.ISchematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

public class Schematic implements ISchematic {
    private static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.GRASS);

    private ItemStack icon;
    private final short[][][] blocks;
    private final byte[][][] metadata;
    private final List<TileEntity> tileEntities = new ArrayList<TileEntity>();
    private final List<Entity> entities = new ArrayList<Entity>();
    private final int width;
    private final int height;
    private final int length;
    private String author;

    public Schematic(final ItemStack icon, final int width, final int height, final int length) {
        this(icon, width, height, length, "");
    }

    public Schematic(final ItemStack icon, final int width, final int height, final int length, @Nonnull final String author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }

        this.icon = icon;
        this.blocks = new short[width][height][length];
        this.metadata = new byte[width][height][length];

        this.width = width;
        this.height = height;
        this.length = length;

        this.author = author;
    }

    @Override
    public IBlockState getBlockState(final BlockPos pos) {
        if (!isValid(pos)) {
            return Blocks.AIR.getDefaultState();
        }

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final Block block = Block.REGISTRY.getObjectById(this.blocks[x][y][z]);

        return block.getStateFromMeta(this.metadata[x][y][z]);
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final IBlockState blockState) {
        if (!isValid(pos)) {
            return false;
        }

        final Block block = blockState.getBlock();
        final int id = Block.REGISTRY.getIDForObject(block);
        if (id == -1) {
            return false;
        }

        final int meta = block.getMetaFromState(blockState);
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        this.blocks[x][y][z] = (short) id;
        this.metadata[x][y][z] = (byte) meta;
        return true;
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos) {
        for (final TileEntity tileEntity : this.tileEntities) {
            if (tileEntity.getPos().equals(pos)) {
                return tileEntity;
            }
        }

        return null;
    }

    @Override
    public List<TileEntity> getTileEntities() {
        return this.tileEntities;
    }

    @Override
    public void setTileEntity(final BlockPos pos, final TileEntity tileEntity) {
        if (!isValid(pos)) {
            return;
        }

        removeTileEntity(pos);

        if (tileEntity != null) {
            this.tileEntities.add(tileEntity);
        }
    }

    @Override
    public void removeTileEntity(final BlockPos pos) {
        final Iterator<TileEntity> iterator = this.tileEntities.iterator();

        while (iterator.hasNext()) {
            final TileEntity tileEntity = iterator.next();
            if (tileEntity.getPos().equals(pos)) {
                iterator.remove();
            }
        }
    }

    @Override
    public List<Entity> getEntities() {
        return this.entities;
    }

    @Override
    public void addEntity(final Entity entity) {
        if (entity == null || entity.getUniqueID() == null || entity instanceof EntityPlayer) {
            return;
        }

        for (final Entity e : this.entities) {
            if (entity.getUniqueID().equals(e.getUniqueID())) {
                return;
            }
        }

        this.entities.add(entity);
    }

    @Override
    public void removeEntity(final Entity entity) {
        if (entity == null || entity.getUniqueID() == null) {
            return;
        }

        final Iterator<Entity> iterator = this.entities.iterator();
        while (iterator.hasNext()) {
            final Entity e = iterator.next();
            if (entity.getUniqueID().equals(e.getUniqueID())) {
                iterator.remove();
            }
        }
    }

    @Override
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    public void setIcon(final ItemStack icon) {
        if (icon != null) {
            this.icon = icon;
        } else {
            this.icon = DEFAULT_ICON.copy();
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    private boolean isValid(final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        return !(x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length);
    }

    @Override
    @Nonnull
    public String getAuthor() {
        return this.author;
    }

    @Override
    public void setAuthor(@Nonnull final String author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        this.author = author;
    }
}
