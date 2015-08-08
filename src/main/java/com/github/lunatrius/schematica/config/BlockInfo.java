package com.github.lunatrius.schematica.config;

import com.github.lunatrius.schematica.reference.Names;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameData;
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
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

import static com.github.lunatrius.schematica.config.PlacementData.PlacementType;

public class BlockInfo {
    public static final Map<Block, Item> BLOCK_ITEM_MAP = new HashMap<Block, Item>();
    public static final Map<Class, PlacementData> CLASS_PLACEMENT_MAP = new HashMap<Class, PlacementData>();
    public static final Map<Item, PlacementData> ITEM_PLACEMENT_MAP = new HashMap<Item, PlacementData>();

    private static String modId = Names.ModId.MINECRAFT;

    public static void setModId(String modId) {
        BlockInfo.modId = modId;
    }

    public static void populateBlockItemMap() {
        BLOCK_ITEM_MAP.clear();

        /**
         * minecraft
         */
        addBlockItemMapping(Blocks.flowing_water, Items.water_bucket);
        addBlockItemMapping(Blocks.water, Items.water_bucket);
        addBlockItemMapping(Blocks.flowing_lava, Items.lava_bucket);
        addBlockItemMapping(Blocks.lava, Items.lava_bucket);
        addBlockItemMapping(Blocks.bed, Items.bed);
        addBlockItemMapping(Blocks.redstone_wire, Items.redstone);
        addBlockItemMapping(Blocks.wheat, Items.wheat_seeds);
        addBlockItemMapping(Blocks.lit_furnace, Blocks.furnace);
        addBlockItemMapping(Blocks.standing_sign, Items.sign);
        addBlockItemMapping(Blocks.wooden_door, Items.wooden_door);
        addBlockItemMapping(Blocks.iron_door, Items.iron_door);
        addBlockItemMapping(Blocks.wall_sign, Items.sign);
        addBlockItemMapping(Blocks.unlit_redstone_torch, Blocks.redstone_torch);
        addBlockItemMapping(Blocks.reeds, Items.reeds);
        addBlockItemMapping(Blocks.unpowered_repeater, Items.repeater);
        addBlockItemMapping(Blocks.powered_repeater, Items.repeater);
        addBlockItemMapping(Blocks.pumpkin_stem, Items.pumpkin_seeds);
        addBlockItemMapping(Blocks.melon_stem, Items.melon_seeds);
        addBlockItemMapping(Blocks.nether_wart, Items.nether_wart);
        addBlockItemMapping(Blocks.brewing_stand, Items.brewing_stand);
        addBlockItemMapping(Blocks.cauldron, Items.cauldron);
        addBlockItemMapping(Blocks.lit_redstone_lamp, Blocks.redstone_lamp);
        addBlockItemMapping(Blocks.cocoa, Items.dye);
        addBlockItemMapping(Blocks.tripwire, Items.string);
        addBlockItemMapping(Blocks.flower_pot, Items.flower_pot);
        addBlockItemMapping(Blocks.carrots, Items.carrot);
        addBlockItemMapping(Blocks.potatoes, Items.potato);
        addBlockItemMapping(Blocks.skull, Items.skull);
        addBlockItemMapping(Blocks.unpowered_comparator, Items.comparator);
        addBlockItemMapping(Blocks.powered_comparator, Items.comparator);
    }

    private static Item addBlockItemMapping(Block block, Item item) {
        if (block == null || item == null) {
            return null;
        }

        return BLOCK_ITEM_MAP.put(block, item);
    }

    private static Item addBlockItemMapping(Block block, Block item) {
        return addBlockItemMapping(block, Item.getItemFromBlock(item));
    }

    private static Item addBlockItemMapping(Object blockObj, Object itemObj) {
        if (!Names.ModId.MINECRAFT.equals(modId) && !Loader.isModLoaded(modId)) {
            return null;
        }

        Block block = null;
        Item item = null;

        if (blockObj instanceof Block) {
            block = (Block) blockObj;
        } else if (blockObj instanceof String) {
            block = GameData.getBlockRegistry().getObject(String.format("%s:%s", modId, blockObj));
        }

        if (itemObj instanceof Item) {
            item = (Item) itemObj;
        } else if (itemObj instanceof Block) {
            item = Item.getItemFromBlock((Block) itemObj);
        } else if (itemObj instanceof String) {
            String formattedName = String.format("%s:%s", modId, itemObj);
            item = GameData.getItemRegistry().getObject(formattedName);
            if (item == null) {
                item = Item.getItemFromBlock(GameData.getBlockRegistry().getObject(formattedName));
            }
        }

        return addBlockItemMapping(block, item);
    }

    public static Item getItemFromBlock(Block block) {
        Item item = BLOCK_ITEM_MAP.get(block);
        if (item != null) {
            return item;
        }

        return Item.getItemFromBlock(block);
    }

    public static void populatePlacementMaps() {
        ITEM_PLACEMENT_MAP.clear();

        /**
         * minecraft
         */
        addPlacementMapping(BlockButton.class, new PlacementData(PlacementType.BLOCK, -1, -1, 3, 4, 1, 2).setMaskMeta(0x7));
        addPlacementMapping(BlockChest.class, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
        addPlacementMapping(BlockDispenser.class, new PlacementData(PlacementType.PISTON, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
        addPlacementMapping(BlockEnderChest.class, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
        addPlacementMapping(BlockFurnace.class, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 2, 5, 4));
        addPlacementMapping(BlockHopper.class, new PlacementData(PlacementType.BLOCK, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
        addPlacementMapping(BlockPistonBase.class, new PlacementData(PlacementType.PISTON, 0, 1, 2, 3, 4, 5).setMaskMeta(0x7));
        addPlacementMapping(BlockPumpkin.class, new PlacementData(PlacementType.PLAYER, -1, -1, 0, 2, 3, 1).setMaskMeta(0xF));
        addPlacementMapping(BlockRotatedPillar.class, new PlacementData(PlacementType.BLOCK, 0, 0, 8, 8, 4, 4).setMaskMeta(0xC).setMaskMetaInHand(0x3));
        addPlacementMapping(BlockStairs.class, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 2, 1, 0).setOffset(0x4, 0.0f, 1.0f).setMaskMeta(0x3));
        addPlacementMapping(BlockTorch.class, new PlacementData(PlacementType.BLOCK, 5, -1, 3, 4, 1, 2).setMaskMeta(0xF));

        addPlacementMapping(Blocks.dirt, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.planks, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.sandstone, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.wool, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.yellow_flower, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.red_flower, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.double_stone_slab, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.stone_slab, new PlacementData(PlacementType.BLOCK).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x7).setMaskMetaInHand(0x7));
        addPlacementMapping(Blocks.stained_glass, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.ladder, new PlacementData(PlacementType.BLOCK, -1, -1, 3, 2, 5, 4));
        addPlacementMapping(Blocks.lever, new PlacementData(PlacementType.BLOCK, -1, -1, 3, 4, 1, 2).setMaskMeta(0x7));
        addPlacementMapping(Blocks.snow_layer, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0x7));
        addPlacementMapping(Blocks.trapdoor, new PlacementData(PlacementType.BLOCK, -1, -1, 1, 0, 3, 2).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x3));
        addPlacementMapping(Blocks.monster_egg, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.stonebrick, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.tripwire_hook, new PlacementData(PlacementType.BLOCK, -1, -1, 0, 2, 3, 1).setMaskMeta(0x3));
        addPlacementMapping(Blocks.quartz_block, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.fence_gate, new PlacementData(PlacementType.PLAYER, -1, -1, 2, 0, 1, 3).setMaskMeta(0x3));
        addPlacementMapping(Blocks.double_wooden_slab, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.wooden_slab, new PlacementData(PlacementType.BLOCK).setOffset(0x8, 0.0f, 1.0f).setMaskMeta(0x7).setMaskMetaInHand(0x7));
        addPlacementMapping(Blocks.anvil, new PlacementData(PlacementType.PLAYER, -1, -1, 1, 3, 0, 2).setMaskMeta(0x3).setMaskMetaInHand(0xC).setBitShiftMetaInHand(2));
        addPlacementMapping(Blocks.stained_hardened_clay, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.carpet, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Blocks.stained_glass_pane, new PlacementData(PlacementType.BLOCK).setMaskMetaInHand(0xF));
        addPlacementMapping(Items.wooden_door, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 1, 2, 0).setMaskMeta(0x7));
        addPlacementMapping(Items.iron_door, new PlacementData(PlacementType.PLAYER, -1, -1, 3, 1, 2, 0).setMaskMeta(0x7));
        addPlacementMapping(Items.repeater, new PlacementData(PlacementType.PLAYER, -1, -1, 0, 2, 3, 1).setMaskMeta(0x3));
        addPlacementMapping(Items.comparator, new PlacementData(PlacementType.PLAYER, -1, -1, 0, 2, 3, 1).setMaskMeta(0x3));
    }

    public static PlacementData addPlacementMapping(Class clazz, PlacementData data) {
        if (clazz == null || data == null) {
            return null;
        }

        return CLASS_PLACEMENT_MAP.put(clazz, data);
    }

    public static PlacementData addPlacementMapping(Item item, PlacementData data) {
        if (item == null || data == null) {
            return null;
        }

        return ITEM_PLACEMENT_MAP.put(item, data);
    }

    public static PlacementData addPlacementMapping(Block block, PlacementData data) {
        return addPlacementMapping(Item.getItemFromBlock(block), data);
    }

    public static PlacementData addPlacementMapping(Object itemObj, PlacementData data) {
        if (itemObj == null || data == null) {
            return null;
        }

        Item item = null;

        if (itemObj instanceof Item) {
            item = (Item) itemObj;
        } else if (itemObj instanceof Block) {
            item = Item.getItemFromBlock((Block) itemObj);
        } else if (itemObj instanceof String) {
            String formattedName = String.format("%s:%s", modId, itemObj);
            item = GameData.getItemRegistry().getObject(formattedName);
            if (item == null) {
                item = Item.getItemFromBlock(GameData.getBlockRegistry().getObject(formattedName));
            }
        }

        return addPlacementMapping(item, data);
    }

    public static PlacementData getPlacementDataFromItem(Item item) {
        Block block = Block.getBlockFromItem(item);
        PlacementData data = null;

        for (Class clazz : CLASS_PLACEMENT_MAP.keySet()) {
            if (clazz.isInstance(block)) {
                data = CLASS_PLACEMENT_MAP.get(clazz);
                break;
            }
        }

        for (Item i : ITEM_PLACEMENT_MAP.keySet()) {
            if (i == item) {
                data = ITEM_PLACEMENT_MAP.get(i);
                break;
            }
        }

        return data;
    }

    static {
        populateBlockItemMap();
        populatePlacementMaps();
    }
}
