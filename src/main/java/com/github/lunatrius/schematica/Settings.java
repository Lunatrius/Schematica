package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.client.gui.GuiSchematicControl;
import com.github.lunatrius.schematica.client.gui.GuiSchematicLoad;
import com.github.lunatrius.schematica.client.gui.GuiSchematicSave;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicChunk;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCache;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Settings {
	public static final Settings instance = new Settings();

	public KeyBinding[] keyBindings = new KeyBinding[] {
			new KeyBinding("schematica.key.load", Keyboard.KEY_DIVIDE, "schematica.key.category"),
			new KeyBinding("schematica.key.save", Keyboard.KEY_MULTIPLY, "schematica.key.category"),
			new KeyBinding("schematica.key.control", Keyboard.KEY_SUBTRACT, "schematica.key.category")
	};

	public static final File SCHEMATIC_DIRECTORY = new File(Minecraft.getMinecraft().mcDataDir, "/schematics/");
	public static final File TEXTURE_DIRECTORY = new File(Minecraft.getMinecraft().mcDataDir, "/resources/mod/schematica/");
	public static final RenderItem renderItem = new RenderItem();

	private final Vector3f translationVector = new Vector3f();
	public Minecraft minecraft = Minecraft.getMinecraft();
	public ChunkCache mcWorldCache = null;
	public SchematicWorld schematic = null;
	public Vector3f playerPosition = new Vector3f();
	public RendererSchematicChunk[][][] rendererSchematicChunk = null;
	public final List<RendererSchematicChunk> sortedRendererSchematicChunk = new ArrayList<RendererSchematicChunk>();
	public RenderBlocks renderBlocks = null;
	public Vector3f pointA = new Vector3f();
	public Vector3f pointB = new Vector3f();
	public Vector3f pointMin = new Vector3f();
	public Vector3f pointMax = new Vector3f();
	public int rotationRender = 0;
	public int orientation = 0;
	public Vector3f offset = new Vector3f();
	public boolean isRenderingSchematic = false;
	public int renderingLayer = -1;
	public boolean isRenderingGuide = false;
	public int chatLines = 0;
	public boolean isPrinterEnabled = true;
	public boolean isSaveEnabled = true;
	public boolean isLoadEnabled = true;
	public boolean isPrinting = false;
	public int[] increments = {
			1, 5, 15, 50, 250
	};

	private Settings() {
	}

	public void reset() {
		this.chatLines = 0;
		this.isPrinterEnabled = true;
		this.isSaveEnabled = true;
		this.isLoadEnabled = true;
		this.isRenderingSchematic = false;
		this.isRenderingGuide = false;
		this.schematic = null;
		this.renderBlocks = null;
		this.rendererSchematicChunk = null;
		this.mcWorldCache = null;
	}

	public void keyboardEvent(KeyBinding keybinding) {
		if (this.minecraft.currentScreen == null) {
			for (int i = 0; i < this.keyBindings.length; i++) {
				if (keybinding == this.keyBindings[i]) {
					keyboardEvent(i);
					break;
				}
			}
		}
	}

	public void keyboardEvent(int key) {
		switch (key) {
		case 0:
			this.minecraft.displayGuiScreen(new GuiSchematicLoad(this.minecraft.currentScreen));
			break;

		case 1:
			this.minecraft.displayGuiScreen(new GuiSchematicSave(this.minecraft.currentScreen));
			break;

		case 2:
			this.minecraft.displayGuiScreen(new GuiSchematicControl(this.minecraft.currentScreen));
			break;
		}
	}

	public void createRendererSchematicChunk() {
		int width = (this.schematic.getWidth() - 1) / RendererSchematicChunk.CHUNK_WIDTH + 1;
		int height = (this.schematic.getHeight() - 1) / RendererSchematicChunk.CHUNK_HEIGHT + 1;
		int length = (this.schematic.getLength() - 1) / RendererSchematicChunk.CHUNK_LENGTH + 1;

		this.rendererSchematicChunk = new RendererSchematicChunk[width][height][length];

		while (this.sortedRendererSchematicChunk.size() > 0) {
			this.sortedRendererSchematicChunk.remove(0).delete();
		}

		int x, y, z;
		for (x = 0; x < width; x++) {
			for (y = 0; y < height; y++) {
				for (z = 0; z < length; z++) {
					this.sortedRendererSchematicChunk.add(this.rendererSchematicChunk[x][y][z] = new RendererSchematicChunk(this.schematic, x, y, z));
				}
			}
		}
	}

	public boolean loadSchematic(String filename) {
		try {
			InputStream stream = new FileInputStream(filename);
			NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(stream);

			if (tagCompound != null) {
				Reference.logger.info(tagCompound);

				this.schematic = SchematicFormat.readFromFile(new File(filename));

				Reference.logger.info(String.format("Loaded %s [w:%d,h:%d,l:%d]", filename, this.schematic.getWidth(), this.schematic.getHeight(), this.schematic.getLength()));

				this.renderBlocks = new RenderBlocks(this.schematic);

				createRendererSchematicChunk();

				this.isRenderingSchematic = true;
			}
		} catch (Exception e) {
			Reference.logger.fatal("Failed to load schematic!", e);
			reset();
			return false;
		}

		return true;
	}

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
						blocks[x - minX][y - minY][z - minZ] = (short) GameData.blockRegistry.getId(this.minecraft.theWorld.getBlock(x, y, z));
						metadata[x - minX][y - minY][z - minZ] = (byte) this.minecraft.theWorld.getBlockMetadata(x, y, z);
						tileEntity = this.minecraft.theWorld.getTileEntity(x, y, z);
						if (tileEntity != null) {
							tileEntityNBT = new NBTTagCompound();
							tileEntity.writeToNBT(tileEntityNBT);

							tileEntity = TileEntity.createAndLoadEntity(tileEntityNBT);
							tileEntity.xCoord -= minX;
							tileEntity.yCoord -= minY;
							tileEntity.zCoord -= minZ;
							tileEntities.add(tileEntity);
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

	public Vector3f getTranslationVector() {
		Vector3f.sub(this.playerPosition, this.offset, this.translationVector);
		return this.translationVector;
	}

	public float getTranslationX() {
		return this.playerPosition.x - this.offset.x;
	}

	public float getTranslationY() {
		return this.playerPosition.y - this.offset.y;
	}

	public float getTranslationZ() {
		return this.playerPosition.z - this.offset.z;
	}

	public void refreshSchematic() {
		for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
			renderer.setDirty();
		}
	}

	public void updatePoints() {
		this.pointMin.x = Math.min(this.pointA.x, this.pointB.x);
		this.pointMin.y = Math.min(this.pointA.y, this.pointB.y);
		this.pointMin.z = Math.min(this.pointA.z, this.pointB.z);

		this.pointMax.x = Math.max(this.pointA.x, this.pointB.x);
		this.pointMax.y = Math.max(this.pointA.y, this.pointB.y);
		this.pointMax.z = Math.max(this.pointA.z, this.pointB.z);
	}

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

	public void moveHere() {
		this.offset.x = (int) Math.floor(this.playerPosition.x);
		this.offset.y = (int) Math.floor(this.playerPosition.y) - 1;
		this.offset.z = (int) Math.floor(this.playerPosition.z);

		if (this.schematic != null) {
			switch (this.rotationRender) {
			case 0:
				this.offset.x -= this.schematic.getWidth();
				this.offset.z += 1;
				break;
			case 1:
				this.offset.x -= this.schematic.getWidth();
				this.offset.z -= this.schematic.getLength();
				break;
			case 2:
				this.offset.x += 1;
				this.offset.z -= this.schematic.getLength();
				break;
			case 3:
				this.offset.x += 1;
				this.offset.z += 1;
				break;
			}

			reloadChunkCache();
		}
	}

	public void toggleRendering() {
		this.isRenderingSchematic = !this.isRenderingSchematic && (this.schematic != null);
	}

	public void reloadChunkCache() {
		if (this.schematic != null) {
			this.mcWorldCache = new ChunkCache(this.minecraft.theWorld, (int) this.offset.x - 1, (int) this.offset.y - 1, (int) this.offset.z - 1, (int) this.offset.x + this.schematic.getWidth() + 1, (int) this.offset.y + this.schematic.getHeight() + 1, (int) this.offset.z + this.schematic.getLength() + 1, 0);
			refreshSchematic();
		}
	}

	public void flipWorld() {
		if (this.schematic != null) {
			this.schematic.flip();
			createRendererSchematicChunk();
		}
	}

	public void rotateWorld() {
		if (this.schematic != null) {
			this.schematic.rotate();
			createRendererSchematicChunk();
		}
	}
}
