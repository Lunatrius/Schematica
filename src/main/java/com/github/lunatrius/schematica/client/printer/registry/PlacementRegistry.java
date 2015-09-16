package com.github.lunatrius.schematica.client.printer.registry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlacementRegistry {
    public static final PlacementRegistry INSTANCE = new PlacementRegistry();

    private final Map<Class<? extends Block>, PlacementData> classPlacementMap = new HashMap<Class<? extends Block>, PlacementData>();
    private final Map<Block, PlacementData> blockPlacementMap = new HashMap<Block, PlacementData>();
    private final Map<Item, PlacementData> itemPlacementMap = new HashMap<Item, PlacementData>();

    public void populatePlacementMaps() {
        this.classPlacementMap.clear();
        this.blockPlacementMap.clear();
        this.itemPlacementMap.clear();

        final IExtraClick extraClickDoubleSlab = new IExtraClick() {
            @Override
            public int getExtraClicks(Block block, int metadata) {
                return ((BlockSlab) block).isOpaqueCube() ? 1 : 0;
            }
        };

        /**
         * minecraft
         */
        addPlacementMapping(BlockButton.class, new PlacementData(PlacementData.PlacementType.BLOCK, -1, -1, 3, 4, 1, 2).setMaskMeta(0x7));
        addPlacementMapping(BlockChest.class, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
        addPlacementMapping(BlockDispenser.class, new PlacementData(PlacementData.PlacementType.PISTON, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
        addPlacementMapping(BlockEnderChest.class, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
        addPlacementMapping(BlockFurnace.class, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
        addPlacementMapping(BlockHopper.class, new PlacementData(PlacementData.PlacementType.BLOCK, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
        addPlacementMapping(BlockPistonBase.class, new PlacementData(PlacementData.PlacementType.PISTON, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
        addPlacementMapping(BlockPumpkin.class, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 0, 2, 3, 1).setMaskMeta(0xF));
        addPlacementMapping(BlockRotatedPillar.class, new PlacementData(PlacementData.PlacementType.BLOCK, 0, 0, 8, 8, 4, 4).setMaskMeta(0xC));
        addPlacementMapping(BlockStairs.class, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 3, 2, 1, 0).setOffset(0x4, 0.0f, 1.0f).setMaskMeta(0x3));
        addPlacementMapping(BlockTorch.class, new PlacementData(PlacementData.PlacementType.BLOCK, 5, -1, 3, 4, 1, 2).setMaskMeta(0xF));

        addPlacementMapping(Blocks.dirt, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.planks, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.sandstone, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.wool, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.yellow_flower, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.red_flower, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.double_stone_slab, new PlacementData(PlacementData.PlacementType.BLOCK).setExtraClick(extraClickDoubleSlab));
        addPlacementMapping(Blocks.stone_slab, new PlacementData(PlacementData.PlacementType.BLOCK).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x7));
        addPlacementMapping(Blocks.stained_glass, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.ladder, new PlacementData(PlacementData.PlacementType.BLOCK, -1, -1, 3, 2, 5, 4));
        addPlacementMapping(Blocks.lever, new PlacementData(PlacementData.PlacementType.BLOCK, -1, -1, 3, 4, 1, 2).setMaskMeta(0x7));
        addPlacementMapping(Blocks.snow_layer, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.trapdoor, new PlacementData(PlacementData.PlacementType.BLOCK, -1, -1, 1, 0, 3, 2).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x3));
        addPlacementMapping(Blocks.monster_egg, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.stonebrick, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.tripwire_hook, new PlacementData(PlacementData.PlacementType.BLOCK, -1, -1, 0, 2, 3, 1).setMaskMeta(0x3));
        addPlacementMapping(Blocks.quartz_block, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.fence_gate, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 2, 0, 1, 3).setMaskMeta(0x3));
        addPlacementMapping(Blocks.double_wooden_slab, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.wooden_slab, new PlacementData(PlacementData.PlacementType.BLOCK).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x7).setExtraClick(extraClickDoubleSlab));
        addPlacementMapping(Blocks.anvil, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 1, 3, 0, 2).setMaskMeta(0x3));
        addPlacementMapping(Blocks.stained_hardened_clay, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.carpet, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Blocks.stained_glass_pane, new PlacementData(PlacementData.PlacementType.BLOCK));
        addPlacementMapping(Items.wooden_door, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 3, 1, 2, 0).setMaskMeta(0x7));
        addPlacementMapping(Items.iron_door, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 3, 1, 2, 0).setMaskMeta(0x7));
        addPlacementMapping(Items.repeater, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 0, 2, 3, 1).setMaskMeta(0x3));
        addPlacementMapping(Items.comparator, new PlacementData(PlacementData.PlacementType.PLAYER, -1, -1, 0, 2, 3, 1).setMaskMeta(0x3));
    }

    public PlacementData addPlacementMapping(Class<? extends Block> clazz, PlacementData data) {
        if (clazz == null || data == null) {
            return null;
        }

        return this.classPlacementMap.put(clazz, data);
    }

    public PlacementData addPlacementMapping(Block block, PlacementData data) {
        if (block == null || data == null) {
            return null;
        }

        return this.blockPlacementMap.put(block, data);
    }

    public PlacementData addPlacementMapping(Item item, PlacementData data) {
        if (item == null || data == null) {
            return null;
        }

        return this.itemPlacementMap.put(item, data);
    }

    public PlacementData getPlacementData(Block block, ItemStack itemStack) {
        final PlacementData placementDataItem = this.itemPlacementMap.get(itemStack.getItem());
        if (placementDataItem != null) {
            return placementDataItem;
        }

        final PlacementData placementDataBlock = this.blockPlacementMap.get(block);
        if (placementDataBlock != null) {
            return placementDataBlock;
        }

        for (Class<? extends Block> clazz : this.classPlacementMap.keySet()) {
            if (clazz.isInstance(block)) {
                return this.classPlacementMap.get(clazz);
            }
        }

        return null;
    }

    static {
        INSTANCE.populatePlacementMaps();
    }
}
