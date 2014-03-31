package com.github.lunatrius.schematica.config;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockInfo {
	public static final List<Block> BLOCK_LIST_IGNORE_BLOCK = new ArrayList<Block>();
	public static final List<Block> BLOCK_LIST_IGNORE_METADATA = new ArrayList<Block>();
	public static final Map<Block, Item> BLOCK_ITEM_MAP = new HashMap<Block, Item>();

	private static String prefix = "minecraft";

	public static void populateIgnoredBlocks() {
		BLOCK_LIST_IGNORE_BLOCK.clear();

		/**
		 * minecraft
		 */
		addIgnoredBlock(Blocks.piston_head);
		addIgnoredBlock(Blocks.piston_extension);
		addIgnoredBlock(Blocks.portal);
		addIgnoredBlock(Blocks.end_portal);
	}

	private static boolean addIgnoredBlock(Block block) {
		return BLOCK_LIST_IGNORE_BLOCK.add(block);
	}

	public static void populateIgnoredBlockMetadata() {
		BLOCK_LIST_IGNORE_METADATA.clear();

		/**
		 * minecraft
		 */
		addIgnoredBlockMetadata(Blocks.flowing_water);
		addIgnoredBlockMetadata(Blocks.water);
		addIgnoredBlockMetadata(Blocks.flowing_lava);
		addIgnoredBlockMetadata(Blocks.lava);
		addIgnoredBlockMetadata(Blocks.dispenser);
		addIgnoredBlockMetadata(Blocks.bed);
		addIgnoredBlockMetadata(Blocks.golden_rail);
		addIgnoredBlockMetadata(Blocks.detector_rail);
		addIgnoredBlockMetadata(Blocks.sticky_piston);
		addIgnoredBlockMetadata(Blocks.piston);
		addIgnoredBlockMetadata(Blocks.torch);
		addIgnoredBlockMetadata(Blocks.oak_stairs);
		addIgnoredBlockMetadata(Blocks.chest);
		addIgnoredBlockMetadata(Blocks.redstone_wire);
		addIgnoredBlockMetadata(Blocks.wheat);
		addIgnoredBlockMetadata(Blocks.farmland);
		addIgnoredBlockMetadata(Blocks.furnace);
		addIgnoredBlockMetadata(Blocks.lit_furnace);
		addIgnoredBlockMetadata(Blocks.standing_sign);
		addIgnoredBlockMetadata(Blocks.wooden_door);
		addIgnoredBlockMetadata(Blocks.ladder);
		addIgnoredBlockMetadata(Blocks.rail);
		addIgnoredBlockMetadata(Blocks.stone_stairs);
		addIgnoredBlockMetadata(Blocks.wall_sign);
		addIgnoredBlockMetadata(Blocks.lever);
		addIgnoredBlockMetadata(Blocks.stone_pressure_plate);
		addIgnoredBlockMetadata(Blocks.iron_door);
		addIgnoredBlockMetadata(Blocks.wooden_pressure_plate);
		addIgnoredBlockMetadata(Blocks.unlit_redstone_torch);
		addIgnoredBlockMetadata(Blocks.redstone_torch);
		addIgnoredBlockMetadata(Blocks.stone_button);
		addIgnoredBlockMetadata(Blocks.cactus);
		addIgnoredBlockMetadata(Blocks.reeds);
		addIgnoredBlockMetadata(Blocks.pumpkin);
		addIgnoredBlockMetadata(Blocks.portal);
		addIgnoredBlockMetadata(Blocks.lit_pumpkin);
		addIgnoredBlockMetadata(Blocks.cake);
		addIgnoredBlockMetadata(Blocks.unpowered_repeater);
		addIgnoredBlockMetadata(Blocks.powered_repeater);
		addIgnoredBlockMetadata(Blocks.trapdoor);
		addIgnoredBlockMetadata(Blocks.vine);
		addIgnoredBlockMetadata(Blocks.fence_gate);
		addIgnoredBlockMetadata(Blocks.brick_stairs);
		addIgnoredBlockMetadata(Blocks.stone_brick_stairs);
		addIgnoredBlockMetadata(Blocks.waterlily);
		addIgnoredBlockMetadata(Blocks.nether_brick_stairs);
		addIgnoredBlockMetadata(Blocks.nether_wart);
		addIgnoredBlockMetadata(Blocks.end_portal_frame);
		addIgnoredBlockMetadata(Blocks.redstone_lamp);
		addIgnoredBlockMetadata(Blocks.lit_redstone_lamp);
		addIgnoredBlockMetadata(Blocks.sandstone_stairs);
		addIgnoredBlockMetadata(Blocks.ender_chest);
		addIgnoredBlockMetadata(Blocks.tripwire_hook);
		addIgnoredBlockMetadata(Blocks.tripwire);
		addIgnoredBlockMetadata(Blocks.spruce_stairs);
		addIgnoredBlockMetadata(Blocks.birch_stairs);
		addIgnoredBlockMetadata(Blocks.jungle_stairs);
		addIgnoredBlockMetadata(Blocks.flower_pot);
		addIgnoredBlockMetadata(Blocks.carrots);
		addIgnoredBlockMetadata(Blocks.potatoes);
		addIgnoredBlockMetadata(Blocks.wooden_button);
		addIgnoredBlockMetadata(Blocks.anvil);
	}

	private static boolean addIgnoredBlockMetadata(Block block) {
		return BLOCK_LIST_IGNORE_METADATA.add(block);
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
	}

	private static Item addBlockItemMapping(Block block, Block item) {
		return BLOCK_ITEM_MAP.put(block, Item.getItemFromBlock(item));
	}

	private static Item addBlockItemMapping(Block block, Item item) {
		return BLOCK_ITEM_MAP.put(block, item);
	}

	static {
		populateIgnoredBlocks();
		populateIgnoredBlockMetadata();
		populateBlockItemMap();
	}
}
