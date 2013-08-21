package lunatrius.schematica.renderer;

import cpw.mods.fml.relauncher.ReflectionHelper;
import lunatrius.schematica.SchematicWorld;
import lunatrius.schematica.Settings;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

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

	private static final Map<String, ResourceLocation> resourcePacks = new HashMap<String, ResourceLocation>();
	private Field fieldMapTexturesStiched;

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

		try {
			this.fieldMapTexturesStiched = ReflectionHelper.findField(TextureMap.class, "f", "field_94252_e", "mapTexturesStiched");
		} catch (Exception ex) {
			Settings.logger.logSevereException("Failed to initialize mapTexturesStiched!", ex);
			this.fieldMapTexturesStiched = null;
		}
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

		// TODO: remove if there are no issues
		// this.minecraft.renderEngine.resetBoundTexture();
	}

	public void renderBlocks(int renderPass, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		IBlockAccess mcWorld = this.settings.mcWorldCache;
		RenderBlocks renderBlocks = this.settings.renderBlocks;

		int x, y, z, wx, wy, wz;
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

						wx = (int) this.settings.offset.x + x;
						wy = (int) this.settings.offset.y + y;
						wz = (int) this.settings.offset.z + z;

						mcBlockId = mcWorld.getBlockId(wx, wy, wz);

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

						boolean isAirBlock = mcWorld.isAirBlock(wx, wy, wz);

						if (!isAirBlock) {
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
								} else if (this.schematic.getBlockMetadata(x, y, z) != mcWorld.getBlockMetadata(wx, wy, wz)) {
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
						} else if (isAirBlock && blockId > 0 && blockId < 0x1000) {
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
			this.minecraft.renderEngine.func_110577_a(TextureMap.field_110575_b);
			return;
		}

		String resourcePackName = this.minecraft.func_110438_M().func_110610_d();

		if (!resourcePacks.containsKey(resourcePackName)) {
			String texturePackFileName = resourcePackName.replaceAll("(?i)[^a-z0-9]", "_") + "-" + (int) (this.settings.alpha * 255) + ".png";

			try {
				File outputfile = new File("assets/" + texturePackFileName);

				ResourceManager manager = this.minecraft.func_110442_L();

				Icon icon = Block.dirt.getIcon(0, 0);
				float deltaU = icon.getMaxU() - icon.getMinU();
				float deltaV = icon.getMaxV() - icon.getMinV();

				int width = (int) Math.pow(2, Math.round(Math.log(icon.getOriginX() / deltaU) / Math.log(2)));
				int height = (int) Math.pow(2, Math.round(Math.log(icon.getOriginY() / deltaV) / Math.log(2)));

				BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

				Map<String, TextureAtlasSprite> map = (Map<String, TextureAtlasSprite>) fieldMapTexturesStiched.get(this.minecraft.renderEngine.func_110581_b(TextureMap.field_110575_b));
				if (map == null) {
					Settings.logger.logSevere("mapTexturesStiched is null!");
					resourcePacks.put(resourcePackName, TextureMap.field_110575_b);
					return;
				}

				Collection<TextureAtlasSprite> sprites = map.values();

				for (TextureAtlasSprite sprite : sprites) {
					ResourceLocation resourcelocation = new ResourceLocation(ForgeHooksClient.fixDomain("textures/blocks/", sprite.getIconName()) + ".png");

					try {
						sprite.load(manager, resourcelocation);
					} catch (RuntimeException ignored) {
					} catch (IOException ignored) {
					}
				}

				for (TextureAtlasSprite sprite : sprites) {
					if (sprite.func_110970_k() != 0) {
						int[] data = sprite.func_110965_a(0);
						int offsetX = sprite.func_130010_a();
						int offsetY = sprite.func_110967_i();

						int x, y;
						int color, alpha, index = 0;

						for (y = 0; y < sprite.getOriginY(); y++) {
							for (x = 0; x < sprite.getOriginX(); x++) {
								color = data[index++];
								alpha = (color >> 24) & 0xFF;
								alpha *= this.settings.alpha;
								color = (color & 0x00FFFFFF) | (alpha << 24);
								bufferedImage.setRGB(offsetX + x, offsetY + y, color);
							}
						}
					}
				}

				ImageIO.write(bufferedImage, "png", outputfile);

				for (TextureAtlasSprite sprite : sprites) {
					if (!sprite.func_130098_m()) {
						sprite.func_130103_l();
					}
				}

				resourcePacks.put(resourcePackName, new ResourceLocation(texturePackFileName));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (resourcePacks.containsKey(resourcePackName)) {
			this.minecraft.renderEngine.func_110577_a(resourcePacks.get(resourcePackName));
		}
	}
}
