package com.github.lunatrius.schematica.proxy;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.handler.ConfigurationHandler;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
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

	public File getDataDirectory() {
		return MinecraftServer.getServer().getFile(".");
	}

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
			short width = (short) (Math.abs(maxX - minX) + 1);
			short height = (short) (Math.abs(maxY - minY) + 1);
			short length = (short) (Math.abs(maxZ - minZ) + 1);

			short[][][] blocks = new short[width][height][length];
			byte[][][] metadata = new byte[width][height][length];
			List<TileEntity> tileEntities = new ArrayList<TileEntity>();

			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
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
