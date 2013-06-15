package lunatrius.schematica.renderer;

import lunatrius.schematica.SchematicWorld;
import lunatrius.schematica.Settings;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.texturepacks.ITexturePack;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RendererSchematicChunk {
	public static final int CHUNK_WIDTH = 16;
	public static final int CHUNK_HEIGHT = 16;
	public static final int CHUNK_LENGTH = 16;

	private static boolean canUpdate = false;

	public boolean isInFrustrum = false;

	public final Vector3f centerPosition = new Vector3f();

	private final Settings settings = Settings.instance();
	private final Minecraft minecraft = this.settings.minecraft;
	private final Profiler profiler = this.minecraft.mcProfiler;
	private final SchematicWorld schematic;
	private final List<TileEntity> tileEntities = new ArrayList<TileEntity>();

	private final AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

	private static final Map<String, Integer> texturePacks = new HashMap<String, Integer>();

	private boolean needsUpdate = true;
	private int glList = -1;

	public RendererSchematicChunk(SchematicWorld schematicWorld, int baseX, int baseY, int baseZ) {
		this.schematic = schematicWorld;
		this.boundingBox.setBounds(baseX * CHUNK_WIDTH, baseY * CHUNK_HEIGHT, baseZ * CHUNK_LENGTH, (baseX + 1) * CHUNK_WIDTH, (baseY + 1) * CHUNK_HEIGHT, (baseZ + 1) * CHUNK_LENGTH);

		this.centerPosition.x = (int) ((baseX + 0.5) * CHUNK_WIDTH);
		this.centerPosition.y = (int) ((baseY + 0.5) * CHUNK_HEIGHT);
		this.centerPosition.z = (int) ((baseZ + 0.5) * CHUNK_LENGTH);

		int x, y, z;
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

	public void delete() {
		GL11.glDeleteLists(this.glList, 3);
	}

	public AxisAlignedBB getBoundingBox() {
		return this.boundingBox;
	}

	public static void setCanUpdate(boolean parCanUpdate) {
		canUpdate = parCanUpdate;
	}

	public static boolean getCanUpdate() {
		return canUpdate;
	}

	public void setDirty() {
		this.needsUpdate = true;
	}

	public boolean getDirty() {
		return this.needsUpdate;
	}

	public float distanceToPoint(Vector3f vector) {
		float x = vector.x - this.centerPosition.x;
		float y = vector.y - this.centerPosition.y;
		float z = vector.z - this.centerPosition.z;
		return x * x + y * y + z * z;
	}

	public void updateRenderer() {
		if (this.needsUpdate) {
			this.needsUpdate = false;
			setCanUpdate(false);

			RenderHelper.createBuffers();

			for (int pass = 0; pass < 3; pass++) {
				RenderHelper.initBuffers();

				int minX, maxX, minY, maxY, minZ, maxZ;

				minX = (int) this.boundingBox.minX;
				maxX = Math.min((int) this.boundingBox.maxX, this.schematic.width());
				minY = (int) this.boundingBox.minY;
				maxY = Math.min((int) this.boundingBox.maxY, this.schematic.height());
				minZ = (int) this.boundingBox.minZ;
				maxZ = Math.min((int) this.boundingBox.maxZ, this.schematic.length());

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

				int quadCount = RenderHelper.getQuadCount();
				int lineCount = RenderHelper.getLineCount();

				if (quadCount > 0 || lineCount > 0) {
					GL11.glDisable(GL11.GL_TEXTURE_2D);

					GL11.glLineWidth(1.5f);

					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

					if (quadCount > 0) {
						GL11.glVertexPointer(3, 0, RenderHelper.getQuadVertexBuffer());
						GL11.glColorPointer(4, 0, RenderHelper.getQuadColorBuffer());
						GL11.glDrawArrays(GL11.GL_QUADS, 0, quadCount);
					}

					if (lineCount > 0) {
						GL11.glVertexPointer(3, 0, RenderHelper.getLineVertexBuffer());
						GL11.glColorPointer(4, 0, RenderHelper.getLineColorBuffer());
						GL11.glDrawArrays(GL11.GL_LINES, 0, lineCount);
					}

					GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
					GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}

				GL11.glEndList();
			}

			RenderHelper.destroyBuffers();
		}
	}

	public void render(int renderPass) {
		if (!this.isInFrustrum) {
			return;
		}

		this.profiler.startSection("blocks");
		bindTexture();
		GL11.glCallList(this.glList + renderPass);

		this.profiler.endStartSection("tileEntities");
		renderTileEntities(renderPass);

		this.profiler.endSection();

		this.minecraft.renderEngine.resetBoundTexture();
	}

	public void renderBlocks(int renderPass, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		IBlockAccess mcWorld = this.settings.mcWorldCache;
		RenderBlocks renderBlocks = this.settings.renderBlocks;

		int x, y, z;
		int blockId, mcBlockId;
		int sides;
		Block block;
		Vector3f zero = new Vector3f();
		Vector3f size = new Vector3f();

		int ambientOcclusion = this.minecraft.gameSettings.ambientOcclusion;
		this.minecraft.gameSettings.ambientOcclusion = 0;

		Tessellator.instance.startDrawingQuads();

		for (y = minY; y < maxY; y++) {
			for (z = minZ; z < maxZ; z++) {
				for (x = minX; x < maxX; x++) {
					try {
						blockId = this.schematic.getBlockId(x, y, z);
						block = Block.blocksList[blockId];

						mcBlockId = mcWorld.getBlockId(x + (int) this.settings.offset.x, y + (int) this.settings.offset.y, z + (int) this.settings.offset.z);

						sides = 0;
						if (block != null) {
							if (block.shouldSideBeRendered(this.schematic, x, y - 1, z, 0)) {
								sides |= RenderHelper.QUAD_DOWN;
							}

							if (block.shouldSideBeRendered(this.schematic, x, y + 1, z, 1)) {
								sides |= RenderHelper.QUAD_UP;
							}

							if (block.shouldSideBeRendered(this.schematic, x, y, z - 1, 2)) {
								sides |= RenderHelper.QUAD_NORTH;
							}

							if (block.shouldSideBeRendered(this.schematic, x, y, z + 1, 3)) {
								sides |= RenderHelper.QUAD_SOUTH;
							}

							if (block.shouldSideBeRendered(this.schematic, x - 1, y, z, 4)) {
								sides |= RenderHelper.QUAD_WEST;
							}

							if (block.shouldSideBeRendered(this.schematic, x + 1, y, z, 5)) {
								sides |= RenderHelper.QUAD_EAST;
							}
						}

						if (mcBlockId != 0) {
							if (this.settings.highlight && renderPass == 2) {
								if (blockId == 0 && this.settings.highlightAir) {
									zero.set(x, y, z);
									size.set(x + 1, y + 1, z + 1);
									if (settings.drawQuads) {
										RenderHelper.drawCuboidSurface(zero, size, RenderHelper.QUAD_ALL, 0.75f, 0.0f, 0.75f, 0.25f);
									}
									if (settings.drawLines) {
										RenderHelper.drawCuboidOutline(zero, size, RenderHelper.QUAD_ALL, 0.75f, 0.0f, 0.75f, 0.25f);
									}
								} else if (blockId != mcBlockId) {
									zero.set(x, y, z);
									size.set(x + 1, y + 1, z + 1);
									if (settings.drawQuads) {
										RenderHelper.drawCuboidSurface(zero, size, sides, 1.0f, 0.0f, 0.0f, 0.25f);
									}
									if (settings.drawLines) {
										RenderHelper.drawCuboidOutline(zero, size, sides, 1.0f, 0.0f, 0.0f, 0.25f);
									}
								} else if (this.schematic.getBlockMetadata(x, y, z) != mcWorld.getBlockMetadata(x + (int) this.settings.offset.x, y + (int) this.settings.offset.y, z + (int) this.settings.offset.z)) {
									zero.set(x, y, z);
									size.set(x + 1, y + 1, z + 1);
									if (settings.drawQuads) {
										RenderHelper.drawCuboidSurface(zero, size, sides, 0.75f, 0.35f, 0.0f, 0.25f);
									}
									if (settings.drawLines) {
										RenderHelper.drawCuboidOutline(zero, size, sides, 0.75f, 0.35f, 0.0f, 0.25f);
									}
								}
							}
						} else if (mcBlockId == 0 && blockId > 0 && blockId < 0x1000) {
							if (this.settings.highlight && renderPass == 2) {
								zero.set(x, y, z);
								size.set(x + 1, y + 1, z + 1);
								if (settings.drawQuads) {
									RenderHelper.drawCuboidSurface(zero, size, sides, 0.0f, 0.75f, 1.0f, 0.25f);
								}
								if (settings.drawLines) {
									RenderHelper.drawCuboidOutline(zero, size, sides, 0.0f, 0.75f, 1.0f, 0.25f);
								}
							}

							if (block != null && block.canRenderInPass(renderPass)) {
								renderBlocks.renderBlockByRenderType(block, x, y, z);
							}
						}
					} catch (Exception e) {
						Settings.logger.logSevereException("Failed to render block!", e);
					}
				}
			}
		}

		Tessellator.instance.draw();

		this.minecraft.gameSettings.ambientOcclusion = ambientOcclusion;
	}

	public void renderTileEntities(int renderPass) {
		if (renderPass != 0) {
			return;
		}

		IBlockAccess mcWorld = this.settings.mcWorldCache;

		int x, y, z;
		int mcBlockId;

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

				mcBlockId = mcWorld.getBlockId(x + (int) this.settings.offset.x, y + (int) this.settings.offset.y, z + (int) this.settings.offset.z);

				if (mcBlockId == 0) {
					TileEntitySpecialRenderer tileEntitySpecialRenderer = TileEntityRenderer.instance.getSpecialRendererForEntity(tileEntity);
					if (tileEntitySpecialRenderer != null) {
						try {
							tileEntitySpecialRenderer.renderTileEntityAt(tileEntity, x, y, z, 0);

							OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
							GL11.glDisable(GL11.GL_TEXTURE_2D);
							OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
						} catch (Exception e) {
							Settings.logger.logSevereException("Failed to render a tile entity!", e);
						}
						GL11.glColor4f(1.0f, 1.0f, 1.0f, this.settings.alpha);
					}
				}
			}
		} catch (Exception ex) {
			Settings.logger.logSevereException("Failed to render tile entities!", ex);
		}
	}

	private void bindTexture() {
		if (!this.settings.enableAlpha) {
			this.minecraft.renderEngine.bindTexture("/terrain.png");
			return;
		}

		ITexturePack texturePackBase = this.minecraft.texturePackList.getSelectedTexturePack();
		String texturePackFileName = texturePackBase.getTexturePackFileName() + "-" + (int) (this.settings.alpha * 255);

		if (!texturePacks.containsKey(texturePackFileName)) {
			Texture texture = this.minecraft.renderEngine.textureMapBlocks.getTexture();
			int width = texture.getWidth();
			int height = texture.getHeight();
			ByteBuffer buffer = texture.getTextureData();
			byte[] byteArray = new byte[width * height * 4];
			buffer.position(0);
			buffer.get(byteArray);

			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			int x, y, offset, alpha, red, green, blue;

			for (y = 0; y < height; y++) {
				for (x = 0; x < width; x++) {
					offset = (y * width + x) * 4;

					alpha = (byteArray[offset + 3] & 0xFF);
					red = (byteArray[offset + 0] & 0xFF);
					green = (byteArray[offset + 1] & 0xFF);
					blue = (byteArray[offset + 2] & 0xFF);

					alpha *= this.settings.alpha;

					bufferedImage.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | (blue));
				}
			}

			texturePacks.put(texturePackFileName, this.minecraft.renderEngine.allocateAndSetupTexture(bufferedImage));
		}

		if (texturePacks.containsKey(texturePackFileName)) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePacks.get(texturePackFileName));
		}
	}
}
