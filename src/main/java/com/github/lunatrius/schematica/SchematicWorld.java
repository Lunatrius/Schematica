package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.lib.Reference;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.SaveHandlerMP;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;

public class SchematicWorld extends World {
	private static final AnvilSaveHandler anvilSaveHandler = new AnvilSaveHandler(Minecraft.getMinecraft().mcDataDir, "tmp/schematica", false);
	private static final WorldSettings worldSettings = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);
	private static final Comparator<ItemStack> blockListComparator = new Comparator<ItemStack>() {
		@Override
		public int compare(ItemStack itemStackA, ItemStack itemStackB) {
			return itemStackA.getUnlocalizedName().compareTo(itemStackB.getUnlocalizedName());
			// return (itemStackA.itemID * 16 + itemStackA.getItemDamage()) - (itemStackB.itemID * 16 + itemStackB.getItemDamage());
		}
	};

	protected static final List<Block> blockListIgnoreID = new ArrayList<Block>();
	protected static final List<Block> blockListIgnoreMetadata = new ArrayList<Block>();
	protected static final Map<Block, Object> blockListMapping = new HashMap<Block, Object>();

	private final Settings settings = Settings.instance;
	private ItemStack icon;
	private int[][][] blocks;
	private int[][][] metadata;
	private final List<TileEntity> tileEntities = new ArrayList<TileEntity>();
	private final List<ItemStack> blockList = new ArrayList<ItemStack>();
	private short width;
	private short length;
	private short height;

	public SchematicWorld() {
		// TODO: revert is any issues arise
		// super(anvilSaveHandler, "Schematica", null, worldSettings, null);
		super(new SaveHandlerMP(), "Schematica", null, worldSettings, null);
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
			// TODO: fix old icons
			/*
			String[] parts = icon.split(":");
			if (parts.length == 1) {
				this.icon = new ItemStack(Integer.parseInt(parts[0]), 1, 0);
			} else if (parts.length == 2) {
				this.icon = new ItemStack(Integer.parseInt(parts[0]), 1, Integer.parseInt(parts[1]));
			}
			*/
			String[] parts = icon.split(",");
			if (parts.length == 1) {
				this.icon = new ItemStack(GameData.itemRegistry.get(parts[0]), 1, 0);
			} else if (parts.length == 2) {
				this.icon = new ItemStack(GameData.itemRegistry.get(parts[0]), 1, Integer.parseInt(parts[1]));
			}
		} catch (Exception e) {
			Reference.logger.error("Failed to assign an icon!", e);
			this.icon = Settings.defaultIcon.copy();
		}
		this.blocks = blocks.clone();
		this.metadata = metadata.clone();
		if (tileEntities != null) {
			this.tileEntities.addAll(tileEntities);
			for (TileEntity tileEntity : this.tileEntities) {
				tileEntity.setWorldObj(this);
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

		NBTTagList tileEntitiesList = tagCompound.getTagList("TileEntities", 9);

		for (int i = 0; i < tileEntitiesList.tagCount(); i++) {
			TileEntity tileEntity = TileEntity.createAndLoadEntity(tileEntitiesList.getCompoundTagAt(i));
			if (tileEntity != null) {
				tileEntity.setWorldObj(this);
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
		tagCompound.setTag("Icon", tagCompoundIcon);

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
					Block block = GameData.blockRegistry.get(localBlocks[pos]);
					Reference.logger.error(String.format("Block %s[%s] with TileEntity %s failed to save! Replacing with bedrock...", block, block != null ? GameData.blockRegistry.getNameForObject(block) : "?", tileEntity.getClass().getName()), e);
				}
				localBlocks[pos] = (byte) GameData.blockRegistry.getId(Blocks.bedrock);
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

		int x, y, z, itemDamage;
		Object itemID;
		ItemStack itemStack = null;

		for (x = 0; x < this.width; x++) {
			for (y = 0; y < this.height; y++) {
				for (z = 0; z < this.length; z++) {
					itemID = this.getBlock(x, y, z);
					itemDamage = this.metadata[x][y][z];

					if (itemID == null || blockListIgnoreID.contains(itemID)) {
						continue;
					}

					if (blockListIgnoreMetadata.contains(itemID)) {
						itemDamage = 0;
					}

					if (blockListMapping.containsKey(itemID)) {
						itemID = blockListMapping.get(itemID);
					}

					if (itemID == Blocks.log || itemID == Blocks.leaves) {
						itemDamage &= 0x03;
					}

					if (itemID == Blocks.stone_slab || itemID == Blocks.wooden_slab) {
						itemDamage &= 0x07;
					}

					if (itemID instanceof Item) {
						itemDamage = 0;
					}

					if (itemID == Blocks.cocoa) {
						itemDamage = 0x03;
					}

					if (itemID == Items.skull) {
						itemDamage = this.metadata[x][y][z];
					}

					itemStack = null;
					for (ItemStack block : this.blockList) {
						if (itemID instanceof Block) {
							itemID = Item.getItemFromBlock((Block) itemID);
						}

						if (block.getItem() == itemID && block.getItemDamage() == itemDamage) {
							itemStack = block;
							itemStack.stackSize++;
							break;
						}
					}

					if (itemStack == null) {
						ItemStack stack = null;
						if (itemID instanceof Block) {
							stack = new ItemStack((Block) itemID, 1, itemDamage);
						} else if (itemID instanceof Item) {
							stack = new ItemStack((Item) itemID, 1, itemDamage);
						}
						if (stack != null && stack.getItem() != null) {
							this.blockList.add(stack);
						}
					}
				}
			}
		}
		Collections.sort(this.blockList, blockListComparator);
	}

	private int getBlockId(int x, int y, int z) {
		if (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length) {
			return 0;
		}
		return (this.blocks[x][y][z]) & 0xFFF;
	}

	@Override
	public TileEntity getTileEntity(int x, int y, int z) {
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

	/*
	@Override
	public float getLightBrightnessForSky(int var1, int var2, int var3, int var4) {
		return 1.0f;
	}
	*/

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

	/*
	@Override
	public Material getBlockMaterial(int x, int y, int z) {
		return getBlock(x, y, z) != null ? getBlock(x, y, z).getMaterial() : Material.air;
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
	*/

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		return getBlock(x, y, z).isAir(this, x, y, z);
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

	/*
	@Override
	public boolean isBlockSolidOnSide(int x, int y, int z, ForgeDirection side, boolean _default) {
		Block block = getBlock(x, y, z);
		if (block == null) {
			return false;
		}
		return block.isBlockSolidOnSide(this, x, y, z, side);
	}
	*/

	@Override
	public Block getBlock(int x, int y, int z) {
		return GameData.blockRegistry.get(getBlockId(x, y, z));
	}

	public void setTileEntities(List<TileEntity> tileEntities) {
		this.tileEntities.clear();
		this.tileEntities.addAll(tileEntities);
		for (TileEntity tileEntity : this.tileEntities) {
			tileEntity.setWorldObj(this);
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
		tileEntityChest.adjacentChestZPos = null;

		if (getBlock(tileEntityChest.xCoord - 1, tileEntityChest.yCoord, tileEntityChest.zCoord) == Blocks.chest) {
			tileEntityChest.adjacentChestXNeg = (TileEntityChest) getTileEntity(tileEntityChest.xCoord - 1, tileEntityChest.yCoord, tileEntityChest.zCoord);
		}

		if (getBlock(tileEntityChest.xCoord + 1, tileEntityChest.yCoord, tileEntityChest.zCoord) == Blocks.chest) {
			tileEntityChest.adjacentChestXPos = (TileEntityChest) getTileEntity(tileEntityChest.xCoord + 1, tileEntityChest.yCoord, tileEntityChest.zCoord);
		}

		if (getBlock(tileEntityChest.xCoord, tileEntityChest.yCoord, tileEntityChest.zCoord - 1) == Blocks.chest) {
			tileEntityChest.adjacentChestZNeg = (TileEntityChest) getTileEntity(tileEntityChest.xCoord, tileEntityChest.yCoord, tileEntityChest.zCoord - 1);
		}

		if (getBlock(tileEntityChest.xCoord, tileEntityChest.yCoord, tileEntityChest.zCoord + 1) == Blocks.chest) {
			tileEntityChest.adjacentChestZPos = (TileEntityChest) getTileEntity(tileEntityChest.xCoord, tileEntityChest.yCoord, tileEntityChest.zCoord + 1);
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
				int angle = skullTileEntity.func_145906_b();
				int base = 0;
				if (angle <= 7) {
					base = 4;
				} else {
					base = 12;
				}

				skullTileEntity.func_145903_a((2 * base - angle) & 15);
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
				skullTileEntity.func_145903_a((skullTileEntity.func_145906_b() + 12) & 15);
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

	// TODO: needed for the printer, temporarily disabled
	public static boolean isBlock(Object item) {
		return item == Blocks.planks || item == Blocks.sandstone || item == Blocks.wool || item == Blocks.stonebrick || item == Blocks.stained_hardened_clay;
	}

	public static boolean isStair(Object item) {
		return item instanceof BlockStairs;
	}

	public static boolean isSlab(Object item) {
		return item == Blocks.stone_slab || item == Blocks.wooden_slab;
	}

	public static boolean isDoubleSlab(Object item) {
		return item == Blocks.double_stone_slab || item == Blocks.double_wooden_slab;
	}

	public static boolean isPistonBase(Object item) {
		return item == Blocks.sticky_piston || item == Blocks.piston;
	}

	public static boolean isRedstoneRepeater(Object item) {
		return item == Blocks.powered_repeater || item == Blocks.unpowered_repeater;
	}

	public static boolean isTorch(Object item) {
		return item == Blocks.redstone_torch || item == Blocks.unlit_redstone_torch || item == Blocks.torch;
	}

	public static boolean isContainer(Object item) {
		return item == Blocks.lit_furnace || item == Blocks.furnace || item == Blocks.dispenser || item == Blocks.chest || item == Blocks.ender_chest || item == Blocks.trapped_chest;
	}

	public static boolean isButton(Object item) {
		return item == Blocks.stone_button || item == Blocks.wooden_button;
	}

	public static boolean isPumpkin(Object item) {
		return item == Blocks.pumpkin || item == Blocks.lit_pumpkin;
	}

	public static boolean isFluidContainer(Object item) {
		return item == Items.water_bucket || item == Items.lava_bucket;
	}

	public static boolean isMetadataSensitive(Object item) {
		return item == Blocks.anvil || item == Blocks.trapdoor || isTorch(item) || isBlock(item) || isSlab(item) || isDoubleSlab(item) || isPistonBase(item) || isRedstoneRepeater(item) || isContainer(item) || isButton(item) || isPumpkin(item);
	}
}
