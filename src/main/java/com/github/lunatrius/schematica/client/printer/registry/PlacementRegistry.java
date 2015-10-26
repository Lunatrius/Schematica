package com.github.lunatrius.schematica.client.printer.registry;

import com.github.lunatrius.schematica.block.state.BlockStateHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlacementRegistry {
    public static final PlacementRegistry INSTANCE = new PlacementRegistry();

    private final Map<Class<? extends Block>, PlacementData> classPlacementMap = new LinkedHashMap<Class<? extends Block>, PlacementData>();
    private final Map<Block, PlacementData> blockPlacementMap = new HashMap<Block, PlacementData>();
    private final Map<Item, PlacementData> itemPlacementMap = new HashMap<Item, PlacementData>();

    private void populateMappings() {
        this.classPlacementMap.clear();
        this.blockPlacementMap.clear();
        this.itemPlacementMap.clear();

        final IValidPlayerFacing playerFacingEntity = new IValidPlayerFacing() {
            @Override
            public boolean isValid(final IBlockState blockState, final EntityPlayer player, final BlockPos pos, final World world) {
                final EnumFacing facing = BlockStateHelper.getPropertyValue(blockState, "facing");
                return facing == player.getHorizontalFacing();
            }
        };
        final IValidPlayerFacing playerFacingEntityOpposite = new IValidPlayerFacing() {
            @Override
            public boolean isValid(final IBlockState blockState, final EntityPlayer player, final BlockPos pos, final World world) {
                final EnumFacing facing = BlockStateHelper.getPropertyValue(blockState, "facing");
                return facing == player.getHorizontalFacing().getOpposite();
            }
        };
        final IValidPlayerFacing playerFacingPiston = new IValidPlayerFacing() {
            @Override
            public boolean isValid(final IBlockState blockState, final EntityPlayer player, final BlockPos pos, final World world) {
                final EnumFacing facing = BlockStateHelper.getPropertyValue(blockState, "facing");
                return facing == BlockPistonBase.getFacingFromEntity(world, pos, player);
            }
        };
        final IValidPlayerFacing playerFacingRotateY = new IValidPlayerFacing() {
            @Override
            public boolean isValid(final IBlockState blockState, final EntityPlayer player, final BlockPos pos, final World world) {
                final EnumFacing facing = BlockStateHelper.getPropertyValue(blockState, "facing");
                return facing == player.getHorizontalFacing().rotateY();
            }
        };
        final IValidPlayerFacing playerFacingLever = new IValidPlayerFacing() {
            @Override
            public boolean isValid(final IBlockState blockState, final EntityPlayer player, final BlockPos pos, final World world) {
                final BlockLever.EnumOrientation value = (BlockLever.EnumOrientation) blockState.getValue(BlockLever.FACING);
                return !value.getFacing().getAxis().isVertical() || BlockLever.EnumOrientation.forFacings(value.getFacing(), player.getHorizontalFacing()) == value;
            }
        };
        final IValidPlayerFacing playerFacingStandingSign = new IValidPlayerFacing() {
            @Override
            public boolean isValid(final IBlockState blockState, final EntityPlayer player, final BlockPos pos, final World world) {
                final int value = (Integer) blockState.getValue(BlockStandingSign.ROTATION);
                final int facing = MathHelper.floor_double((player.rotationYaw + 180.0) * 16.0 / 360.0 + 0.5) & 15;
                return value == facing;
            }
        };
        final IValidPlayerFacing playerFacingIgnore = new IValidPlayerFacing() {
            @Override
            public boolean isValid(final IBlockState blockState, final EntityPlayer player, final BlockPos pos, final World world) {
                return false;
            }
        };

        final IOffset offsetSlab = new IOffset() {
            @Override
            public float getOffset(final IBlockState blockState) {
                if (!((BlockSlab) blockState.getBlock()).isDouble()) {
                    final BlockSlab.EnumBlockHalf half = (BlockSlab.EnumBlockHalf) blockState.getValue(BlockSlab.HALF);
                    return half == BlockSlab.EnumBlockHalf.TOP ? 1 : 0;
                }

                return 0;
            }
        };
        final IOffset offsetStairs = new IOffset() {
            @Override
            public float getOffset(final IBlockState blockState) {
                final BlockStairs.EnumHalf half = (BlockStairs.EnumHalf) blockState.getValue(BlockStairs.HALF);
                return half == BlockStairs.EnumHalf.TOP ? 1 : 0;
            }
        };
        final IOffset offsetTrapDoor = new IOffset() {
            @Override
            public float getOffset(final IBlockState blockState) {
                final BlockTrapDoor.DoorHalf half = (BlockTrapDoor.DoorHalf) blockState.getValue(BlockTrapDoor.HALF);
                return half == BlockTrapDoor.DoorHalf.TOP ? 1 : 0;
            }
        };

        final IValidBlockFacing blockFacingLog = new IValidBlockFacing() {
            @Override
            public List<EnumFacing> getValidBlockFacings(final List<EnumFacing> solidSides, final IBlockState blockState) {
                final List<EnumFacing> list = new ArrayList<EnumFacing>();

                final BlockLog.EnumAxis axis = (BlockLog.EnumAxis) blockState.getValue(BlockLog.LOG_AXIS);
                for (final EnumFacing side : solidSides) {
                    if (axis != BlockLog.EnumAxis.fromFacingAxis(side.getAxis())) {
                        continue;
                    }

                    list.add(side);
                }

                return list;
            }
        };
        final IValidBlockFacing blockFacingPillar = new IValidBlockFacing() {
            @Override
            public List<EnumFacing> getValidBlockFacings(final List<EnumFacing> solidSides, final IBlockState blockState) {
                final List<EnumFacing> list = new ArrayList<EnumFacing>();

                final EnumFacing.Axis axis = (EnumFacing.Axis) blockState.getValue(BlockRotatedPillar.AXIS);
                for (final EnumFacing side : solidSides) {
                    if (axis != side.getAxis()) {
                        continue;
                    }

                    list.add(side);
                }

                return list;
            }
        };
        final IValidBlockFacing blockFacingOpposite = new IValidBlockFacing() {
            @Override
            public List<EnumFacing> getValidBlockFacings(final List<EnumFacing> solidSides, final IBlockState blockState) {
                final List<EnumFacing> list = new ArrayList<EnumFacing>();

                final IProperty propertyFacing = BlockStateHelper.getProperty(blockState, "facing");
                if (propertyFacing != null && propertyFacing.getValueClass().equals(EnumFacing.class)) {
                    final EnumFacing facing = ((EnumFacing) blockState.getValue(propertyFacing));
                    for (final EnumFacing side : solidSides) {
                        if (facing.getOpposite() != side) {
                            continue;
                        }

                        list.add(side);
                    }
                }

                return list;
            }
        };
        final IValidBlockFacing blockFacingSame = new IValidBlockFacing() {
            @Override
            public List<EnumFacing> getValidBlockFacings(final List<EnumFacing> solidSides, final IBlockState blockState) {
                final List<EnumFacing> list = new ArrayList<EnumFacing>();

                final IProperty propertyFacing = BlockStateHelper.getProperty(blockState, "facing");
                if (propertyFacing != null && propertyFacing.getValueClass().equals(EnumFacing.class)) {
                    final EnumFacing facing = (EnumFacing) blockState.getValue(propertyFacing);
                    for (final EnumFacing side : solidSides) {
                        if (facing != side) {
                            continue;
                        }

                        list.add(side);
                    }
                }

                return list;
            }
        };
        final IValidBlockFacing blockFacingHopper = new IValidBlockFacing() {
            @Override
            public List<EnumFacing> getValidBlockFacings(final List<EnumFacing> solidSides, final IBlockState blockState) {
                final List<EnumFacing> list = new ArrayList<EnumFacing>();

                final EnumFacing facing = (EnumFacing) blockState.getValue(BlockHopper.FACING);
                for (final EnumFacing side : solidSides) {
                    if (facing != side) {
                        continue;
                    }

                    list.add(side);
                }

                return list;
            }
        };
        final IValidBlockFacing blockFacingLever = new IValidBlockFacing() {
            @Override
            public List<EnumFacing> getValidBlockFacings(final List<EnumFacing> solidSides, final IBlockState blockState) {
                final List<EnumFacing> list = new ArrayList<EnumFacing>();

                final BlockLever.EnumOrientation facing = (BlockLever.EnumOrientation) blockState.getValue(BlockLever.FACING);
                for (final EnumFacing side : solidSides) {
                    if (facing.getFacing().getOpposite() != side) {
                        continue;
                    }

                    list.add(side);
                }

                return list;
            }
        };
        final IValidBlockFacing blockFacingQuartz = new IValidBlockFacing() {
            @Override
            public List<EnumFacing> getValidBlockFacings(final List<EnumFacing> solidSides, final IBlockState blockState) {
                final List<EnumFacing> list = new ArrayList<EnumFacing>();

                final BlockQuartz.EnumType variant = (BlockQuartz.EnumType) blockState.getValue(BlockQuartz.VARIANT);
                for (final EnumFacing side : solidSides) {
                    if (variant == BlockQuartz.EnumType.LINES_X && side.getAxis() != EnumFacing.Axis.X) {
                        continue;
                    } else if (variant == BlockQuartz.EnumType.LINES_Y && side.getAxis() != EnumFacing.Axis.Y) {
                        continue;
                    } else if (variant == BlockQuartz.EnumType.LINES_Z && side.getAxis() != EnumFacing.Axis.Z) {
                        continue;
                    }

                    list.add(side);
                }

                return list;
            }
        };

        final IExtraClick extraClickDoubleSlab = new IExtraClick() {
            @Override
            public int getExtraClicks(final IBlockState blockState) {
                return ((BlockSlab) blockState.getBlock()).isDouble() ? 1 : 0;
            }
        };

        /**
         * minecraft
         */
        // extends BlockRotatedPillar
        addPlacementMapping(BlockLog.class, new PlacementData(blockFacingLog));

        addPlacementMapping(BlockButton.class, new PlacementData(blockFacingOpposite));
        addPlacementMapping(BlockChest.class, new PlacementData(playerFacingEntityOpposite));
        addPlacementMapping(BlockDispenser.class, new PlacementData(playerFacingPiston));
        addPlacementMapping(BlockDoor.class, new PlacementData(playerFacingEntity));
        addPlacementMapping(BlockEnderChest.class, new PlacementData(playerFacingEntityOpposite));
        addPlacementMapping(BlockFenceGate.class, new PlacementData(playerFacingEntity));
        addPlacementMapping(BlockFurnace.class, new PlacementData(playerFacingEntityOpposite));
        addPlacementMapping(BlockHopper.class, new PlacementData(blockFacingHopper));
        addPlacementMapping(BlockPistonBase.class, new PlacementData(playerFacingPiston));
        addPlacementMapping(BlockPumpkin.class, new PlacementData(playerFacingEntityOpposite));
        addPlacementMapping(BlockRotatedPillar.class, new PlacementData(blockFacingPillar));
        addPlacementMapping(BlockSlab.class, new PlacementData().setOffsetY(offsetSlab).setExtraClick(extraClickDoubleSlab));
        addPlacementMapping(BlockStairs.class, new PlacementData(playerFacingEntity).setOffsetY(offsetStairs));
        addPlacementMapping(BlockTorch.class, new PlacementData(blockFacingOpposite));
        addPlacementMapping(BlockTrapDoor.class, new PlacementData(blockFacingOpposite).setOffsetY(offsetTrapDoor));

        addPlacementMapping(Blocks.anvil, new PlacementData(playerFacingRotateY));
        addPlacementMapping(Blocks.cocoa, new PlacementData(blockFacingSame));
        addPlacementMapping(Blocks.end_portal_frame, new PlacementData(playerFacingEntityOpposite));
        addPlacementMapping(Blocks.ladder, new PlacementData(blockFacingOpposite));
        addPlacementMapping(Blocks.lever, new PlacementData(playerFacingLever, blockFacingLever));
        addPlacementMapping(Blocks.quartz_block, new PlacementData(blockFacingQuartz));
        addPlacementMapping(Blocks.standing_sign, new PlacementData(playerFacingStandingSign));
        addPlacementMapping(Blocks.tripwire_hook, new PlacementData(blockFacingOpposite));
        addPlacementMapping(Blocks.wall_sign, new PlacementData(blockFacingOpposite));

        addPlacementMapping(Items.comparator, new PlacementData(playerFacingEntityOpposite));
        addPlacementMapping(Items.repeater, new PlacementData(playerFacingEntityOpposite));

        addPlacementMapping(Blocks.bed, new PlacementData(playerFacingIgnore));
        addPlacementMapping(Blocks.end_portal, new PlacementData(playerFacingIgnore));
        addPlacementMapping(Blocks.piston_extension, new PlacementData(playerFacingIgnore));
        addPlacementMapping(Blocks.piston_head, new PlacementData(playerFacingIgnore));
        addPlacementMapping(Blocks.portal, new PlacementData(playerFacingIgnore));
        addPlacementMapping(Blocks.skull, new PlacementData(playerFacingIgnore));
        addPlacementMapping(Blocks.standing_banner, new PlacementData(playerFacingIgnore));
        addPlacementMapping(Blocks.wall_banner, new PlacementData(playerFacingIgnore));
    }

    private PlacementData addPlacementMapping(final Class<? extends Block> clazz, final PlacementData data) {
        if (clazz == null || data == null) {
            return null;
        }

        return this.classPlacementMap.put(clazz, data);
    }

    private PlacementData addPlacementMapping(final Block block, final PlacementData data) {
        if (block == null || data == null) {
            return null;
        }

        return this.blockPlacementMap.put(block, data);
    }

    private PlacementData addPlacementMapping(final Item item, final PlacementData data) {
        if (item == null || data == null) {
            return null;
        }

        return this.itemPlacementMap.put(item, data);
    }

    public PlacementData getPlacementData(final IBlockState blockState, final ItemStack itemStack) {
        final Item item = itemStack.getItem();

        final PlacementData placementDataItem = this.itemPlacementMap.get(item);
        if (placementDataItem != null) {
            return placementDataItem;
        }

        final Block block = blockState.getBlock();

        final PlacementData placementDataBlock = this.blockPlacementMap.get(block);
        if (placementDataBlock != null) {
            return placementDataBlock;
        }

        for (final Class<? extends Block> clazz : this.classPlacementMap.keySet()) {
            if (clazz.isInstance(block)) {
                return this.classPlacementMap.get(clazz);
            }
        }

        return null;
    }

    static {
        INSTANCE.populateMappings();
    }
}
