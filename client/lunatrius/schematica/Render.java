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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import lunatrius.schematica.util.Vector3f;
import lunatrius.schematica.util.Vector3i;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.Frustrum;
import net.minecraft.src.GLAllocation;
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

import org.lwjgl.opengl.GL11;

public class Render {
	private final Settings settings = Settings.instance();
	private final AxisAlignedBB axisAlignedBB = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
	private final Map<String, Integer> glLists = new HashMap<String, Integer>();
	private final List<String> textures = new ArrayList<String>();
	private final BufferedImage missingTextureImage = new BufferedImage(64, 64, 2);
	private Field fieldTextureMap = null;
	private Field fieldSingleIntBuffer = null;

	public Render() {
		initTexture();
		initReflection();
	}

	public void initTexture() {
		Graphics graphics = this.missingTextureImage.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 64, 64);
		graphics.setColor(Color.BLACK);
		graphics.drawString("missingtex", 1, 10);
		graphics.dispose();
	}

	public void initReflection() {
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

		if (this.settings.isRenderingSchematic && this.settings.schematic != null) {
			GL11.glTranslatef(-this.settings.getTranslationX(), -this.settings.getTranslationY(), -this.settings.getTranslationZ());
			renderSchematic();
			GL11.glTranslatef(this.settings.getTranslationX(), this.settings.getTranslationY(), this.settings.getTranslationZ());
		}

		if (this.settings.isRenderingGuide) {
			GL11.glTranslatef(-this.settings.playerPosition.x, -this.settings.playerPosition.y, -this.settings.playerPosition.z);
			renderGuide();
			GL11.glTranslatef(this.settings.playerPosition.x, this.settings.playerPosition.y, this.settings.playerPosition.z);
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glPopMatrix();
	}

	private void renderSchematic() {
		int posX = (int) this.settings.getTranslationX();
		int posY = (int) this.settings.getTranslationY() - 1;
		int posZ = (int) this.settings.getTranslationZ();

		int minX = 0;
		int maxX = 0;
		int minY = 0;
		int maxY = 0;
		int minZ = 0;
		int maxZ = 0;

		if (this.settings.renderingLayer < 0) {
			this.settings.renderBlocks.renderAllFaces = false;
			minX = Math.max(posX - this.settings.renderRange.x, 0);
			maxX = Math.min(posX + this.settings.renderRange.x, this.settings.schematic.width());
			minY = Math.max(posY - this.settings.renderRange.y, 0);
			maxY = Math.min(posY + this.settings.renderRange.y, this.settings.schematic.height());
			minZ = Math.max(posZ - this.settings.renderRange.z, 0);
			maxZ = Math.min(posZ + this.settings.renderRange.z, this.settings.schematic.length());
		} else {
			this.settings.renderBlocks.renderAllFaces = true;
			minX = Math.max(posX - this.settings.renderRange.x * this.settings.renderRange.y, 0);
			maxX = Math.min(posX + this.settings.renderRange.x * this.settings.renderRange.y, this.settings.schematic.width());
			minY = this.settings.renderingLayer;
			maxY = this.settings.renderingLayer + 1;
			minZ = Math.max(posZ - this.settings.renderRange.z * this.settings.renderRange.y, 0);
			maxZ = Math.min(posZ + this.settings.renderRange.z * this.settings.renderRange.y, this.settings.schematic.length());
		}

		renderSchematic(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private void renderSchematic(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		Tessellator.instance.startDrawingQuads();

		SchematicWorld world = this.settings.schematic;
		int[][][] worldMatrix = this.settings.schematicMatrix;
		RenderBlocks renderBlocks = this.settings.renderBlocks;
		RenderTileEntity renderTileEntity = this.settings.renderTileEntity;
		List<Vector3i> invalidBlockId = new ArrayList<Vector3i>();
		List<Vector3i> invalidBlockMetadata = new ArrayList<Vector3i>();
		List<Vector3i> todoBlocks = new ArrayList<Vector3i>();

		renderBlocks.aoType = 0;

		Frustrum frustrum = new Frustrum();
		frustrum.setPosition(this.settings.playerPosition.x - this.settings.offset.x, this.settings.playerPosition.y - this.settings.offset.y, this.settings.playerPosition.z - this.settings.offset.z);
		frustrum.setPosition(0, 0, 0);

		int x, y, z;
		int blockId = 0;
		Block block = null;
		String lastTexture = "";

		boolean ambientOcclusion = this.settings.minecraft.gameSettings.ambientOcclusion;
		this.settings.minecraft.gameSettings.ambientOcclusion = false;
		for (x = minX; x < maxX; x++) {
			for (y = minY; y < maxY; y++) {
				for (z = minZ; z < maxZ; z++) {
					if (worldMatrix[x][y][z] == 0x00) {
						continue;
					}

					try {
						if (!frustrum.isBoundingBoxInFrustum(this.axisAlignedBB.setBounds(x, y, z, x + 1, y + 1, z + 1))) {
							continue;
						}

						switch (worldMatrix[x][y][z]) {
						case 0x01:
							if (this.settings.highlight) {
								todoBlocks.add(new Vector3i(x, y, z));
							}

							blockId = world.getBlockId(x, y, z);

							if ((block = Block.blocksList[blockId]) != null) {
								if (lastTexture != block.getTextureFile()) {
									ForgeHooksClient.bindTexture(getTextureName(block.getTextureFile()), 0);
									lastTexture = block.getTextureFile();
								}

								if (this.settings.renderingLayer >= 0 && (blockId == Block.redstoneRepeaterActive.blockID || blockId == Block.redstoneRepeaterIdle.blockID)) {
									renderBlocks.renderAllFaces = false;
								}

								renderBlocks.renderBlockByRenderType(block, x, y, z);

								if (this.settings.renderingLayer >= 0 && (blockId == Block.redstoneRepeaterActive.blockID || blockId == Block.redstoneRepeaterIdle.blockID)) {
									renderBlocks.renderAllFaces = true;
								}
							}
							break;
						case 0x02:
							if (this.settings.highlight) {
								invalidBlockId.add(new Vector3i(x, y, z));
							}
							break;
						case 0x04:
							if (this.settings.highlight) {
								invalidBlockMetadata.add(new Vector3i(x, y, z));
							}
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		this.settings.minecraft.gameSettings.ambientOcclusion = ambientOcclusion;

		Tessellator.instance.draw();

		GL11.glColor4f(1.0f, 1.0f, 1.0f, this.settings.alpha);

		try {
			for (TileEntity tileEntity : world.getTileEntities()) {
				x = tileEntity.xCoord;
				y = tileEntity.yCoord;
				z = tileEntity.zCoord;

				if (worldMatrix[x][y][z] == 0x00) {
					continue;
				}

				if (!frustrum.isBoundingBoxInFrustum(this.axisAlignedBB.setBounds(x, y, z, x + 1, y + 1, z + 1))) {
					continue;
				}

				if (x < minX || x >= maxX) {
					continue;
				} else if (z < minZ || z >= maxZ) {
					continue;
				} else if (y < minY || y >= maxY) {
					continue;
				}

				if (worldMatrix[x][y][z] == 0x01) {
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

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);

		GL11.glLineWidth(1.5f);
		GL11.glColor4f(0.75f, 0.0f, 0.75f, 0.25f);
		drawCuboid(Vector3i.ZERO, new Vector3i(world.width(), world.height(), world.length()), 0x01);

		GL11.glColor4f(1.0f, 0.0f, 0.0f, 0.25f);
		for (Vector3i invalidBlock : invalidBlockId) {
			drawCuboid(invalidBlock, invalidBlock.clone().add(1), 0x03);
		}

		GL11.glColor4f(0.75f, 0.35f, 0.0f, 0.45f);
		for (Vector3i invalidBlock : invalidBlockMetadata) {
			drawCuboid(invalidBlock, invalidBlock.clone().add(1), 0x03);
		}

		GL11.glColor4f(0.0f, 0.75f, 1.0f, 0.25f);
		for (Vector3i todoBlock : todoBlocks) {
			drawCuboid(todoBlock, todoBlock.clone().add(1), 0x03);
		}

		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void renderGuide() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);

		GL11.glLineWidth(1.5f);

		GL11.glColor4f(0.0f, 0.75f, 0.0f, 0.25f);
		drawCuboid(this.settings.pointMin, this.settings.pointMax.clone().add(1), 0x01);

		GL11.glColor4f(0.75f, 0.0f, 0.0f, 0.25f);
		drawCuboid(this.settings.pointA, this.settings.pointA.clone().add(1), 0x03);

		GL11.glColor4f(0.0f, 0.0f, 0.75f, 0.25f);
		drawCuboid(this.settings.pointB, this.settings.pointB.clone().add(1), 0x03);

		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void drawCuboid(Vector3i a, Vector3i b, int type) {
		Vector3f zero = new Vector3f().sub(this.settings.blockDelta);
		Vector3f size = new Vector3f(b.x, b.y, b.z).sub(a.x, a.y, a.z).add(this.settings.blockDelta);
		String key = size.x + "/" + size.y + "/" + size.z + "/" + type;

		if (!this.glLists.containsKey(key)) {
			this.glLists.put(key, compileList(zero, size, type));
		}

		GL11.glTranslatef(a.x, a.y, a.z);
		GL11.glCallList(this.glLists.get(key));
		GL11.glTranslatef(-a.x, -a.y, -a.z);
	}

	private int compileList(Vector3f zero, Vector3f size, int type) {
		int list = GL11.glGenLists(1);

		GL11.glNewList(list, GL11.GL_COMPILE);

		if ((type & 0x01) != 0) {
			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex3f(zero.x, zero.y, zero.z);
			GL11.glVertex3f(zero.x, zero.y, size.z);
			GL11.glVertex3f(zero.x, size.y, size.z);
			GL11.glVertex3f(zero.x, size.y, zero.z);
			GL11.glEnd();

			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex3f(size.x, zero.y, size.z);
			GL11.glVertex3f(size.x, zero.y, zero.z);
			GL11.glVertex3f(size.x, size.y, zero.z);
			GL11.glVertex3f(size.x, size.y, size.z);
			GL11.glEnd();

			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex3f(size.x, zero.y, zero.z);
			GL11.glVertex3f(zero.x, zero.y, zero.z);
			GL11.glVertex3f(zero.x, size.y, zero.z);
			GL11.glVertex3f(size.x, size.y, zero.z);
			GL11.glEnd();

			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex3f(zero.x, zero.y, size.z);
			GL11.glVertex3f(size.x, zero.y, size.z);
			GL11.glVertex3f(size.x, size.y, size.z);
			GL11.glVertex3f(zero.x, size.y, size.z);
			GL11.glEnd();

			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex3f(size.x, size.y, zero.z);
			GL11.glVertex3f(zero.x, size.y, zero.z);
			GL11.glVertex3f(zero.x, size.y, size.z);
			GL11.glVertex3f(size.x, size.y, size.z);
			GL11.glEnd();

			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex3f(size.x, zero.y, zero.z);
			GL11.glVertex3f(size.x, zero.y, size.z);
			GL11.glVertex3f(zero.x, zero.y, size.z);
			GL11.glVertex3f(zero.x, zero.y, zero.z);
			GL11.glEnd();
		}

		if ((type & 0x02) != 0) {
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3f(zero.x, zero.y, zero.z);
			GL11.glVertex3f(zero.x, zero.y, size.z);
			GL11.glVertex3f(zero.x, size.y, size.z);
			GL11.glVertex3f(zero.x, size.y, zero.z);
			GL11.glVertex3f(size.x, zero.y, size.z);
			GL11.glVertex3f(size.x, zero.y, zero.z);
			GL11.glVertex3f(size.x, size.y, zero.z);
			GL11.glVertex3f(size.x, size.y, size.z);
			GL11.glVertex3f(size.x, zero.y, zero.z);
			GL11.glVertex3f(zero.x, zero.y, zero.z);
			GL11.glVertex3f(zero.x, size.y, zero.z);
			GL11.glVertex3f(size.x, size.y, zero.z);
			GL11.glVertex3f(zero.x, zero.y, size.z);
			GL11.glVertex3f(size.x, zero.y, size.z);
			GL11.glVertex3f(size.x, size.y, size.z);
			GL11.glVertex3f(zero.x, size.y, size.z);
			GL11.glVertex3f(size.x, size.y, zero.z);
			GL11.glVertex3f(zero.x, size.y, zero.z);
			GL11.glVertex3f(zero.x, size.y, size.z);
			GL11.glVertex3f(size.x, size.y, size.z);
			GL11.glVertex3f(size.x, zero.y, zero.z);
			GL11.glVertex3f(size.x, zero.y, size.z);
			GL11.glVertex3f(zero.x, zero.y, size.z);
			GL11.glVertex3f(zero.x, zero.y, zero.z);
			GL11.glEnd();
		}

		GL11.glEndList();

		return list;
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
