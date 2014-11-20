package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

            short[][][] blocks = new short[width][height][length];
            byte[][][] metadata = new byte[width][height][length];
            List<TileEntity> tileEntities = new ArrayList<TileEntity>();

            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    int localMinX = minX < (chunkX << 4) ? 0 : (minX & 15);
                    int localMaxX = maxX > ((chunkX << 4) + 15) ? 15 : (maxX & 15);
                    int localMinZ = minZ < (chunkZ << 4) ? 0 : (minZ & 15);
                    int localMaxZ = maxZ > ((chunkZ << 4) + 15) ? 15 : (maxZ & 15);

                    for (int chunkLocalX = localMinX; chunkLocalX <= localMaxX; chunkLocalX++) {
                        for (int y = minY; y <= maxY; y++) {
                            for (int chunkLocalZ = localMinZ; chunkLocalZ <= localMaxZ; chunkLocalZ++) {
                                int x = chunkLocalX | (chunkX << 4);
                                int z = chunkLocalZ | (chunkZ << 4);

                                blocks[x - minX][y - minY][z - minZ] = (short) GameData.getBlockRegistry().getId(world.getBlock(x, y, z));
                                metadata[x - minX][y - minY][z - minZ] = (byte) world.getBlockMetadata(x, y, z);

                                TileEntity tileEntity = world.getTileEntity(x, y, z);
                                if (tileEntity != null) {
                                    try {
                                        NBTTagCompound tileEntityNBT = new NBTTagCompound();
                                        tileEntity.writeToNBT(tileEntityNBT);

                                        tileEntity = TileEntity.createAndLoadEntity(tileEntityNBT);
                                        tileEntity.xCoord -= minX;
                                        tileEntity.yCoord -= minY;
                                        tileEntity.zCoord -= minZ;
                                        tileEntities.add(tileEntity);
                                    } catch (Exception e) {
                                        Reference.logger.error(String.format("Error while trying to save tile entity %s!", tileEntity), e);
                                        blocks[x - minX][y - minY][z - minZ] = (short) GameData.getBlockRegistry().getId(Blocks.bedrock);
                                        metadata[x - minX][y - minY][z - minZ] = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return new SchematicWorld("", blocks, metadata, tileEntities, width, height, length);
        } catch (Exception e) {
            Reference.logger.error("Failed to extract schematic!", e);
        }

        return null;
    }

    public abstract boolean saveSchematic(EntityPlayer player, File directory, String filename, World world, Vector3i from, Vector3i to);

    public abstract boolean loadSchematic(EntityPlayer player, File directory, String filename);

    public abstract void setActiveSchematic(SchematicWorld world);

    public abstract void setActiveSchematic(SchematicWorld world, EntityPlayer player);

    public abstract SchematicWorld getActiveSchematic();

    public abstract SchematicWorld getActiveSchematic(EntityPlayer player);
}
