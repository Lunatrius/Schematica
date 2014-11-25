package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.nbt.TileEntityException;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.github.lunatrius.schematica.world.schematic.SchematicUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.io.File;

public abstract class CommonProxy {
    public boolean isSaveEnabled = true;
    public boolean isLoadEnabled = true;

    public abstract void setConfigEntryClasses();

    public abstract void registerKeybindings();

    public abstract void registerEvents();

    public void createFolders() {
        if (!ConfigurationHandler.schematicDirectory.exists()) {
            if (!ConfigurationHandler.schematicDirectory.mkdirs()) {
                Reference.logger.info("Could not create schematic directory [%s]!", ConfigurationHandler.schematicDirectory.getAbsolutePath());
            }
        }
    }

    public abstract File getDataDirectory();

    public void resetSettings() {
        this.isSaveEnabled = true;
        this.isLoadEnabled = true;
    }

    public SchematicWorld getSchematicFromWorld(World world, Vector3i from, Vector3i to) {
        try {
            int minX = Math.min(from.x, to.x);
            int maxX = Math.max(from.x, to.x);
            int minY = Math.min(from.y, to.y);
            int maxY = Math.max(from.y, to.y);
            int minZ = Math.min(from.z, to.z);
            int maxZ = Math.max(from.z, to.z);

            int minChunkX = minX >> 4;
            int maxChunkX = maxX >> 4;
            int minChunkZ = minZ >> 4;
            int maxChunkZ = maxZ >> 4;

            short width = (short) (Math.abs(maxX - minX) + 1);
            short height = (short) (Math.abs(maxY - minY) + 1);
            short length = (short) (Math.abs(maxZ - minZ) + 1);

            final SchematicWorld schematic = new SchematicWorld(null, width, height, length);

            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    copyChunkToSchematic(schematic, world, chunkX, chunkZ, minX, maxX, minY, maxY, minZ, maxZ);
                }
            }

            return schematic;
        } catch (Exception e) {
            Reference.logger.error("Failed to extract schematic!", e);
        }

        return null;
    }

    protected void copyChunkToSchematic(final SchematicWorld schematic, final World world, final int chunkX, final int chunkZ, final int minX, final int maxX, final int minY, final int maxY, final int minZ, final int maxZ) {
        final int localMinX = minX < (chunkX << 4) ? 0 : (minX & 15);
        final int localMaxX = maxX > ((chunkX << 4) + 15) ? 15 : (maxX & 15);
        final int localMinZ = minZ < (chunkZ << 4) ? 0 : (minZ & 15);
        final int localMaxZ = maxZ > ((chunkZ << 4) + 15) ? 15 : (maxZ & 15);

        for (int chunkLocalX = localMinX; chunkLocalX <= localMaxX; chunkLocalX++) {
            for (int chunkLocalZ = localMinZ; chunkLocalZ <= localMaxZ; chunkLocalZ++) {
                for (int y = minY; y <= maxY; y++) {
                    final int x = chunkLocalX | (chunkX << 4);
                    final int z = chunkLocalZ | (chunkZ << 4);

                    final int localX = x - minX;
                    final int localY = y - minY;
                    final int localZ = z - minZ;

                    final Block block = world.getBlock(x, y, z);
                    final int metadata = world.getBlockMetadata(x, y, z);
                    final boolean success = schematic.setBlock(localX, localY, localZ, block, metadata);

                    if (success && block.hasTileEntity(metadata)) {
                        final TileEntity tileEntity = world.getTileEntity(x, y, z);
                        if (tileEntity != null) {
                            try {
                                final TileEntity reloadedTileEntity = NBTHelper.reloadTileEntity(tileEntity, minX, minY, minZ);
                                schematic.setTileEntity(localX, localY, localZ, reloadedTileEntity);
                            } catch (TileEntityException e) {
                                Reference.logger.error(String.format("Error while trying to save tile entity '%s'!", tileEntity), e);
                                schematic.setBlock(localX, localY, localZ, Blocks.bedrock);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean saveSchematic(EntityPlayer player, File directory, String filename, World world, Vector3i from, Vector3i to) {
        try {
            String iconName = "";

            try {
                String[] parts = filename.split(";");
                if (parts.length == 2) {
                    iconName = parts[0];
                    filename = parts[1];
                }
            } catch (Exception e) {
                Reference.logger.error("Failed to parse icon data!", e);
            }

            SchematicWorld schematic = getSchematicFromWorld(world, from, to);
            schematic.setIcon(SchematicUtil.getIconFromName(iconName));
            SchematicFormat.writeToFile(directory, filename, schematic);

            return true;
        } catch (Exception e) {
            Reference.logger.error("Failed to save schematic!", e);
        }
        return false;
    }

    public abstract boolean loadSchematic(EntityPlayer player, File directory, String filename);

    public abstract void setActiveSchematic(SchematicWorld world);

    public abstract void setActiveSchematic(SchematicWorld world, EntityPlayer player);

    public abstract SchematicWorld getActiveSchematic();

    public abstract SchematicWorld getActiveSchematic(EntityPlayer player);
}
