package com.github.lunatrius.schematica;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraftforge.common.ForgeDirection;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;

public class SchematicWorld extends World {
	private static final AnvilSaveHandler anvilSaveHandler = new AnvilSaveHandler(Minecraft.getMinecraft().mcDataDir, "mods/saves-schematica-dummy", false);
	private static final WorldSettings worldSettings = new WorldSettings(0, EnumGameType.CREATIVE, false, false, WorldType.FLAT);
	private static final Comparator<ItemStack> blockListComparator = new Comparator<ItemStack>() {
		@Override
		public int compare(ItemStack itemStackA, ItemStack itemStackB) {
			return (itemStackA.itemID * 16 + itemStackA.getItemDamage()) - (itemStackB.itemID * 16 + itemStackB.getItemDamage());
		}
	};

	protected static final List<Integer> blockListIgnoreID = new ArrayList<Integer>();
	protected static final List<Integer> blockListIgnoreMetadata = new ArrayList<Integer>();
	protected static final Map<Integer, Integer> blockListMapping = new HashMap<Integer, Integer>();

	private final Settings settings = Settings.instance();
	private ItemStack icon;
	private int[][][] blocks;
	private int[][][] metadata;
	private final List<TileEntity> tileEntities = new ArrayList<TileEntity>();
	private final List<ItemStack> blockList = new ArrayList<ItemStack>();
	private short width;
	private short length;
	private short height;

	public SchematicWorld() {
		super(anvilSaveHandler, "", null, worldSettings, null, null);
		this.icon = Settings.defaultIcon.copy();
		this.blocks = null;
		this.metadata = null;
		this.tileEntities.clear();
		this.width = 0;
		this.length = 0;
		this.height = 0;
	}

	public SchematicWorld(String icon, int[][][] blocks, int[][][] metadata, List<TileEntity> tileEntities, short width, short height, short length) {
		this();
		try {
			String[] parts = icon.split(":");
			if (parts.length == 1) {
				this.icon = new ItemStack(Integer.parseInt(parts[0]), 1, 0);
			} else if (parts.length == 2) {
				this.icon = new ItemStack(Integer.parseInt(parts[0]), 1, Integer.parseInt(parts[1]));
			}
		} catch (Exception e) {
			Settings.logger.logSevereException("Failed to assign an icon!", e);
			this.icon = Settings.defaultIcon.copy();
		}
		this.blocks = blocks.clone();
		this.metadata = metadata.clone();
		if (tileEntities != null) {
			this.tileEntities.addAll(tileEntities);
			for (TileEntity tileEntity : this.tileEntities) {
				tileEntity.worldObj = this;
				tileEntity.validate();
			}
		}
		this.width = width;
		this.length = length;
		this.height = height;

		generateBlockList();
	}

	@SuppressWarnings("null")
	public void readFromNBT(NBTTagCompound tagCompound) {
		if (tagCompound.hasKey("Icon")) {
			this.icon.readFromNBT(tagCompound.getCompoundTag("Icon"));
		} else {
			this.icon = Settings.defaultIcon.copy();
		}

		byte localBlocks[] = tagCompound.getByteArray("Blocks");
		byte localMetadata[] = tagCompound.getByteArray("Data");

		boolean extra = tagCompound.hasKey("Add") || tagCompound.hasKey("AddBlocks");
		byte extraBlocks[] = null;
		byte extraBlocksNibble[] = null;
		if (tagCompound.hasKey("AddBlocks")) {
			extraBlocksNibble = tagCompound.getByteArray("AddBlocks");
			extraBlocks = new byte[extraBlocksNibble.length * 2];
			for (int i = 0; i < extraBlocksNibble.length; i++) {
				extraBlocks[i * 2 + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
				extraBlocks[i * 2 + 1] = (byte) (extraBlocksNibble[i] & 0xF);
			}
		} else if (tagCompound.hasKey("Add")) {
			extraBlocks = tagCompound.getByteArray("Add");
		}

		this.width = tagCompound.getShort("Width");
		this.length = tagCompound.getShort("Length");
		this.height = tagCompound.getShort("Height");

		this.blocks = new int[this.width][this.height][this.length];
		this.metadata = new int[this.width][this.height][this.length];

		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < this.length; z++) {
					this.blocks[x][y][z] = (localBlocks[x + (y * this.length + z) * this.width]) & 0xFF;
					this.metadata[x][y][z] = (localMetadata[x + (y * this.length + z) * this.width]) & 0xFF;
					if (extra) {
						this.blocks[x][y][z] |= ((extraBlocks[x + (y * this.length + z) * this.width]) & 0xFF) << 8;
					}
				}
			}
		}

		this.tileEntities.clear();

		NBTTagList tileEntitiesList = tagCompound.getTagList("TileEntities");

		for (int i = 0; i < tileEntitiesList.tagCount(); i++) {
			TileEntity tileEntity = TileEntity.createAndLoadEntity((NBTTagCompound) tileEntitiesList.tagAt(i));
			if (tileEntity != null) {
				tileEntity.worldObj = this;
				tileEntity.validate();
				this.tileEntities.add(tileEntity);
			}
		}

		refreshChests();

		generateBlockList();
	}

	public void writeToNBT(NBTTagCompound tagCompound) {
		NBTTagCompound tagCompoundIcon = new NBTTagCompound();
		this.icon.writeToNBT(tagCompoundIcon);
		tagCompound.setCompoundTag("Icon", tagCompoundIcon);

		tagCompound.setShort("Width", this.width);
		tagCompound.setShort("Length", this.length);
		tagCompound.setShort("Height", this.height);

		int size = this.width * this.length * this.height;
		byte localBlocks[] = new byte[size];
		byte localMetadata[] = new byte[size];
		byte extraBlocks[] = new byte[size];
		byte extraBlocksNibble[] = new byte[(int) Math.ceil(size / 2.0)];
		boolean extra = false;

		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < this.length; z++) {
					localBlocks[x + (y * this.length + z) * this.width] = (byte) this.blocks[x][y][z];
					localMetadata[x + (y * this.length + z) * this.width] = (byte) this.metadata[x][y][z];
					if ((extraBlocks[x + (y * this.length + z) * this.width] = (byte) (this.blocks[x][y][z] >> 8)) > 0) {
						extra = true;
					}
				}
			}
		}

		for (int i = 0; i < extraBlocksNibble.length; i++) {
			if (i * 2 + 1 < extraBlocks.length) {
				extraBlocksNibble[i] = (byte) ((extraBlocks[i * 2 + 0] << 4) | extraBlocks[i * 2 + 1]);
			} else {
				extraBlocksNibble[i] = (byte) (extraBlocks[i * 2 + 0] << 4);
			}
		}

		int count = 20;
		NBTTagList tileEntitiesList = new NBTTagList();
		for (TileEntity tileEntity : this.tileEntities) {
			NBTTagCompound tileEntityTagCompound = new NBTTagCompound();
			try {
				tileEntity.writeToNBT(tileEntityTagCompound);
				tileEntitiesList.appendTag(tileEntityTagCompound);
			} catch (Exception e) {
				int pos = tileEntity.xCoord + (tileEntity.yCoord * this.length + tileEntity.zCoord) * this.width;
				if (--count > 0) {
					Block block = Block.blocksList[localBlocks[pos]];
					Settings.logger.logSevereException(String.format("Block %s[%d] with TileEntity %s failed to save! Replacing with bedrock...", block, block != null ? block.blockID : -1, tileEntity.getClass().getName()), e);
				}
				localBlocks[pos] = (byte) Block.bedrock.blockID;
				localMetadata[pos] = 0;
				extraBlocks[pos] = 0;
			}
		}

		tagCompound.setString("Materials", "Alpha");
		tagCompound.setByteArray("Blocks", localBlocks);
		tagCompound.setByteArray("Data", localMetadata);
		if (extra) {
			tagCompound.setByteArray("AddBlocks", extraBlocksNibble);
		}
		tagCompound.setTag("Entities", new NBTTagList());
		tagCompound.setTag("TileEntities", tileEntitiesList);
	}

	private void generateBlockList() {
		this.blockList.clear();

		int x, y, z, itemID, itemDamage;
		ItemStack itemStack = null;

		for (x = 0; x < this.width; x++) {
			for (y = 0; y < this.height; y++) {
				for (z = 0; z < this.length; z++) {
					itemID = this.blocks[x][y][z];
					itemDamage = this.metadata[x][y][z];

					if (itemID == 0 || blockListIgnoreID.contains(itemID)) {
						continue;
					}

					if (blockListIgnoreMetadata.contains(itemID)) {
						itemDamage = 0;
					}

					if (blockListMapping.containsKey(itemID)) {
						itemID = blockListMapping.get(itemID);
					}

					if (itemID == Block.wood.blockID || itemID == Block.leaves.blockID) {
						itemDamage &= 0x03;
					}

					if (itemID == Block.stoneSingleSlab.blockID || itemID == Block.woodSingleSlab.blockID) {
						itemDamage &= 0x07;
					}

					if (itemID >= 256) {
						itemDamage = 0;
					}

					if (itemID - 256 == Block.cocoaPlant.blockID) {
						itemDamage = 0x03;
					}

					if (itemID == Item.skull.itemID) {
						itemDamage = this.metadata[x][y][z];
					}

					itemStack = null;
					for (ItemStack block : this.blockList) {
						if (block.itemID == itemID && block.getItemDamage() == itemDamage) {
							itemStack = block;
							itemStack.stackSize++;
							break;
						}
					}

					if (itemStack == null) {
						this.blockList.add(new ItemStack(itemID, 1, itemDamage));
					}
				}
			}
		}
		Collections.sort(this.blockList, blockListComparator);
	}

	@Override
	public int getBlockId(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length) {
			return 0;
		}
		return (this.blocks[x][y][z]) & 0xFFF;
	}

	@Override
	public TileEntity getBlockTileEntity(int x, int y, int z) {
		for (int i = 0; i < this.tileEntities.size(); i++) {
			TileEntity tileEntity = this.tileEntities.get(i);
			if (tileEntity.xCoord == x && tileEntity.yCoord == y && tileEntity.zCoord == z) {
				return tileEntity;
			}
		}
		return null;
	}

	@Override
	public int getSkyBlockTypeBrightness(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4) {
		return 15;
	}

	@Override
	public float getBrightness(int var1, int var2, int var3, int var4) {
		return 1.0f;
	}

	@Override
	public float getLightBrightness(int x, int y, int z) {
		return 1.0f;
	}

	@Override
	public int getBlockMetadata(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length) {
			return 0;
		}
		return this.metadata[x][y][z];
	}

	@Override
	public Material getBlockMaterial(int x, int y, int z) {
		return getBlock(x, y, z) != null ? getBlock(x, y, z).blockMaterial : Material.air;
	}

	@Override
	public boolean isBlockOpaqueCube(int x, int y, int z) {
		if (this.settings.renderingLayer != -1 && this.settings.renderingLayer != y) {
			return false;
		}
		return getBlock(x, y, z) != null && getBlock(x, y, z).isOpaqueCube();
	}

	@Override
	public boolean isBlockNormalCube(int x, int y, int z) {
		return getBlockMaterial(x, y, z).isOpaque() && getBlock(x, y, z) != null && getBlock(x, y, z).renderAsNormalBlock();
	}

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length) {
			return true;
		}
		return this.blocks[x][y][z] == 0;
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(int var1, int var2) {
		return BiomeGenBase.forest;
	}

	@Override
	public int getHeight() {
		return this.height + 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean extendedLevelsInChunkCache() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean doesBlockHaveSolidTopSurface(int var1, int var2, int var3) {
		return false;
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return null;
	}

	@Override
	public Entity getEntityByID(int var1) {
		return null;
	}

	@Override
	public boolean blockExists(int x, int y, int z) {
		return false;
	}

	@Override
	public boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag) {
		this.metadata[x][y][z] = metadata;
		return true;
	}

	@Override
	public boolean isBlockSolidOnSide(int x, int y, int z, ForgeDirection side, boolean _default) {
		Block block = getBlock(x, y, z);
		if (block == null) {
			return false;
		}
		return block.isBlockSolidOnSide(this, x, y, z, side);
	}

	public Block getBlock(int x, int y, int z) {
		return Block.blocksList[getBlockId(x, y, z)];
	}

	public void setTileEntities(List<TileEntity> tileEntities) {
		this.tileEntities.clear();
		this.tileEntities.addAll(tileEntities);
		for (TileEntity tileEntity : this.tileEntities) {
			tileEntity.worldObj = this;
			tileEntity.validate();
		}
	}

	public List<TileEntity> getTileEntities() {
		return this.tileEntities;
	}

	public List<ItemStack> getBlockList() {
		return this.blockList;
	}

	public void refreshChests() {
		TileEntity tileEntity;
		for (int i = 0; i < this.tileEntities.size(); i++) {
			tileEntity = this.tileEntities.get(i);

			if (tileEntity instanceof TileEntityChest) {
				checkForAdjacentChests((TileEntityChest) tileEntity);
			}
		}
	}

	private void checkForAdjacentChests(TileEntityChest tileEntityChest) {
		tileEntityChest.adjacentChestChecked = true;
		tileEntityChest.adjacentChestZNeg = null;
		tileEntityChest.adjacentChestXPos = null;
		tileEntityChest.adjacentChestXNeg = null;
		tileEntityChest.adjacentChestZPosition = null;

		if (getBlockId(tileEntityChest.xCoord - 1, tileEntityChest.yCoord, tileEntityChest.zCoord) == Block.chest.blockID) {
			tileEntityChest.adjacentChestXNeg = (TileEntityChest) getBlockTileEntity(tileEntityChest.xCoord - 1, tileEntityChest.yCoord, tileEntityChest.zCoord);
		}

		if (getBlockId(tileEntityChest.xCoord + 1, tileEntityChest.yCoord, tileEntityChest.zCoord) == Block.chest.blockID) {
			tileEntityChest.adjacentChestXPos = (TileEntityChest) getBlockTileEntity(tileEntityChest.xCoord + 1, tileEntityChest.yCoord, tileEntityChest.zCoord);
		}

		if (getBlockId(tileEntityChest.xCoord, tileEntityChest.yCoord, tileEntityChest.zCoord - 1) == Block.chest.blockID) {
			tileEntityChest.adjacentChestZNeg = (TileEntityChest) getBlockTileEntity(tileEntityChest.xCoord, tileEntityChest.yCoord, tileEntityChest.zCoord - 1);
		}

		if (getBlockId(tileEntityChest.xCoord, tileEntityChest.yCoord, tileEntityChest.zCoord + 1) == Block.chest.blockID) {
			tileEntityChest.adjacentChestZPosition = (TileEntityChest) getBlockTileEntity(tileEntityChest.xCoord, tileEntityChest.yCoord, tileEntityChest.zCoord + 1);
		}
	}

	public void flip() {
		int tmp;
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < (this.length + 1) / 2; z++) {
					tmp = this.blocks[x][y][z];
					this.blocks[x][y][z] = this.blocks[x][y][this.length - 1 - z];
					this.blocks[x][y][this.length - 1 - z] = tmp;

					if (z == this.length - 1 - z) {
						this.metadata[x][y][z] = BlockInfo.getTransformedMetadataFlip(this.blocks[x][y][z], this.metadata[x][y][z]);
					} else {
						tmp = this.metadata[x][y][z];
						this.metadata[x][y][z] = BlockInfo.getTransformedMetadataFlip(this.blocks[x][y][z], this.metadata[x][y][this.length - 1 - z]);
						this.metadata[x][y][this.length - 1 - z] = BlockInfo.getTransformedMetadataFlip(this.blocks[x][y][this.length - 1 - z], tmp);
					}
				}
			}
		}

		TileEntity tileEntity;
		for (int i = 0; i < this.tileEntities.size(); i++) {
			tileEntity = this.tileEntities.get(i);
			tileEntity.zCoord = this.length - 1 - tileEntity.zCoord;
			tileEntity.blockMetadata = this.metadata[tileEntity.xCoord][tileEntity.yCoord][tileEntity.zCoord];

			if (tileEntity instanceof TileEntitySkull && tileEntity.blockMetadata == 0x1) {
				TileEntitySkull skullTileEntity = (TileEntitySkull) tileEntity;
				int angle = skullTileEntity.func_82119_b();
				int base = 0;
				if (angle <= 7) {
					base = 4;
				} else {
					base = 12;
				}

				skullTileEntity.setSkullRotation((2 * base - angle) & 15);
			}
		}

		refreshChests();
	}

	public void rotate() {
		int[][][] localBlocks = new int[this.length][this.height][this.width];
		int[][][] localMetadata = new int[this.length][this.height][this.width];

		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < this.length; z++) {
					localBlocks[z][y][x] = this.blocks[this.width - 1 - x][y][z];
					localMetadata[z][y][x] = BlockInfo.getTransformedMetadataRotation(this.blocks[this.width - 1 - x][y][z], this.metadata[this.width - 1 - x][y][z]);
				}
			}
		}

		this.blocks = localBlocks;
		this.metadata = localMetadata;

		TileEntity tileEntity;
		int coord;
		for (int i = 0; i < this.tileEntities.size(); i++) {
			tileEntity = this.tileEntities.get(i);
			coord = tileEntity.xCoord;
			tileEntity.xCoord = tileEntity.zCoord;
			tileEntity.zCoord = this.width - 1 - coord;
			tileEntity.blockMetadata = this.metadata[tileEntity.xCoord][tileEntity.yCoord][tileEntity.zCoord];

			if (tileEntity instanceof TileEntitySkull && tileEntity.blockMetadata == 0x1) {
				TileEntitySkull skullTileEntity = (TileEntitySkull) tileEntity;
				skullTileEntity.setSkullRotation((skullTileEntity.func_82119_b() + 12) & 15);
			}

		}

		short tmp = this.width;
		this.width = this.length;
		this.length = tmp;

		refreshChests();
	}

	public int width() {
		return this.width;
	}

	public int length() {
		return this.length;
	}

	public int height() {
		return this.height;
	}

	public Vector3f dimensions() {
		return new Vector3f(this.width, this.height, this.length);
	}

	public static boolean isBlock(int itemId) {
		return itemId == Block.planks.blockID || itemId == Block.sandStone.blockID || itemId == Block.cloth.blockID || itemId == Block.stoneBrick.blockID || itemId == Block.stainedClay.blockID;
	}

	public static boolean isStair(int itemId) {
		return itemId == Block.stairsWoodOak.blockID || itemId == Block.stairsCobblestone.blockID || itemId == Block.stairsBrick.blockID || itemId == Block.stairsStoneBrick.blockID || itemId == Block.stairsNetherBrick.blockID || itemId == Block.stairsSandStone.blockID || itemId == Block.stairsWoodSpruce.blockID || itemId == Block.stairsWoodBirch.blockID || itemId == Block.stairsWoodJungle.blockID || itemId == Block.stairsNetherQuartz.blockID;
	}

	public static boolean isSlab(int itemId) {
		return itemId == Block.stoneSingleSlab.blockID || itemId == Block.woodSingleSlab.blockID;
	}

	public static boolean isDoubleSlab(int itemId) {
		return itemId == Block.stoneDoubleSlab.blockID || itemId == Block.woodDoubleSlab.blockID;
	}

	public static boolean isPistonBase(int itemId) {
		return itemId == Block.pistonStickyBase.blockID || itemId == Block.pistonBase.blockID;
	}

	public static boolean isRedstoneRepeater(int itemId) {
		return itemId == Block.redstoneRepeaterActive.blockID || itemId == Block.redstoneRepeaterIdle.blockID;
	}

	public static boolean isTorch(int itemId) {
		return itemId == Block.torchRedstoneActive.blockID || itemId == Block.torchRedstoneIdle.blockID || itemId == Block.torchWood.blockID;
	}

	public static boolean isContainer(int itemId) {
		return itemId == Block.furnaceBurning.blockID || itemId == Block.furnaceIdle.blockID || itemId == Block.dispenser.blockID || itemId == Block.chest.blockID || itemId == Block.enderChest.blockID;
	}

	public static boolean isButton(int itemId) {
		return itemId == Block.stoneButton.blockID || itemId == Block.woodenButton.blockID;
	}

	public static boolean isPumpkin(int itemId) {
		return itemId == Block.pumpkin.blockID || itemId == Block.pumpkinLantern.blockID;
	}

	public static boolean isFluidContainer(int itemId) {
		return itemId == Item.bucketWater.itemID || itemId == Item.bucketLava.itemID;
	}

	public static boolean isMetadataSensitive(int itemId) {
		return itemId == Block.anvil.blockID || itemId == Block.trapdoor.blockID || isTorch(itemId) || isBlock(itemId) || isSlab(itemId) || isDoubleSlab(itemId) || isPistonBase(itemId) || isRedstoneRepeater(itemId) || isContainer(itemId) || isButton(itemId) || isPumpkin(itemId);
	}
}
