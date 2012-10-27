package lunatrius.schematica;

import net.minecraft.src.Block;
import net.minecraft.src.BlockChest;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.ModelChest;
import net.minecraft.src.ModelLargeChest;
import net.minecraft.src.ModelSign;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.TileEntityEnderChest;
import net.minecraft.src.TileEntityRenderer;
import net.minecraft.src.TileEntitySign;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderTileEntity {
	private final Settings settings = Settings.instance();
	private final ModelChest chestModel = new ModelChest();
	private final ModelChest largeChestModel = new ModelLargeChest();
	private final ModelSign modelSign = new ModelSign();
	private final SchematicWorld world;

	public RenderTileEntity(SchematicWorld world) {
		this.world = world;
	}

	public void renderTileEntityChestAt(TileEntityChest par1TileEntityChest) {
		int var9 = 0;

		Block var10 = getBlockType(par1TileEntityChest);

		if (var10 != null) {
			unifyAdjacentChests((BlockChest) var10, this.world, par1TileEntityChest.xCoord, par1TileEntityChest.yCoord, par1TileEntityChest.zCoord);
			var9 = getBlockMetadata(par1TileEntityChest);
		}

		if (par1TileEntityChest.adjacentChestZNeg == null && par1TileEntityChest.adjacentChestXNeg == null) {
			ModelChest var14;

			if (par1TileEntityChest.adjacentChestXPos == null && par1TileEntityChest.adjacentChestZPosition == null) {
				var14 = this.chestModel;
				this.bindTextureByName("/item/chest.png");
			} else {
				var14 = this.largeChestModel;
				this.bindTextureByName("/item/largechest.png");
			}

			GL11.glPushMatrix();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glTranslatef(par1TileEntityChest.xCoord, par1TileEntityChest.yCoord + 1.0f, par1TileEntityChest.zCoord + 1.0f);
			GL11.glScalef(1.0f, -1.0f, -1.0f);
			GL11.glTranslatef(0.5f, 0.5f, 0.5f);
			short var11 = 0;

			switch (var9) {
			case 2:
				var11 = 180;
				break;
			case 3:
				var11 = 0;
				break;
			case 4:
				var11 = 90;
				break;
			case 5:
				var11 = -90;
				break;
			}

			if (var9 == 2 && par1TileEntityChest.adjacentChestXPos != null) {
				GL11.glTranslatef(1.0f, 0.0f, 0.0f);
			}

			if (var9 == 5 && par1TileEntityChest.adjacentChestZPosition != null) {
				GL11.glTranslatef(0.0f, 0.0f, -1.0f);
			}

			GL11.glRotatef(var11, 0.0f, 1.0f, 0.0f);
			GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
			float var12 = par1TileEntityChest.prevLidAngle;
			float var13;

			if (par1TileEntityChest.adjacentChestZNeg != null) {
				var13 = par1TileEntityChest.adjacentChestZNeg.prevLidAngle;

				if (var13 > var12) {
					var12 = var13;
				}
			}

			if (par1TileEntityChest.adjacentChestXNeg != null) {
				var13 = par1TileEntityChest.adjacentChestXNeg.prevLidAngle;

				if (var13 > var12) {
					var12 = var13;
				}
			}

			var12 = 1.0f - var12;
			var12 = 1.0f - var12 * var12 * var12;
			var14.chestLid.rotateAngleX = -(var12 * (float) Math.PI / 2.0f);
			var14.renderAll();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glPopMatrix();
		}
	}

	public void renderTileEntityEnderChestAt(TileEntityEnderChest par1TileEntityEnderChest) {
		int var9 = 0;

		var9 = getBlockMetadata(par1TileEntityEnderChest);

		this.bindTextureByName("/item/enderchest.png");
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glTranslatef(par1TileEntityEnderChest.xCoord, par1TileEntityEnderChest.yCoord + 1.0f, par1TileEntityEnderChest.zCoord + 1.0f);
		GL11.glScalef(1.0f, -1.0f, -1.0f);
		GL11.glTranslatef(0.5f, 0.5f, 0.5f);
		short var10 = 0;

		switch (var9) {
		case 2:
			var10 = 180;
			break;
		case 3:
			var10 = 0;
			break;
		case 4:
			var10 = 90;
			break;
		case 5:
			var10 = -90;
			break;
		}

		GL11.glRotatef(var10, 0.0f, 1.0f, 0.0f);
		GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
		float var11 = par1TileEntityEnderChest.prevLidAngle + (par1TileEntityEnderChest.lidAngle - par1TileEntityEnderChest.prevLidAngle);
		var11 = 1.0f - var11;
		var11 = 1.0f - var11 * var11 * var11;
		this.chestModel.chestLid.rotateAngleX = -(var11 * (float) Math.PI / 2.0f);
		this.chestModel.renderAll();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public void renderTileEntitySignAt(TileEntitySign par1TileEntitySign) {
		Block var9 = getBlockType(par1TileEntitySign);
		GL11.glPushMatrix();
		float var10 = 0.6666667f;
		float var12;

		if (var9 == Block.signPost) {
			GL11.glTranslatef(par1TileEntitySign.xCoord + 0.5f, par1TileEntitySign.yCoord + 0.75f * var10, par1TileEntitySign.zCoord + 0.5f);
			float var11 = getBlockMetadata(par1TileEntitySign) * 360 / 16.0f;
			GL11.glRotatef(-var11, 0.0f, 1.0f, 0.0f);
			this.modelSign.signStick.showModel = true;
		} else {
			int var16 = getBlockMetadata(par1TileEntitySign);
			var12 = 0.0f;

			if (var16 == 2) {
				var12 = 180.0f;
			}

			if (var16 == 4) {
				var12 = 90.0f;
			}

			if (var16 == 5) {
				var12 = -90.0f;
			}

			GL11.glTranslatef(par1TileEntitySign.xCoord + 0.5f, par1TileEntitySign.yCoord + 0.75f * var10, par1TileEntitySign.zCoord + 0.5f);
			GL11.glRotatef(-var12, 0.0f, 1.0f, 0.0f);
			GL11.glTranslatef(0.0f, -0.3125f, -0.4375f);
			this.modelSign.signStick.showModel = false;
		}

		this.bindTextureByName("/item/sign.png");
		GL11.glPushMatrix();
		GL11.glScalef(var10, -var10, -var10);
		this.modelSign.renderSign();
		GL11.glPopMatrix();

		FontRenderer var17 = this.settings.minecraft.fontRenderer;
		var12 = 0.016666668f * var10;
		GL11.glTranslatef(0.0f, 0.5f * var10, 0.07f * var10);
		GL11.glScalef(var12, -var12, var12);
		GL11.glNormal3f(0.0f, 0.0f, -1.0f * var12);
		GL11.glDepthMask(false);
		int var13 = (int) (this.settings.alpha * 255) * 0x1000000;

		for (int var14 = 0; var14 < par1TileEntitySign.signText.length; ++var14) {
			String var15 = par1TileEntitySign.signText[var14];
			var17.drawString(var15, -var17.getStringWidth(var15) / 2, var14 * 10 - par1TileEntitySign.signText.length * 5, var13);
		}

		GL11.glDepthMask(true);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, this.settings.alpha);
		GL11.glPopMatrix();
	}

	private Block getBlockType(TileEntity tileEntity) {
		return this.world.getBlock(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	}

	private int getBlockMetadata(TileEntity tileEntity) {
		return this.world.getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	}

	private void unifyAdjacentChests(BlockChest par1BlockChest, SchematicWorld par2World, int par3, int par4, int par5) {
		int var5 = par2World.getBlockId(par3, par4, par5 - 1);
		int var6 = par2World.getBlockId(par3, par4, par5 + 1);
		int var7 = par2World.getBlockId(par3 - 1, par4, par5);
		int var8 = par2World.getBlockId(par3 + 1, par4, par5);
		int var10;
		int var11;
		byte metadata;
		int var14;

		if (var5 != par1BlockChest.blockID && var6 != par1BlockChest.blockID) {
			if (var7 != par1BlockChest.blockID && var8 != par1BlockChest.blockID) {
				metadata = 3;

				if (Block.opaqueCubeLookup[var5] && !Block.opaqueCubeLookup[var6]) {
					metadata = 3;
				}

				if (Block.opaqueCubeLookup[var6] && !Block.opaqueCubeLookup[var5]) {
					metadata = 2;
				}

				if (Block.opaqueCubeLookup[var7] && !Block.opaqueCubeLookup[var8]) {
					metadata = 5;
				}

				if (Block.opaqueCubeLookup[var8] && !Block.opaqueCubeLookup[var7]) {
					metadata = 4;
				}
			} else {
				var10 = par2World.getBlockId(var7 == par1BlockChest.blockID ? par3 - 1 : par3 + 1, par4, par5 - 1);
				var11 = par2World.getBlockId(var7 == par1BlockChest.blockID ? par3 - 1 : par3 + 1, par4, par5 + 1);
				metadata = 3;
				if (var7 == par1BlockChest.blockID) {
					var14 = par2World.getBlockMetadata(par3 - 1, par4, par5);
				} else {
					var14 = par2World.getBlockMetadata(par3 + 1, par4, par5);
				}

				if (var14 == 2) {
					metadata = 2;
				}

				if ((Block.opaqueCubeLookup[var5] || Block.opaqueCubeLookup[var10]) && !Block.opaqueCubeLookup[var6] && !Block.opaqueCubeLookup[var11]) {
					metadata = 3;
				}

				if ((Block.opaqueCubeLookup[var6] || Block.opaqueCubeLookup[var11]) && !Block.opaqueCubeLookup[var5] && !Block.opaqueCubeLookup[var10]) {
					metadata = 2;
				}
			}
		} else {
			var10 = par2World.getBlockId(par3 - 1, par4, var5 == par1BlockChest.blockID ? par5 - 1 : par5 + 1);
			var11 = par2World.getBlockId(par3 + 1, par4, var5 == par1BlockChest.blockID ? par5 - 1 : par5 + 1);
			metadata = 5;
			if (var5 == par1BlockChest.blockID) {
				var14 = par2World.getBlockMetadata(par3, par4, par5 - 1);
			} else {
				var14 = par2World.getBlockMetadata(par3, par4, par5 + 1);
			}

			if (var14 == 4) {
				metadata = 4;
			}

			if ((Block.opaqueCubeLookup[var7] || Block.opaqueCubeLookup[var10]) && !Block.opaqueCubeLookup[var8] && !Block.opaqueCubeLookup[var11]) {
				metadata = 5;
			}

			if ((Block.opaqueCubeLookup[var8] || Block.opaqueCubeLookup[var11]) && !Block.opaqueCubeLookup[var7] && !Block.opaqueCubeLookup[var10]) {
				metadata = 4;
			}

			par2World.setBlockMetadata(par3, par4, par5, metadata);
		}
	}

	private void bindTextureByName(String string) {
		RenderEngine var2 = TileEntityRenderer.instance.renderEngine;

		if (var2 != null) {
			var2.bindTexture(var2.getTexture(string));
		}
	}
}
