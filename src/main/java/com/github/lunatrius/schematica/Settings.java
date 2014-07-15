package com.github.lunatrius.schematica;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicChunk;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Settings {
	@Deprecated
	public static final Settings instance = new Settings();

	@Deprecated
	private final Vector3f translationVector = new Vector3f();
	@Deprecated
	public Minecraft minecraft = Minecraft.getMinecraft();
	@Deprecated
	public Vector3f playerPosition = new Vector3f();
	@Deprecated
	public final List<RendererSchematicChunk> sortedRendererSchematicChunk = new ArrayList<RendererSchematicChunk>();
	@Deprecated
	public RenderBlocks renderBlocks = null;
	@Deprecated
	public Vector3f pointA = new Vector3f();
	@Deprecated
	public Vector3f pointB = new Vector3f();
	@Deprecated
	public Vector3f pointMin = new Vector3f();
	@Deprecated
	public Vector3f pointMax = new Vector3f();
	@Deprecated
	public int rotationRender = 0;
	@Deprecated
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	@Deprecated
	public Vector3f offset = new Vector3f();
	@Deprecated
	public boolean isRenderingGuide = false;
	@Deprecated
	public int chatLines = 0;
	@Deprecated
	public boolean isSaveEnabled = true;
	@Deprecated
	public boolean isLoadEnabled = true;
	@Deprecated
	public boolean isPendingReset = false;
	@Deprecated
	public int[] increments = {
			1, 5, 15, 50, 250
	};

	@Deprecated
	private Settings() {
	}

	@Deprecated
	public void reset() {
		this.chatLines = 0;
		SchematicPrinter.INSTANCE.setEnabled(true);
		this.isSaveEnabled = true;
		this.isLoadEnabled = true;
		this.isRenderingGuide = false;
		Schematica.proxy.setActiveSchematic(null);
		this.renderBlocks = null;
		while (this.sortedRendererSchematicChunk.size() > 0) {
			this.sortedRendererSchematicChunk.remove(0).delete();
		}
		SchematicPrinter.INSTANCE.setSchematic(null);
	}

	@Deprecated
	public void createRendererSchematicChunk() {
		SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
		int width = (schematic.getWidth() - 1) / RendererSchematicChunk.CHUNK_WIDTH + 1;
		int height = (schematic.getHeight() - 1) / RendererSchematicChunk.CHUNK_HEIGHT + 1;
		int length = (schematic.getLength() - 1) / RendererSchematicChunk.CHUNK_LENGTH + 1;

		while (this.sortedRendererSchematicChunk.size() > 0) {
			this.sortedRendererSchematicChunk.remove(0).delete();
		}

		int x, y, z;
		for (x = 0; x < width; x++) {
			for (y = 0; y < height; y++) {
				for (z = 0; z < length; z++) {
					this.sortedRendererSchematicChunk.add(new RendererSchematicChunk(schematic, x, y, z));
				}
			}
		}
	}

	@Deprecated
	public boolean loadSchematic(String filename) {
		try {
			InputStream stream = new FileInputStream(filename);
			NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(stream);

			if (tagCompound != null) {
				Reference.logger.info(tagCompound);

				SchematicWorld schematic = SchematicFormat.readFromFile(new File(filename));
				Schematica.proxy.setActiveSchematic(schematic);

				Reference.logger.info(String.format("Loaded %s [w:%d,h:%d,l:%d]", filename, schematic.getWidth(), schematic.getHeight(), schematic.getLength()));

				this.renderBlocks = new RenderBlocks(schematic);

				createRendererSchematicChunk();

				schematic.setRendering(true);

				SchematicPrinter.INSTANCE.setSchematic(schematic);
			}
		} catch (Exception e) {
			Reference.logger.fatal("Failed to load schematic!", e);
			reset();
			return false;
		}

		return true;
	}

	@Deprecated
	public boolean saveSchematic(File directory, String filename, Vector3f from, Vector3f to) {
		try {
			int minX = (int) Math.min(from.x, to.x);
			int maxX = (int) Math.max(from.x, to.x);
			int minY = (int) Math.min(from.y, to.y);
			int maxY = (int) Math.max(from.y, to.y);
			int minZ = (int) Math.min(from.z, to.z);
			int maxZ = (int) Math.max(from.z, to.z);
			short width = (short) (Math.abs(maxX - minX) + 1);
			short height = (short) (Math.abs(maxY - minY) + 1);
			short length = (short) (Math.abs(maxZ - minZ) + 1);

			short[][][] blocks = new short[width][height][length];
			byte[][][] metadata = new byte[width][height][length];
			List<TileEntity> tileEntities = new ArrayList<TileEntity>();
			TileEntity tileEntity = null;
			NBTTagCompound tileEntityNBT = null;

			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
						blocks[x - minX][y - minY][z - minZ] = (short) GameData.getBlockRegistry().getId(this.minecraft.theWorld.getBlock(x, y, z));
						metadata[x - minX][y - minY][z - minZ] = (byte) this.minecraft.theWorld.getBlockMetadata(x, y, z);
						tileEntity = this.minecraft.theWorld.getTileEntity(x, y, z);
						if (tileEntity != null) {
							try {
								tileEntityNBT = new NBTTagCompound();
								tileEntity.writeToNBT(tileEntityNBT);

								tileEntity = TileEntity.createAndLoadEntity(tileEntityNBT);
								tileEntity.xCoord -= minX;
								tileEntity.yCoord -= minY;
								tileEntity.zCoord -= minZ;
								tileEntities.add(tileEntity);
							} catch (Exception e) {
								Reference.logger.error("Error while trying to save tile entity " + tileEntity + "!", e);
								blocks[x - minX][y - minY][z - minZ] = (short) GameData.getBlockRegistry().getId(Blocks.bedrock);
								metadata[x - minX][y - minY][z - minZ] = 0;
							}
						}
					}
				}
			}

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

			SchematicWorld schematicOut = new SchematicWorld(iconName, blocks, metadata, tileEntities, width, height, length);

			SchematicFormat.writeToFile(directory, filename, schematicOut);
		} catch (Exception e) {
			Reference.logger.error("Failed to save schematic!", e);
			return false;
		}
		return true;
	}

	@Deprecated
	public Vector3f getTranslationVector() {
		this.translationVector.set(this.playerPosition).sub(this.offset);
		return this.translationVector;
	}

	@Deprecated
	public float getTranslationX() {
		return this.playerPosition.x - this.offset.x;
	}

	@Deprecated
	public float getTranslationY() {
		return this.playerPosition.y - this.offset.y;
	}

	@Deprecated
	public float getTranslationZ() {
		return this.playerPosition.z - this.offset.z;
	}

	@Deprecated
	public void refreshSchematic() {
		for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
			renderer.setDirty();
		}
	}

	@Deprecated
	public void updatePoints() {
		this.pointMin.x = Math.min(this.pointA.x, this.pointB.x);
		this.pointMin.y = Math.min(this.pointA.y, this.pointB.y);
		this.pointMin.z = Math.min(this.pointA.z, this.pointB.z);

		this.pointMax.x = Math.max(this.pointA.x, this.pointB.x);
		this.pointMax.y = Math.max(this.pointA.y, this.pointB.y);
		this.pointMax.z = Math.max(this.pointA.z, this.pointB.z);
	}

	@Deprecated
	public void moveHere(Vector3f point) {
		point.x = (int) Math.floor(this.playerPosition.x);
		point.y = (int) Math.floor(this.playerPosition.y - 1);
		point.z = (int) Math.floor(this.playerPosition.z);

		switch (this.rotationRender) {
		case 0:
			point.x -= 1;
			point.z += 1;
			break;
		case 1:
			point.x -= 1;
			point.z -= 1;
			break;
		case 2:
			point.x += 1;
			point.z -= 1;
			break;
		case 3:
			point.x += 1;
			point.z += 1;
			break;
		}
	}

	@Deprecated
	public void moveHere() {
		this.offset.x = (int) Math.floor(this.playerPosition.x);
		this.offset.y = (int) Math.floor(this.playerPosition.y) - 1;
		this.offset.z = (int) Math.floor(this.playerPosition.z);

		SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
		if (schematic != null) {
			switch (this.rotationRender) {
			case 0:
				this.offset.x -= schematic.getWidth();
				this.offset.z += 1;
				break;
			case 1:
				this.offset.x -= schematic.getWidth();
				this.offset.z -= schematic.getLength();
				break;
			case 2:
				this.offset.x += 1;
				this.offset.z -= schematic.getLength();
				break;
			case 3:
				this.offset.x += 1;
				this.offset.z += 1;
				break;
			}
		}
	}
}
