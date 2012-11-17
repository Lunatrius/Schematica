package lunatrius.schematica;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import lunatrius.schematica.util.Vector3f;
import lunatrius.schematica.util.Vector3i;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ITexturePack;
import net.minecraft.src.Profiler;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.TileEntityEnderChest;
import net.minecraft.src.TileEntityRenderer;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class RendererSchematicChunk {
	public static final int CHUNK_WIDTH = 16;
	public static final int CHUNK_HEIGHT = 16;
	public static final int CHUNK_LENGTH = 16;

	public static boolean canUpdate = false;

	public boolean isInFrustrum = false;

	public final Vector3i centerPosition = new Vector3i();

	private final Settings settings = Settings.instance();
	private final Profiler profiler = this.settings.minecraft.mcProfiler;
	private final SchematicWorld schematic;
	private final List<TileEntity> tileEntities = new ArrayList<TileEntity>();

	private final AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

	private final int initialSize = 1152;

	private final List<String> textures = new ArrayList<String>();
	private final BufferedImage missingTextureImage = new BufferedImage(64, 64, 2);
	private Field fieldTextureMap = null;
	private Field fieldSingleIntBuffer = null;

	private float[] quadColorBuffer = new float[this.initialSize * 4];
	private float[] quadVertexBuffer = new float[this.initialSize * 3];
	private int quadColorIndex = -1;
	private int quadVertexIndex = -1;
	private int quadCount = -1;

	private float[] lineColorBuffer = new float[this.initialSize * 4];
	private float[] lineVertexBuffer = new float[this.initialSize * 3];
	private int lineColorIndex = -1;
	private int lineVertexIndex = -1;
	private int lineCount = -1;

	private boolean needsUpdate = true;
	private boolean empty = true;
	private int glList = -1;

	public RendererSchematicChunk(SchematicWorld schematicWorld, int baseX, int baseY, int baseZ) {
		initTexture();
		initReflection();

		this.schematic = schematicWorld;
		this.boundingBox.setBounds(baseX * CHUNK_WIDTH, baseY * CHUNK_HEIGHT, baseZ * CHUNK_LENGTH, (baseX + 1) * CHUNK_WIDTH, (baseY + 1) * CHUNK_HEIGHT, (baseZ + 1) * CHUNK_LENGTH);

		this.centerPosition.x = (int) ((baseX + 0.5) * CHUNK_WIDTH);
		this.centerPosition.y = (int) ((baseY + 0.5) * CHUNK_HEIGHT);
		this.centerPosition.z = (int) ((baseZ + 0.5) * CHUNK_LENGTH);

		int x, y, z;
		for (y = (int) this.boundingBox.minY; y < this.boundingBox.maxY; y++) {
			for (x = (int) this.boundingBox.minX; x < this.boundingBox.maxX; x++) {
				for (z = (int) this.boundingBox.minZ; z < this.boundingBox.maxZ; z++) {
					if (this.schematic.getBlockId(x, y, z) != 0) {
						this.empty = false;
					}
				}
			}
		}

		for (TileEntity tileEntity : this.schematic.getTileEntities()) {
			x = tileEntity.xCoord;
			y = tileEntity.yCoord;
			z = tileEntity.zCoord;

			if (x < this.boundingBox.minX || x >= this.boundingBox.maxX) {
				continue;
			} else if (z < this.boundingBox.minZ || z >= this.boundingBox.maxZ) {
				continue;
			} else if (y < this.boundingBox.minY || y >= this.boundingBox.maxY) {
				continue;
			}

			this.tileEntities.add(tileEntity);
		}

		this.glList = GL11.glGenLists(3);
	}

	private void initTexture() {
		Graphics graphics = this.missingTextureImage.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 64, 64);
		graphics.setColor(Color.BLACK);
		graphics.drawString("missingtex", 1, 10);
		graphics.dispose();
	}

	private void initReflection() {
		try {
			this.fieldTextureMap = ReflectionHelper.findField(RenderEngine.class, "c", "textureMap");
			this.fieldSingleIntBuffer = ReflectionHelper.findField(RenderEngine.class, "f", "singleIntBuffer");
		} catch (Exception e) {
			this.fieldTextureMap = null;
			this.fieldSingleIntBuffer = null;
			e.printStackTrace();
		}
	}

	public void delete() {
		GL11.glDeleteLists(this.glList, 3);
	}

	public AxisAlignedBB getBoundingBox() {
		return this.boundingBox;
	}

	public void setDirty() {
		if (!this.empty) {
			this.needsUpdate = true;
		}
	}

	public boolean getDirty() {
		return this.needsUpdate && !this.empty;
	}

	public float distanceToPoint(Vector3f vector) {
		float x = vector.x - this.centerPosition.x;
		float y = vector.y - this.centerPosition.y;
		float z = vector.z - this.centerPosition.z;
		return x * x + y * y + z * z;
	}

	public void updateRenderer() {
		if (this.empty) {
			return;
		}

		if (this.needsUpdate) {
			this.needsUpdate = false;
			canUpdate = false;

			for (int pass = 0; pass < 3; pass++) {
				this.quadColorIndex = 0;
				this.quadVertexIndex = 0;
				this.quadCount = 0;

				this.lineColorIndex = 0;
				this.lineVertexIndex = 0;
				this.lineCount = 0;

				int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;

				minX = (int) this.boundingBox.minX;
				maxX = (int) this.boundingBox.maxX;
				minY = (int) this.boundingBox.minY;
				maxY = (int) this.boundingBox.maxY;
				minZ = (int) this.boundingBox.minZ;
				maxZ = (int) this.boundingBox.maxZ;

				if (this.settings.renderingLayer >= 0) {
					if (this.settings.renderingLayer >= minY && this.settings.renderingLayer < maxY) {
						minY = this.settings.renderingLayer;
						maxY = this.settings.renderingLayer + 1;
					} else {
						minY = maxY = 0;
					}
				}

				GL11.glNewList(this.glList + pass, GL11.GL_COMPILE);
				renderBlocks(pass, minX, minY, minZ, maxX, maxY, maxZ);

				if (this.quadCount > 0 || this.lineCount > 0) {
					GL11.glDisable(GL11.GL_TEXTURE_2D);

					GL11.glLineWidth(1.5f);

					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

					FloatBuffer colorBuffer, vertexBuffer;

					if (this.quadCount > 0) {
						colorBuffer = BufferUtils.createFloatBuffer(this.quadColorBuffer.length).put(this.quadColorBuffer);
						vertexBuffer = BufferUtils.createFloatBuffer(this.quadVertexBuffer.length).put(this.quadVertexBuffer);

						colorBuffer.flip();
						vertexBuffer.flip();

						GL11.glColorPointer(4, 0, colorBuffer);
						GL11.glVertexPointer(3, 0, vertexBuffer);
						GL11.glDrawArrays(GL11.GL_QUADS, 0, this.quadCount);
					}

					if (this.lineCount > 0) {
						colorBuffer = BufferUtils.createFloatBuffer(this.lineColorBuffer.length).put(this.lineColorBuffer);
						vertexBuffer = BufferUtils.createFloatBuffer(this.lineVertexBuffer.length).put(this.lineVertexBuffer);

						colorBuffer.flip();
						vertexBuffer.flip();

						GL11.glColorPointer(4, 0, colorBuffer);
						GL11.glVertexPointer(3, 0, vertexBuffer);
						GL11.glDrawArrays(GL11.GL_LINES, 0, this.lineCount);
					}

					GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
					GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}

				GL11.glEndList();
			}
		}
	}

	public void render(int renderPass) {
		if (this.empty || !this.isInFrustrum) {
			return;
		}

		this.profiler.startSection("blocks");
		GL11.glCallList(this.glList + renderPass);

		this.profiler.endStartSection("tileEntities");
		renderTileEntities(renderPass);

		this.profiler.endSection();
	}

	public void renderBlocks(int renderPass, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		IBlockAccess mcWorld = this.settings.minecraft.theWorld;
		RenderBlocks renderBlocks = this.settings.renderBlocks;

		int x, y, z;
		int blockId = 0, mcBlockId = 0;
		int sides = 0;
		Block block = null;
		String lastTexture = "";
		Vector3i tmp;

		boolean ambientOcclusion = this.settings.minecraft.gameSettings.ambientOcclusion;
		this.settings.minecraft.gameSettings.ambientOcclusion = false;

		Tessellator.instance.startDrawingQuads();

		for (y = minY; y < maxY; y++) {
			for (z = minZ; z < maxZ; z++) {
				for (x = minX; x < maxX; x++) {
					try {
						blockId = this.schematic.getBlockId(x, y, z);
						block = Block.blocksList[blockId];

						mcBlockId = mcWorld.getBlockId(x + this.settings.offset.x, y + this.settings.offset.y, z + this.settings.offset.z);

						sides = 0;
						if (block != null) {
							if (block.shouldSideBeRendered(this.schematic, x, y - 1, z, 0)) {
								sides |= 0x01;
							}

							if (block.shouldSideBeRendered(this.schematic, x, y + 1, z, 1)) {
								sides |= 0x02;
							}

							if (block.shouldSideBeRendered(this.schematic, x, y, z - 1, 2)) {
								sides |= 0x04;
							}

							if (block.shouldSideBeRendered(this.schematic, x, y, z + 1, 3)) {
								sides |= 0x08;
							}

							if (block.shouldSideBeRendered(this.schematic, x - 1, y, z, 4)) {
								sides |= 0x10;
							}

							if (block.shouldSideBeRendered(this.schematic, x + 1, y, z, 5)) {
								sides |= 0x20;
							}
						}

						if (mcBlockId != 0) {
							if (this.settings.highlight && renderPass == 2) {
								if (blockId != mcBlockId) {
									tmp = new Vector3i(x, y, z);
									drawCuboidSurface(tmp, tmp.clone().add(1), sides, 1.0f, 0.0f, 0.0f, 0.25f);
									drawCuboidOutline(tmp, tmp.clone().add(1), sides, 1.0f, 0.0f, 0.0f, 0.25f);
								} else if (this.schematic.getBlockMetadata(x, y, z) != mcWorld.getBlockMetadata(x + this.settings.offset.x, y + this.settings.offset.y, z + this.settings.offset.z)) {
									tmp = new Vector3i(x, y, z);
									drawCuboidSurface(tmp, tmp.clone().add(1), sides, 0.75f, 0.35f, 0.0f, 0.45f);
									drawCuboidOutline(tmp, tmp.clone().add(1), sides, 0.75f, 0.35f, 0.0f, 0.45f);
								}
							}
						} else if (mcBlockId == 0 && blockId > 0 && blockId < 0x1000) {
							if (this.settings.highlight && renderPass == 2) {
								tmp = new Vector3i(x, y, z);
								drawCuboidSurface(tmp, tmp.clone().add(1), sides, 0.0f, 0.75f, 1.0f, 0.25f);
								drawCuboidOutline(tmp, tmp.clone().add(1), sides, 0.0f, 0.75f, 1.0f, 0.25f);
							}

							if (block != null) {
								if (lastTexture != block.getTextureFile()) {
									ForgeHooksClient.bindTexture(getTextureName(block.getTextureFile()), 0);
									lastTexture = block.getTextureFile();
								}

								if (block.canRenderInPass(renderPass)) {
									renderBlocks.renderBlockByRenderType(block, x, y, z);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		Tessellator.instance.draw();

		this.settings.minecraft.gameSettings.ambientOcclusion = ambientOcclusion;
	}

	public void renderTileEntities(int renderPass) {
		IBlockAccess mcWorld = this.settings.minecraft.theWorld;
		RendererTileEntity rendererTileEntity = this.settings.rendererTileEntity;

		int x, y, z;
		int mcBlockId = 0;

		GL11.glColor4f(1.0f, 1.0f, 1.0f, this.settings.alpha);

		try {
			for (TileEntity tileEntity : this.tileEntities) {
				x = tileEntity.xCoord;
				y = tileEntity.yCoord;
				z = tileEntity.zCoord;

				if (this.settings.renderingLayer >= 0) {
					if (y != this.settings.renderingLayer) {
						continue;
					}
				}

				mcBlockId = mcWorld.getBlockId(x + this.settings.offset.x, y + this.settings.offset.y, z + this.settings.offset.z);

				if (mcBlockId == 0) {
					if (tileEntity instanceof TileEntitySign) {
						rendererTileEntity.renderTileEntitySignAt((TileEntitySign) tileEntity);
					} else if (tileEntity instanceof TileEntityChest) {
						rendererTileEntity.renderTileEntityChestAt((TileEntityChest) tileEntity);
					} else if (tileEntity instanceof TileEntityEnderChest) {
						rendererTileEntity.renderTileEntityEnderChestAt((TileEntityEnderChest) tileEntity);
					} else {
						TileEntitySpecialRenderer tileEntitySpecialRenderer = TileEntityRenderer.instance.getSpecialRendererForEntity(tileEntity);
						if (tileEntitySpecialRenderer != null) {
							tileEntitySpecialRenderer.renderTileEntityAt(tileEntity, x, y, z, 0);
							GL11.glColor4f(1.0f, 1.0f, 1.0f, this.settings.alpha);
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void drawCuboidSurface(Vector3i a, Vector3i b, int sides, float red, float green, float blue, float alpha) {
		Vector3f zero = new Vector3f(a.x, a.y, a.z).sub(this.settings.blockDelta);
		Vector3f size = new Vector3f(b.x, b.y, b.z).add(this.settings.blockDelta);

		if (this.quadVertexIndex + 72 >= this.quadVertexBuffer.length) {
			float[] tempVertexBuffer = new float[this.quadVertexBuffer.length * 2];
			System.arraycopy(this.quadVertexBuffer, 0, tempVertexBuffer, 0, this.quadVertexBuffer.length);
			this.quadVertexBuffer = tempVertexBuffer;

			float[] tempColorBuffer = new float[this.quadColorBuffer.length * 2];
			System.arraycopy(this.quadColorBuffer, 0, tempColorBuffer, 0, this.quadColorBuffer.length);
			this.quadColorBuffer = tempColorBuffer;
		}

		int total = 0;

		// left
		if ((sides & 0x10) != 0) {
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			total += 4;
		}

		// right
		if ((sides & 0x20) != 0) {
			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			total += 4;
		}

		// near
		if ((sides & 0x04) != 0) {
			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			total += 4;
		}

		// far
		if ((sides & 0x08) != 0) {
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			total += 4;
		}

		// bottom
		if ((sides & 0x01) != 0) {
			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			total += 4;
		}

		// top
		if ((sides & 0x02) != 0) {
			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = zero.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = zero.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			this.quadVertexBuffer[this.quadVertexIndex++] = size.x;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.y;
			this.quadVertexBuffer[this.quadVertexIndex++] = size.z;
			this.quadCount++;

			total += 4;
		}

		for (int i = 0; i < total; i++) {
			this.quadColorBuffer[this.quadColorIndex++] = red;
			this.quadColorBuffer[this.quadColorIndex++] = green;
			this.quadColorBuffer[this.quadColorIndex++] = blue;
			this.quadColorBuffer[this.quadColorIndex++] = alpha;
		}
	}

	private void drawCuboidOutline(Vector3i a, Vector3i b, int sides, float red, float green, float blue, float alpha) {
		Vector3f zero = new Vector3f(a.x, a.y, a.z).sub(this.settings.blockDelta);
		Vector3f size = new Vector3f(b.x, b.y, b.z).add(this.settings.blockDelta);

		if (this.lineVertexIndex + 72 >= this.lineVertexBuffer.length) {
			float[] tempVertexBuffer = new float[this.lineVertexBuffer.length * 2];
			System.arraycopy(this.lineVertexBuffer, 0, tempVertexBuffer, 0, this.lineVertexBuffer.length);
			this.lineVertexBuffer = tempVertexBuffer;

			float[] tempColorBuffer = new float[this.lineColorBuffer.length * 2];
			System.arraycopy(this.lineColorBuffer, 0, tempColorBuffer, 0, this.lineColorBuffer.length);
			this.lineColorBuffer = tempColorBuffer;
		}

		int total = 0;

		// bottom left
		if ((sides & 0x11) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			total += 2;
		}

		// top left
		if ((sides & 0x12) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			total += 2;
		}

		// bottom right
		if ((sides & 0x21) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			total += 2;
		}

		// top right
		if ((sides & 0x22) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			total += 2;
		}

		// bottom near
		if ((sides & 0x05) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			total += 2;
		}

		// top near
		if ((sides & 0x06) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			total += 2;
		}

		// bottom far
		if ((sides & 0x09) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			total += 2;
		}

		// top far
		if ((sides & 0x0A) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			total += 2;
		}

		// near left
		if ((sides & 0x14) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			total += 2;
		}

		// near right
		if ((sides & 0x24) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.z;
			this.lineCount++;

			total += 2;
		}

		// far left
		if ((sides & 0x18) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = zero.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			total += 2;
		}

		// far right
		if ((sides & 0x28) != 0) {
			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = zero.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			this.lineVertexBuffer[this.lineVertexIndex++] = size.x;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.y;
			this.lineVertexBuffer[this.lineVertexIndex++] = size.z;
			this.lineCount++;

			total += 2;
		}

		for (int i = 0; i < total; i++) {
			this.lineColorBuffer[this.lineColorIndex++] = red;
			this.lineColorBuffer[this.lineColorIndex++] = green;
			this.lineColorBuffer[this.lineColorIndex++] = blue;
			this.lineColorBuffer[this.lineColorIndex++] = alpha;
		}
	}

	private String getTextureName(String texture) {
		if (!this.settings.enableAlpha) {
			return texture;
		}

		String textureName = "/" + (int) (this.settings.alpha * 255) + texture.replace('/', '-');

		if (this.textures.contains(textureName)) {
			return textureName;
		}

		try {
			ITexturePack texturePackBase = this.settings.minecraft.texturePackList.getSelectedTexturePack();
			File newTextureFile = new File(Settings.textureDirectory, texturePackBase.getTexturePackFileName().replace(".zip", "") + textureName);
			if (!newTextureFile.exists()) {
				BufferedImage bufferedImage = readTextureImage(texturePackBase.getResourceAsStream(texture));
				if (bufferedImage == null) {
					return texture;
				}

				int x, y, color;
				for (x = 0; x < bufferedImage.getWidth(); x++) {
					for (y = 0; y < bufferedImage.getHeight(); y++) {
						color = bufferedImage.getRGB(x, y);
						bufferedImage.setRGB(x, y, (((int) (((color >> 24) & 0xFF) * this.settings.alpha)) << 24) | (color & 0x00FFFFFF));
					}
				}

				newTextureFile.getParentFile().mkdirs();
				ImageIO.write(bufferedImage, "png", newTextureFile);
			}

			loadTexture(textureName, readTextureImage(new BufferedInputStream(new FileInputStream(newTextureFile))));

			this.textures.add(textureName);
			return textureName;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return texture;
	}

	private int loadTexture(String texture, BufferedImage textureImage) throws IllegalArgumentException, IllegalAccessException {
		HashMap<String, Integer> textureMap = (HashMap<String, Integer>) this.fieldTextureMap.get(this.settings.minecraft.renderEngine);
		IntBuffer singleIntBuffer = (IntBuffer) this.fieldSingleIntBuffer.get(this.settings.minecraft.renderEngine);

		Integer textureId = textureMap.get(texture);

		if (textureId != null) {
			return textureId.intValue();
		}

		try {
			singleIntBuffer.clear();
			GLAllocation.generateTextureNames(singleIntBuffer);
			int glTextureId = singleIntBuffer.get(0);
			this.settings.minecraft.renderEngine.setupTexture(textureImage, glTextureId);
			textureMap.put(texture, Integer.valueOf(glTextureId));
			return glTextureId;
		} catch (Exception e) {
			e.printStackTrace();
			GLAllocation.generateTextureNames(singleIntBuffer);
			int glTextureId = singleIntBuffer.get(0);
			this.settings.minecraft.renderEngine.setupTexture(this.missingTextureImage, glTextureId);
			textureMap.put(texture, Integer.valueOf(glTextureId));
			return glTextureId;
		}
	}

	private BufferedImage readTextureImage(InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		inputStream.close();
		return bufferedImage;
	}
}
