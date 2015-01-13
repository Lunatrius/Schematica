package com.github.lunatrius.schematica.api;

import com.github.lunatrius.core.util.vector.Vector3f;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public interface ISchematic {
    /**
     * Gets a block at a given location within the schematic. Requesting a block outside of those bounds returns Air.
     * @param x the X coord in world space.
     * @param y the Y coord in world space.
     * @param z the Z coord in world space.
     * @return the block at the requested location.
     */
    Block getBlock(int x, int y, int z);

    /**
     * Sets the block at the given location, metadata will be set to 0. Attempting to set a block outside of the schematic
     * boundaries or with an invalid block will result in no change being made and this method will return false.
     * @param x the X coord in world space.
     * @param y the Y coord in world space.
     * @param z the Z coord in world space.
     * @param block the Block to set
     * @return true if the block was succesfully set.
     */
    boolean setBlock(int x, int y, int z, Block block);

    /**
     * Sets the block and metadata at the given location. Attempting to set a block outside of the schematic
     * boundaries or with an invalid block will result in no change being made and this method will return false.
     * @param x the X coord in world space.
     * @param y the Y coord in world space.
     * @param z the Z coord in world space.
     * @param block the Block to set
     * @param metadata the metadata value to set.
     * @return true if the block was succesfully set.
     */
    boolean setBlock(int x, int y, int z, Block block, int metadata);

    /**
     * Gets the Tile Entity at the requested location. If no tile entity exists at that location, null will be returned.
     * @param x the X coord in world space.
     * @param y the Y coord in world space.
     * @param z the Z coord in world space.
     * @return the located tile entity.
     */
    TileEntity getTileEntity(int x, int y, int z);

    /**
     * returns a list of all tile entities in the schematic.
     * @return all tile entities.
     */
    List<TileEntity> getTileEntities();

    /**
     * Add or replace a tile entity to a block at the requested location. Does nothing if the location is out of bounds.
     * @param x the X coord in world space.
     * @param y the Y coord in world space.
     * @param z the Z coord in world space.
     * @param tileEntity the Tile Entity to set.
     */
    void setTileEntity(int x, int y, int z, TileEntity tileEntity);

    /**
     * Removes a Tile Entity from the specific location if it exists, otherwise it silently continues.
     * @param x the X coord in world space.
     * @param y the Y coord in world space.
     * @param z the Z coord in world space.
     */
    void removeTileEntity(int x, int y, int z);

    /**
     * Gets the metadata of the block at the requested location.
     * @param x the X coord in world space.
     * @param y the Y coord in world space.
     * @param z the Z coord in world space.
     * @return the Metadata Value
     */
    int getBlockMetadata(int x, int y, int z);

    /**
     * Modify the metadata of the block at the requested location.
     * @param x the X coord in world space.
     * @param y the Y coord in world space.
     * @param z the Z coord in world space.
     * @param metadata the Metadata Value
     */
    void setBlockMetadata(int x, int y, int z, int metadata);

    /**
     * Retrieves the icon that will be used to save the schematic.
     * @return the schematic's future icon.
     */
    ItemStack getIcon();

    /**
     * Modifies the icon that will be used when saving the schematic.
     * @param icon an ItemStack of the Item you wish you use as the icon.
     */
    void setIcon(ItemStack icon);

    /**
     * The width of the schematic
     * @return the schematic width
     */
    int getWidth();

    /**
     * The length of the schematic
     * @return the schematic length
     */
    int getLength();

    /**
     * The height of the schematic
     * @return the schematic height
     */
    int getHeight();
}
