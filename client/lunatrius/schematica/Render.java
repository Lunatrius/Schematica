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
import java.util.logging.Level;

import javax.imageio.ImageIO;

import lunatrius.schematica.util.Vector3f;
import lunatrius.schematica.util.Vector3i;
import lunatrius.schematica.util.Vector4i;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ITexturePack;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Render {
	private final Settings settings = Settings.instance();
	private final List<String> textures = new ArrayList<String>();
	private final BufferedImage missingTextureImage = new BufferedImage(64, 64, 2);
	private Field fieldTextureMap = null;
	private Field fieldSingleIntBuffer = null;

	private final int glBlockList = GL11.glGenLists(1);

	private int bufferSizeQuad = 1048576;
	private FloatBuffer cBufferQuad = BufferUtils.createFloatBuffer(this.bufferSizeQuad * 4);
	private FloatBuffer vBufferQuad = BufferUtils.createFloatBuffer(this.bufferSizeQuad * 3);
	private int objectCountQuad = -1;
	private boolean needsExpansionQuad = false;

	private int bufferSizeLine = 1048576;
	private FloatBuffer cBufferLine = BufferUtils.createFloatBuffer(this.bufferSizeLine * 4);
	private FloatBuffer vBufferLine = BufferUtils.createFloatBuffer(this.bufferSizeLine * 3);
	private int objectCountLine = -1;
	private boolean needsExpansionLine = false;

	public Render() {
		initTexture();
		initReflection();
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
			this.fieldTextureMap = RenderEngine.class.getDeclaredField("c");
			this.fieldTextureMap.setAccessible(true);
			this.fieldSingleIntBuffer = RenderEngine.class.getDeclaredField("f");
			this.fieldSingleIntBuffer.setAccessible(true);
		} catch (Exception e1) {
			this.fieldTextureMap = null;
			this.fieldSingleIntBuffer = null;

			try {
				this.fieldTextureMap = RenderEngine.class.getDeclaredField("textureMap");
				this.fieldTextureMap.setAccessible(true);
				this.fieldSingleIntBuffer = RenderEngine.class.getDeclaredField("singleIntBuffer");
				this.fieldSingleIntBuffer.setAccessible(true);
			} catch (Exception e2) {
				e2.printStackTrace();
				this.fieldTextureMap = null;
				this.fieldSingleIntBuffer = null;
				this.settings.enableAlpha = false;
			}
		}
	}

	@ForgeSubscribe
	public void onRender(RenderWorldLastEvent event) {
		if (this.settings.minecraft != null) {
			EntityPlayerSP player = this.settings.minecraft.thePlayer;
			if (player != null) {
				this.settings.playerPosition.x = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks);
				this.settings.playerPosition.y = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks);
				this.settings.playerPosition.z = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks);

				this.settings.rotationRender = (int) (((player.rotationYaw / 90) % 4 + 4) % 4);

				render();
			}
		}
	}

	private void render() {
		GL11.glPushMatrix();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);

		if (this.needsExpansionQuad) {
			Settings.logger.log(Level.INFO, "Expanding QUADS buffer...");
			this.bufferSizeQuad *= 2;
			this.cBufferQuad = BufferUtils.createFloatBuffer(this.bufferSizeQuad * 4);
			this.vBufferQuad = BufferUtils.createFloatBuffer(this.bufferSizeQuad * 3);
			this.needsExpansionQuad = false;
		}

		if (this.needsExpansionLine) {
			Settings.logger.log(Level.INFO, "Expanding LINES buffer...");
			this.bufferSizeLine *= 2;
			this.cBufferLine = BufferUtils.createFloatBuffer(this.bufferSizeLine * 4);
			this.vBufferLine = BufferUtils.createFloatBuffer(this.bufferSizeLine * 3);
			this.needsExpansionLine = false;
		}

		int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;

		if (this.settings.schematic != null) {
			maxX = this.settings.schematic.width();
			maxY = this.settings.schematic.height();
			maxZ = this.settings.schematic.length();

			if (this.settings.renderingLayer >= 0) {
				minY = this.settings.renderingLayer;
				maxY = this.settings.renderingLayer + 1;
			}
		}

		if (this.settings.needsUpdate) {
			this.objectCountQuad = 0;
			this.objectCountLine = 0;

			this.cBufferQuad.clear();
			this.vBufferQuad.clear();
			this.cBufferLine.clear();
			this.vBufferLine.clear();

			GL11.glNewList(this.glBlockList, GL11.GL_COMPILE);

			if (this.settings.isRenderingSchematic && this.settings.schematic != null) {
				renderBlocks(minX, minY, minZ, maxX, maxY, maxZ);
			}

			if (this.settings.isRenderingGuide) {
				renderGuide();
			}

			GL11.glEndList();

			this.settings.needsUpdate = false;
		}

		GL11.glTranslatef(-this.settings.getTranslationX(), -this.settings.getTranslationY(), -this.settings.getTranslationZ());
		GL11.glCallList(this.glBlockList);

		if (this.settings.isRenderingSchematic && this.settings.schematic != null) {
			renderTileEntities(minX, minY, minZ, maxX, maxY, maxZ);
		}

		if (this.objectCountQuad > 0 || this.objectCountLine > 0) {
			this.cBufferQuad.flip();
			this.vBufferQuad.flip();

			this.cBufferLine.flip();
			this.vBufferLine.flip();

			GL11.glDisable(GL11.GL_TEXTURE_2D);

			GL11.glLineWidth(1.5f);

			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

			if (this.objectCountQuad > 0) {
				GL11.glColorPointer(4, 0, this.cBufferQuad);
				GL11.glVertexPointer(3, 0, this.vBufferQuad);
				GL11.glDrawArrays(GL11.GL_QUADS, 0, this.objectCountQuad);
			}

			if (this.objectCountLine > 0) {
				GL11.glColorPointer(4, 0, this.cBufferLine);
				GL11.glVertexPointer(3, 0, this.vBufferLine);
				GL11.glDrawArrays(GL11.GL_LINES, 0, this.objectCountLine);
			}

			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		GL11.glTranslatef(this.settings.getTranslationX(), +this.settings.getTranslationY(), +this.settings.getTranslationZ());

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glPopMatrix();
	}

	private void renderBlocks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		IBlockAccess mcWorld = this.settings.minecraft.theWorld;
		SchematicWorld world = this.settings.schematic;
		RenderBlocks renderBlocks = this.settings.renderBlocks;
		List<Vector4i> invalidBlockId = new ArrayList<Vector4i>();
		List<Vector4i> invalidBlockMetadata = new ArrayList<Vector4i>();
		List<Vector4i> todoBlocks = new ArrayList<Vector4i>();

		int x, y, z;
		int blockId = 0, mcBlockId = 0;
		int sides = 0;
		Block block = null;
		String lastTexture = "";

		boolean ambientOcclusion = this.settings.minecraft.gameSettings.ambientOcclusion;
		this.settings.minecraft.gameSettings.ambientOcclusion = false;

		Tessellator.instance.startDrawingQuads();

		for (x = minX; x < maxX; x++) {
			for (y = minY; y < maxY; y++) {
				for (z = minZ; z < maxZ; z++) {
					try {
						blockId = world.getBlockId(x, y, z);
						block = Block.blocksList[blockId];

						mcBlockId = mcWorld.getBlockId(x + this.settings.offset.x, y + this.settings.offset.y, z + this.settings.offset.z);

						sides = 0;
						if (block != null) {
							if (block.shouldSideBeRendered(world, x, y - 1, z, 0)) {
								sides |= 0x01;
							}

							if (block.shouldSideBeRendered(world, x, y + 1, z, 1)) {
								sides |= 0x02;
							}

							if (block.shouldSideBeRendered(world, x, y, z - 1, 2)) {
								sides |= 0x04;
							}

							if (block.shouldSideBeRendered(world, x, y, z + 1, 3)) {
								sides |= 0x08;
							}

							if (block.shouldSideBeRendered(world, x - 1, y, z, 4)) {
								sides |= 0x10;
							}

							if (block.shouldSideBeRendered(world, x + 1, y, z, 5)) {
								sides |= 0x20;
							}
						}

						if (mcBlockId != 0) {
							if (this.settings.highlight) {
								if (blockId != mcBlockId) {
									invalidBlockId.add(new Vector4i(x, y, z, sides));
								} else if (world.getBlockMetadata(x, y, z) != mcWorld.getBlockMetadata(x + this.settings.offset.x, y + this.settings.offset.y, z + this.settings.offset.z)) {
									invalidBlockMetadata.add(new Vector4i(x, y, z, sides));
								}
							}
						} else if (mcBlockId == 0 && blockId > 0 && blockId < 0x1000) {
							if (this.settings.highlight) {
								todoBlocks.add(new Vector4i(x, y, z, sides));
							}

							if (block != null) {
								if (lastTexture != block.getTextureFile()) {
									ForgeHooksClient.bindTexture(getTextureName(block.getTextureFile()), 0);
									lastTexture = block.getTextureFile();
								}

								renderBlocks.renderBlockByRenderType(block, x, y, z);
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

		drawCuboidLine(Vector3i.ZERO, new Vector3i(world.width(), world.height(), world.length()), 0x3F, 0.75f, 0.0f, 0.75f, 0.25f);

		Vector3i tmp;
		for (Vector4i invalidBlock : invalidBlockId) {
			tmp = new Vector3i(invalidBlock.x, invalidBlock.y, invalidBlock.z);

			drawCuboidQuad(tmp, tmp.clone().add(1), invalidBlock.w, 1.0f, 0.0f, 0.0f, 0.25f);
			drawCuboidLine(tmp, tmp.clone().add(1), invalidBlock.w, 1.0f, 0.0f, 0.0f, 0.25f);
		}

		for (Vector4i invalidBlock : invalidBlockMetadata) {
			tmp = new Vector3i(invalidBlock.x, invalidBlock.y, invalidBlock.z);

			drawCuboidQuad(tmp, tmp.clone().add(1), invalidBlock.w, 0.75f, 0.35f, 0.0f, 0.45f);
			drawCuboidLine(tmp, tmp.clone().add(1), invalidBlock.w, 0.75f, 0.35f, 0.0f, 0.45f);
		}

		for (Vector4i todoBlock : todoBlocks) {
			tmp = new Vector3i(todoBlock.x, todoBlock.y, todoBlock.z);

			drawCuboidQuad(tmp, tmp.clone().add(1), todoBlock.w, 0.0f, 0.75f, 1.0f, 0.25f);
			drawCuboidLine(tmp, tmp.clone().add(1), todoBlock.w, 0.0f, 0.75f, 1.0f, 0.25f);
		}
	}

	private void renderTileEntities(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		IBlockAccess mcWorld = this.settings.minecraft.theWorld;
		SchematicWorld world = this.settings.schematic;
		RenderTileEntity renderTileEntity = this.settings.renderTileEntity;

		int x, y, z;
		int mcBlockId = 0;

		GL11.glColor4f(1.0f, 1.0f, 1.0f, this.settings.alpha);

		try {
			for (TileEntity tileEntity : world.getTileEntities()) {
				x = tileEntity.xCoord;
				y = tileEntity.yCoord;
				z = tileEntity.zCoord;

				if (x < minX || x >= maxX) {
					continue;
				} else if (z < minZ || z >= maxZ) {
					continue;
				} else if (y < minY || y >= maxY) {
					continue;
				}

				mcBlockId = mcWorld.getBlockId(x + this.settings.offset.x, y + this.settings.offset.y, z + this.settings.offset.z);

				if (mcBlockId == 0) {
					if (tileEntity instanceof TileEntitySign) {
						renderTileEntity.renderTileEntitySignAt((TileEntitySign) tileEntity);
					} else if (tileEntity instanceof TileEntityChest) {
						renderTileEntity.renderTileEntityChestAt((TileEntityChest) tileEntity);
					} else if (tileEntity instanceof TileEntityEnderChest) {
						renderTileEntity.renderTileEntityEnderChestAt((TileEntityEnderChest) tileEntity);
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

	private void renderGuide() {
		Vector3i start = null;
		Vector3i end = null;

		start = this.settings.pointMin.clone().sub(this.settings.offset);
		end = this.settings.pointMax.clone().sub(this.settings.offset).add(1);
		drawCuboidLine(start, end, 0x3F, 0.0f, 0.75f, 0.0f, 0.25f);

		start = this.settings.pointA.clone().sub(this.settings.offset);
		end = start.clone().add(1);
		drawCuboidLine(start, end, 0x3F, 0.75f, 0.0f, 0.0f, 0.25f);
		drawCuboidQuad(start, end, 0x3F, 0.75f, 0.0f, 0.0f, 0.25f);

		start = this.settings.pointB.clone().sub(this.settings.offset);
		end = start.clone().add(1);
		drawCuboidLine(start, end, 0x3F, 0.0f, 0.0f, 0.75f, 0.25f);
		drawCuboidQuad(start, end, 0x3F, 0.0f, 0.0f, 0.75f, 0.25f);
	}

	private void drawCuboidQuad(Vector3i a, Vector3i b, int sides, float red, float green, float blue, float alpha) {
		Vector3f zero = new Vector3f(a.x, a.y, a.z).sub(this.settings.blockDelta);
		Vector3f size = new Vector3f(b.x, b.y, b.z).add(this.settings.blockDelta);

		if (this.objectCountQuad + 24 >= this.bufferSizeQuad) {
			this.needsExpansionQuad = true;
			return;
		}

		int total = 0;

		// left
		if ((sides & 0x10) != 0) {
			this.vBufferQuad.put(zero.x).put(zero.y).put(zero.z);
			this.vBufferQuad.put(zero.x).put(zero.y).put(size.z);
			this.vBufferQuad.put(zero.x).put(size.y).put(size.z);
			this.vBufferQuad.put(zero.x).put(size.y).put(zero.z);

			total += 4;
		}

		// right
		if ((sides & 0x20) != 0) {
			this.vBufferQuad.put(size.x).put(zero.y).put(size.z);
			this.vBufferQuad.put(size.x).put(zero.y).put(zero.z);
			this.vBufferQuad.put(size.x).put(size.y).put(zero.z);
			this.vBufferQuad.put(size.x).put(size.y).put(size.z);

			total += 4;
		}

		// near
		if ((sides & 0x04) != 0) {
			this.vBufferQuad.put(size.x).put(zero.y).put(zero.z);
			this.vBufferQuad.put(zero.x).put(zero.y).put(zero.z);
			this.vBufferQuad.put(zero.x).put(size.y).put(zero.z);
			this.vBufferQuad.put(size.x).put(size.y).put(zero.z);

			total += 4;
		}

		// far
		if ((sides & 0x08) != 0) {
			this.vBufferQuad.put(zero.x).put(zero.y).put(size.z);
			this.vBufferQuad.put(size.x).put(zero.y).put(size.z);
			this.vBufferQuad.put(size.x).put(size.y).put(size.z);
			this.vBufferQuad.put(zero.x).put(size.y).put(size.z);

			total += 4;
		}

		// bottom
		if ((sides & 0x01) != 0) {
			this.vBufferQuad.put(size.x).put(zero.y).put(zero.z);
			this.vBufferQuad.put(size.x).put(zero.y).put(size.z);
			this.vBufferQuad.put(zero.x).put(zero.y).put(size.z);
			this.vBufferQuad.put(zero.x).put(zero.y).put(zero.z);

			total += 4;
		}

		// top
		if ((sides & 0x02) != 0) {
			this.vBufferQuad.put(size.x).put(size.y).put(zero.z);
			this.vBufferQuad.put(zero.x).put(size.y).put(zero.z);
			this.vBufferQuad.put(zero.x).put(size.y).put(size.z);
			this.vBufferQuad.put(size.x).put(size.y).put(size.z);

			total += 4;
		}

		for (int i = 0; i < total; i++) {
			this.cBufferQuad.put(red).put(green).put(blue).put(alpha);
		}

		this.objectCountQuad += total;
	}

	private void drawCuboidLine(Vector3i a, Vector3i b, int sides, float red, float green, float blue, float alpha) {
		Vector3f zero = new Vector3f(a.x, a.y, a.z).sub(this.settings.blockDelta);
		Vector3f size = new Vector3f(b.x, b.y, b.z).add(this.settings.blockDelta);

		if (this.objectCountLine + 24 >= this.bufferSizeLine) {
			this.needsExpansionLine = true;
			return;
		}

		int total = 0;

		// bottom left
		if ((sides & 0x11) != 0) {
			this.vBufferLine.put(zero.x).put(zero.y).put(zero.z);
			this.vBufferLine.put(zero.x).put(zero.y).put(size.z);
			total += 2;
		}

		// top left
		if ((sides & 0x12) != 0) {
			this.vBufferLine.put(zero.x).put(size.y).put(zero.z);
			this.vBufferLine.put(zero.x).put(size.y).put(size.z);

			total += 2;
		}

		// bottom right
		if ((sides & 0x21) != 0) {
			this.vBufferLine.put(size.x).put(zero.y).put(zero.z);
			this.vBufferLine.put(size.x).put(zero.y).put(size.z);

			total += 2;
		}

		// top right
		if ((sides & 0x22) != 0) {
			this.vBufferLine.put(size.x).put(size.y).put(zero.z);
			this.vBufferLine.put(size.x).put(size.y).put(size.z);

			total += 2;
		}

		// bottom near
		if ((sides & 0x05) != 0) {
			this.vBufferLine.put(zero.x).put(zero.y).put(zero.z);
			this.vBufferLine.put(size.x).put(zero.y).put(zero.z);

			total += 2;
		}

		// top near
		if ((sides & 0x06) != 0) {
			this.vBufferLine.put(zero.x).put(size.y).put(zero.z);
			this.vBufferLine.put(size.x).put(size.y).put(zero.z);

			total += 2;
		}

		// bottom far
		if ((sides & 0x09) != 0) {
			this.vBufferLine.put(zero.x).put(zero.y).put(size.z);
			this.vBufferLine.put(size.x).put(zero.y).put(size.z);

			total += 2;
		}

		// top far
		if ((sides & 0x0A) != 0) {
			this.vBufferLine.put(zero.x).put(size.y).put(size.z);
			this.vBufferLine.put(size.x).put(size.y).put(size.z);

			total += 2;
		}

		// near left
		if ((sides & 0x14) != 0) {
			this.vBufferLine.put(zero.x).put(zero.y).put(zero.z);
			this.vBufferLine.put(zero.x).put(size.y).put(zero.z);

			total += 2;
		}

		// near right
		if ((sides & 0x24) != 0) {
			this.vBufferLine.put(size.x).put(zero.y).put(zero.z);
			this.vBufferLine.put(size.x).put(size.y).put(zero.z);

			total += 2;
		}

		// far left
		if ((sides & 0x18) != 0) {
			this.vBufferLine.put(zero.x).put(zero.y).put(size.z);
			this.vBufferLine.put(zero.x).put(size.y).put(size.z);

			total += 2;
		}

		// far right
		if ((sides & 0x28) != 0) {
			this.vBufferLine.put(size.x).put(zero.y).put(size.z);
			this.vBufferLine.put(size.x).put(size.y).put(size.z);

			total += 2;
		}

		for (int i = 0; i < total; i++) {
			this.cBufferLine.put(red).put(green).put(blue).put(alpha);
		}

		this.objectCountLine += total;
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
