package com.github.lunatrius.schematica.world;

import com.github.lunatrius.core.util.vector.Vector3f;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.config.BlockInfo;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.ChunkProviderSchematic;
import com.github.lunatrius.schematica.world.storage.SaveHandlerSchematic;
import com.github.lunatrius.schematica.world.storage.Schematic;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SchematicWorld extends World {
    private static final WorldSettings WORLD_SETTINGS = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);
    private static final Comparator<ItemStack> BLOCK_COMPARATOR = new Comparator<ItemStack>() {
        @Override
        public int compare(ItemStack itemStackA, ItemStack itemStackB) {
            return itemStackA.getUnlocalizedName().compareTo(itemStackB.getUnlocalizedName());
        }
    };

    public static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.grass);

    private ISchematic schematic;

    public final Vector3i position = new Vector3i();
    public boolean isRendering;
    public boolean isRenderingLayer;
    public int renderingLayer;

    public SchematicWorld(ISchematic schematic) {
        super(new SaveHandlerSchematic(), "Schematica", WORLD_SETTINGS, null, null);
        this.schematic = schematic;

        for (TileEntity tileEntity : schematic.getTileEntities()) {
            initializeTileEntity(tileEntity);
        }

        this.isRendering = false;
        this.isRenderingLayer = false;
        this.renderingLayer = 0;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        if (this.isRenderingLayer && this.renderingLayer != y) {
            return Blocks.air;
        }

        return this.schematic.getBlock(x, y, z);
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block block, int metadata, int flags) {
        return this.schematic.setBlock(x, y, z, block, metadata);
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return this.schematic.getTileEntity(x, y, z);
    }

    @Override
    public void setTileEntity(int x, int y, int z, TileEntity tileEntity) {
        this.schematic.setTileEntity(x, y, z, tileEntity);
        initializeTileEntity(tileEntity);
    }

    @Override
    public void removeTileEntity(int x, int y, int z) {
        this.schematic.removeTileEntity(x, y, z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getSkyBlockTypeBrightness(EnumSkyBlock skyBlock, int x, int y, int z) {
        return 15;
    }

    @Override
    public float getLightBrightness(int x, int y, int z) {
        return 1.0f;
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        return this.schematic.getBlockMetadata(x, y, z);
    }

    @Override
    public boolean isBlockNormalCubeDefault(int x, int y, int z, boolean _default) {
        return getBlock(x, y, z).isNormalCube();
    }

    @Override
    protected int func_152379_p() {
        return 0;
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        return getBlock(x, y, z).isAir(this, x, y, z);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return BiomeGenBase.jungle;
    }

    public int getWidth() {
        return this.schematic.getWidth();
    }

    public int getLength() {
        return this.schematic.getLength();
    }

    @Override
    public int getHeight() {
        return this.schematic.getHeight();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return new ChunkProviderSchematic(this);
    }

    @Override
    public Entity getEntityByID(int id) {
        return null;
    }

    @Override
    public boolean blockExists(int x, int y, int z) {
        return false;
    }

    @Override
    public boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag) {
        return this.schematic.setBlockMetadata(x, y, z, metadata);
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side) {
        return isSideSolid(x, y, z, side, false);
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
    }

    public void initializeTileEntity(TileEntity tileEntity) {
        tileEntity.setWorldObj(this);
        tileEntity.getBlockType();
        try {
            tileEntity.validate();
        } catch (Exception e) {
            Reference.logger.error("TileEntity validation for {} failed!", tileEntity.getClass(), e);
        }
    }

    public void setIcon(ItemStack icon) {
        this.schematic.setIcon(icon);
    }

    public ItemStack getIcon() {
        return this.schematic.getIcon();
    }

    public List<TileEntity> getTileEntities() {
        return this.schematic.getTileEntities();
    }

    public List<ItemStack> getBlockList() {
        final List<ItemStack> blockList = new ArrayList<ItemStack>();

        int x, y, z, itemDamage;
        Block block;
        Item item;
        ItemStack itemStack;

        final int width = this.schematic.getWidth();
        final int height = this.schematic.getHeight();
        final int length = this.schematic.getLength();

        for (y = 0; y < height; y++) {
            if (this.isRenderingLayer && y != this.renderingLayer) {
                continue;
            }

            for (z = 0; z < length; z++) {
                for (x = 0; x < width; x++) {
                    block = getBlock(x, y, z);
                    item = Item.getItemFromBlock(block);
                    itemDamage = getBlockMetadata(x, y, z);

                    if (isAirBlock(x, y, z)) {
                        continue;
                    }

                    if (BlockInfo.BLOCK_LIST_IGNORE_BLOCK.contains(block)) {
                        continue;
                    }

                    if (BlockInfo.BLOCK_LIST_IGNORE_METADATA.contains(block)) {
                        itemDamage = 0;
                    }

                    Item tmp = BlockInfo.BLOCK_ITEM_MAP.get(block);
                    if (tmp != null) {
                        item = tmp;
                        Block blockFromItem = Block.getBlockFromItem(item);
                        if (blockFromItem != Blocks.air) {
                            block = blockFromItem;
                        } else {
                            itemDamage = 0;
                        }
                    }

                    if (block instanceof BlockLog || block instanceof BlockLeavesBase) {
                        itemDamage &= 0x03;
                    }

                    if (block instanceof BlockSlab) {
                        itemDamage &= 0x07;
                    }

                    if (block instanceof BlockDoublePlant) {
                        if ((itemDamage & 0x08) == 0x08) {
                            continue;
                        }
                    }

                    if (block == Blocks.cocoa) {
                        itemDamage = 0x03;
                    }

                    if (item == Items.skull) {
                        TileEntity tileEntity = getTileEntity(x, y, z);
                        if (tileEntity instanceof TileEntitySkull) {
                            itemDamage = ((TileEntitySkull) tileEntity).func_145904_a();
                        }
                    }

                    itemStack = null;
                    for (ItemStack stack : blockList) {
                        if (stack.getItem() == item && stack.getItemDamage() == itemDamage) {
                            itemStack = stack;
                            itemStack.stackSize++;
                            break;
                        }
                    }

                    if (itemStack == null) {
                        itemStack = new ItemStack(item, 1, itemDamage);
                        if (itemStack.getItem() != null) {
                            blockList.add(itemStack);
                        }
                    }
                }
            }
        }

        Collections.sort(blockList, BLOCK_COMPARATOR);

        return blockList;
    }

    public boolean toggleRendering() {
        this.isRendering = !this.isRendering;
        return this.isRendering;
    }

    public void refreshChests() {
        for (TileEntity tileEntity : this.schematic.getTileEntities()) {
            if (tileEntity instanceof TileEntityChest) {
                TileEntityChest tileEntityChest = (TileEntityChest) tileEntity;
                tileEntityChest.adjacentChestChecked = false;
                tileEntityChest.checkForAdjacentChests();
            }
        }
    }

    public void flip() {
        /*
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
		*/
    }

    public void rotate() {
        final ItemStack icon = this.schematic.getIcon();
        final int width = this.schematic.getWidth();
        final int height = this.schematic.getHeight();
        final int length = this.schematic.getLength();

        final ISchematic schematicRotated = new Schematic(icon, length, height, width);

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    try {
                        getBlock(x, y, length - 1 - z).rotateBlock(this, x, y, length - 1 - z, ForgeDirection.UP);
                    } catch (Exception e) {
                        Reference.logger.debug("Failed to rotate block!", e);
                    }

                    final Block block = getBlock(x, y, length - 1 - z);
                    final int metadata = getBlockMetadata(x, y, length - 1 - z);
                    schematicRotated.setBlock(z, y, x, block, metadata);
                }
            }
        }

        for (TileEntity tileEntity : this.schematic.getTileEntities()) {
            final int coord = tileEntity.zCoord;
            tileEntity.zCoord = tileEntity.xCoord;
            tileEntity.xCoord = length - 1 - coord;
            tileEntity.blockMetadata = schematicRotated.getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);

            if (tileEntity instanceof TileEntitySkull && tileEntity.blockMetadata == 0x1) {
                TileEntitySkull skullTileEntity = (TileEntitySkull) tileEntity;
                skullTileEntity.func_145903_a((skullTileEntity.func_145906_b() + 12) & 15);
            }

            schematicRotated.setTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, tileEntity);
        }

        this.schematic = schematicRotated;

        refreshChests();
    }

    public Vector3f dimensions() {
        return new Vector3f(this.schematic.getWidth(), this.schematic.getHeight(), this.schematic.getLength());
    }

    public String getDebugDimensions() {
        return "WHL: " + getWidth() + " / " + getHeight() + " / " + getLength();
    }
}
