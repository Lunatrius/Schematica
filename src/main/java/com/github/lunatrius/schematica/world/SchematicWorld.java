package com.github.lunatrius.schematica.world;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.config.BlockInfo;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.chunk.ChunkProviderSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SchematicWorld extends WorldClient {
    private static final WorldSettings WORLD_SETTINGS = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);
    private static final Comparator<ItemStack> BLOCK_COMPARATOR = new Comparator<ItemStack>() {
        @Override
        public int compare(ItemStack itemStackA, ItemStack itemStackB) {
            return itemStackA.getUnlocalizedName().compareTo(itemStackB.getUnlocalizedName());
        }
    };

    public static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.grass);

    private ISchematic schematic;

    public final MBlockPos position = new MBlockPos();
    public boolean isRendering;
    public boolean isRenderingLayer;
    public int renderingLayer;

    public SchematicWorld(ISchematic schematic) {
        super(null, WORLD_SETTINGS, 0, EnumDifficulty.PEACEFUL, Minecraft.getMinecraft().mcProfiler);
        this.schematic = schematic;

        for (TileEntity tileEntity : schematic.getTileEntities()) {
            initializeTileEntity(tileEntity);
        }

        this.isRendering = false;
        this.isRenderingLayer = false;
        this.renderingLayer = 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (this.isRenderingLayer && this.renderingLayer != pos.getY()) {
            return Blocks.air.getDefaultState();
        }

        return this.schematic.getBlockState(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
        return this.schematic.setBlockState(pos, state);
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if (this.isRenderingLayer && this.renderingLayer != pos.getY()) {
            return null;
        }

        return this.schematic.getTileEntity(pos);
    }

    @Override
    public void setTileEntity(BlockPos pos, TileEntity tileEntity) {
        this.schematic.setTileEntity(pos, tileEntity);
        initializeTileEntity(tileEntity);
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        this.schematic.removeTileEntity(pos);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        return 15;
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        return 1.0f;
    }

    @Override
    public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
        return getBlockState(pos).getBlock().isNormalCube(this, pos);
    }

    @Override
    public void calculateInitialSkylight() {}

    @Override
    protected void calculateInitialWeather() {}

    @Override
    public void setSpawnPoint(BlockPos pos) {}

    @Override
    protected int getRenderDistanceChunks() {
        return 0;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos).getBlock().isAir(this, pos);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
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
    public boolean isSideSolid(BlockPos pos, EnumFacing side) {
        return isSideSolid(pos, side, false);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
    }

    public void initializeTileEntity(TileEntity tileEntity) {
        tileEntity.setWorldObj(this);
        tileEntity.getBlockType();
        try {
            tileEntity.validate();
        } catch (Exception e) {
            Reference.logger.error(String.format("TileEntity validation for %s failed!", tileEntity.getClass()), e);
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
        final MBlockPos pos = new MBlockPos();

        int itemDamage;
        IBlockState blockState;
        Block block;
        Item item;
        ItemStack itemStack;

        final int width = this.schematic.getWidth();
        final int height = this.schematic.getHeight();
        final int length = this.schematic.getLength();

        for (pos.y = 0; pos.y < height; pos.y++) {
            if (this.isRenderingLayer && pos.y != this.renderingLayer) {
                continue;
            }

            for (pos.z = 0; pos.z < length; pos.z++) {
                for (pos.x = 0; pos.x < width; pos.x++) {
                    blockState = getBlockState(pos);
                    block = blockState.getBlock();
                    item = Item.getItemFromBlock(block);
                    itemDamage = block.getMetaFromState(blockState);

                    if (block == Blocks.air || isAirBlock(pos)) {
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
                        TileEntity tileEntity = getTileEntity(pos);
                        if (tileEntity instanceof TileEntitySkull) {
                            itemDamage = ((TileEntitySkull) tileEntity).getSkullType();
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
        // TODO
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
        // TODO
        /*
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
        */
    }

    public String getDebugDimensions() {
        return "WHL: " + getWidth() + " / " + getHeight() + " / " + getLength();
    }
}
