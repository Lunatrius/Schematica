package lunatrius.schematica;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import lunatrius.schematica.util.Vector3f;
import lunatrius.schematica.util.Vector3i;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ChunkCache;
import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.TileEntity;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;

public class Settings {
	private final static Settings instance = new Settings();
	private final StringTranslate strTranslate = StringTranslate.getInstance();

	// loaded from config
	public boolean enableAlpha = false;
	public float alpha = 1.0f;
	public boolean highlight = true;
	public boolean highlightAir = true;
	public float blockDelta = 0.005f;

	public KeyBinding[] keyBindings = new KeyBinding[] {
			new KeyBinding("key.schematic.load", Keyboard.KEY_DIVIDE),
			new KeyBinding("key.schematic.save", Keyboard.KEY_MULTIPLY),
			new KeyBinding("key.schematic.control", Keyboard.KEY_SUBTRACT)
	};

	public static final File schematicDirectory = new File(Minecraft.getMinecraftDir(), "/schematics/");
	public static final File textureDirectory = new File(Minecraft.getMinecraftDir(), "/resources/mod/schematica/");
	public static final Logger logger = FMLCommonHandler.instance().getFMLLogger();
	public Minecraft minecraft = Minecraft.getMinecraft();
	public ChunkCache mcWorldCache = null;
	public SchematicWorld schematic = null;
	public Vector3f playerPosition = new Vector3f();
	public RendererSchematicChunk[][][] rendererSchematicChunk = null;
	public final List<RendererSchematicChunk> sortedRendererSchematicChunk = new ArrayList<RendererSchematicChunk>();
	public RenderBlocks renderBlocks = null;
	public RendererTileEntity rendererTileEntity = null;
	public int selectedSchematic = 0;
	public Vector3i pointA = new Vector3i();
	public Vector3i pointB = new Vector3i();
	public Vector3i pointMin = new Vector3i();
	public Vector3i pointMax = new Vector3i();
	public int rotationRender = 0;
	public Vector3i offset = new Vector3i();
	public boolean isRenderingSchematic = false;
	public int renderingLayer = -1;
	public boolean isRenderingGuide = false;
	public int[] increments = {
			1, 5, 15, 50, 250
	};

	private Settings() {
	}

	public static Settings instance() {
		return instance;
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

	public List<String> getSchematicFiles() {
		ArrayList<String> schematicFiles = new ArrayList<String>();
		schematicFiles.add(this.strTranslate.translateKey("schematic.noschematic"));

		File[] files = schematicDirectory.listFiles(new FileFilterSchematic());
		for (int i = 0; i < files.length; i++) {
			schematicFiles.add(files[i].getName());
		}
		return schematicFiles;
	}

	public void createRendererSchematicChunk() {
		int width = (this.schematic.width() - 1) / RendererSchematicChunk.CHUNK_WIDTH + 1;
		int height = (this.schematic.height() - 1) / RendererSchematicChunk.CHUNK_HEIGHT + 1;
		int length = (this.schematic.width() - 1) / RendererSchematicChunk.CHUNK_LENGTH + 1;
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
				this.schematic = new SchematicWorld();
				this.schematic.readFromNBT(tagCompound);

				this.renderBlocks = new RenderBlocks(this.schematic);
				this.rendererTileEntity = new RendererTileEntity(this.schematic);

				createRendererSchematicChunk();

				this.isRenderingSchematic = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.schematic = null;
			this.renderBlocks = null;
			this.rendererTileEntity = null;
			this.rendererSchematicChunk = null;
			this.isRenderingSchematic = false;
			return false;
		}

		return true;
	}

	public boolean saveSchematic(String filename, Vector3i from, Vector3i to) {
		try {
			NBTTagCompound tagCompound = new NBTTagCompound();

			int minX = Math.min(from.x, to.x);
			int maxX = Math.max(from.x, to.x);
			int minY = Math.min(from.y, to.y);
			int maxY = Math.max(from.y, to.y);
			int minZ = Math.min(from.z, to.z);
			int maxZ = Math.max(from.z, to.z);
			short width = (short) (Math.abs(maxX - minX) + 1);
			short height = (short) (Math.abs(maxY - minY) + 1);
			short length = (short) (Math.abs(maxZ - minZ) + 1);

			int[][][] blocks = new int[width][height][length];
			int[][][] metadata = new int[width][height][length];
			List<TileEntity> tileEntities = new ArrayList<TileEntity>();

			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
						blocks[x - minX][y - minY][z - minZ] = this.minecraft.theWorld.getBlockId(x, y, z);
						metadata[x - minX][y - minY][z - minZ] = this.minecraft.theWorld.getBlockMetadata(x, y, z);
						if (this.minecraft.theWorld.getBlockTileEntity(x, y, z) != null) {
							NBTTagCompound te = new NBTTagCompound();
							this.minecraft.theWorld.getBlockTileEntity(x, y, z).writeToNBT(te);

							TileEntity tileEntity = TileEntity.createAndLoadEntity(te);
							tileEntity.xCoord -= minX;
							tileEntity.yCoord -= minY;
							tileEntity.zCoord -= minZ;
							tileEntities.add(tileEntity);
						}
					}
				}
			}

			SchematicWorld schematicOut = new SchematicWorld(blocks, metadata, tileEntities, width, height, length);
			schematicOut.writeToNBT(tagCompound);

			OutputStream stream = new FileOutputStream(filename);
			CompressedStreamTools.writeCompressed(tagCompound, stream);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

	public void moveHere(Vector3i point) {
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
				this.offset.x -= this.schematic.width();
				this.offset.z += 1;
				break;
			case 1:
				this.offset.x -= this.schematic.width();
				this.offset.z -= this.schematic.length();
				break;
			case 2:
				this.offset.x += 1;
				this.offset.z -= this.schematic.length();
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
			this.mcWorldCache = new ChunkCache(this.minecraft.theWorld, this.offset.x - 1, this.offset.y - 1, this.offset.z - 1, this.offset.x + this.schematic.width() + 1, this.offset.y + this.schematic.height() + 1, this.offset.z + this.schematic.length() + 1);
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
