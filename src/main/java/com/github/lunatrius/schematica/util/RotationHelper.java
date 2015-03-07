package com.github.lunatrius.schematica.util;

import com.github.lunatrius.core.util.MBlockPos;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.List;
import java.util.Set;

public class RotationHelper {
    public static final RotationHelper INSTANCE = new RotationHelper();

    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
    private static final EnumFacing[][] ROTATION_MATRIX = new EnumFacing[6][];

    public boolean rotate(final SchematicWorld world) {
        return rotate(world, EnumFacing.UP, false);
    }

    public boolean rotate(final SchematicWorld world, final EnumFacing axis, final boolean forced) {
        if (world == null) {
            return false;
        }

        try {
            final ISchematic schematic = world.getSchematic();
            final Schematic schematicRotated = rotate(schematic, axis, forced);

            updatePosition(world, axis);

            world.setSchematic(schematicRotated);

            for (final TileEntity tileEntity : world.getTileEntities()) {
                world.initializeTileEntity(tileEntity);
            }

            return true;
        } catch (final RotationException re) {
            Reference.logger.error(re.getMessage());
        }

        return false;
    }

    private void updatePosition(final SchematicWorld world, final EnumFacing axis) {
        switch (axis) {
        case DOWN:
        case UP: {
            final int offset = (world.getWidth() - world.getLength()) / 2;
            world.position.x += offset;
            world.position.z -= offset;
            break;
        }

        case NORTH:
        case SOUTH: {
            final int offset = (world.getWidth() - world.getHeight()) / 2;
            world.position.x += offset;
            world.position.y -= offset;
            break;
        }

        case WEST:
        case EAST: {
            final int offset = (world.getHeight() - world.getLength()) / 2;
            world.position.y += offset;
            world.position.z -= offset;
            break;
        }
        }
    }

    public Schematic rotate(final ISchematic schematic, final EnumFacing axis, boolean forced) throws RotationException {
        final Vec3i dimensionsRotated = rotateDimensions(axis, schematic.getWidth(), schematic.getHeight(), schematic.getLength());
        final Schematic schematicRotated = new Schematic(schematic.getIcon(), dimensionsRotated.getX(), dimensionsRotated.getY(), dimensionsRotated.getZ());
        final MBlockPos tmp = new MBlockPos();

        for (final MBlockPos pos : MBlockPos.getAllInBox(BlockPos.ORIGIN, new BlockPos(schematic.getWidth() - 1, schematic.getHeight() - 1, schematic.getLength() - 1))) {
            final IBlockState blockState = schematic.getBlockState(pos);
            final IBlockState blockStateRotated = rotateBlock(blockState, axis, forced);
            schematicRotated.setBlockState(rotatePos(pos, axis, dimensionsRotated, tmp), blockStateRotated);
        }

        final List<TileEntity> tileEntities = schematic.getTileEntities();
        for (final TileEntity tileEntity : tileEntities) {
            final BlockPos pos = tileEntity.getPos();
            tileEntity.setPos(new BlockPos(rotatePos(pos, axis, dimensionsRotated, tmp)));
            schematicRotated.setTileEntity(tileEntity.getPos(), tileEntity);
        }

        return schematicRotated;
    }

    private Vec3i rotateDimensions(final EnumFacing axis, final int width, final int height, final int length) throws RotationException {
        switch (axis) {
        case DOWN:
        case UP:
            return new Vec3i(length, height, width);

        case NORTH:
        case SOUTH:
            return new Vec3i(height, width, length);

        case WEST:
        case EAST:
            return new Vec3i(width, length, height);
        }

        throw new RotationException("'%s' is not a valid axis!", axis.getName());
    }

    private BlockPos rotatePos(final BlockPos pos, final EnumFacing axis, final Vec3i dimensions, final MBlockPos rotated) throws RotationException {
        switch (axis) {
        case DOWN:
            return rotated.set(pos.getZ(), pos.getY(), dimensions.getZ() - 1 - pos.getX());

        case UP:
            return rotated.set(dimensions.getX() - 1 - pos.getZ(), pos.getY(), pos.getX());

        case NORTH:
            return rotated.set(dimensions.getX() - 1 - pos.getY(), pos.getX(), pos.getZ());

        case SOUTH:
            return rotated.set(pos.getY(), dimensions.getY() - 1 - pos.getX(), pos.getZ());

        case WEST:
            return rotated.set(pos.getX(), dimensions.getY() - 1 - pos.getZ(), pos.getY());

        case EAST:
            return rotated.set(pos.getX(), pos.getZ(), dimensions.getZ() - 1 - pos.getY());
        }

        throw new RotationException("'%s' is not a valid axis!", axis.getName());
    }

    private IBlockState rotateBlock(final IBlockState blockState, final EnumFacing axis, boolean forced) throws RotationException {
        final PropertyDirection facingProperty = getFacingProperty(blockState);
        if (facingProperty == null) {
            return blockState;
        }

        final Comparable value = blockState.getValue(facingProperty);
        if (value instanceof EnumFacing) {
            final EnumFacing facing = getRotatedFacing(axis, (EnumFacing) value);
            if (facingProperty.getAllowedValues().contains(facing)) {
                return blockState.withProperty(facingProperty, facing);
            }
        }

        if (!forced) {
            throw new RotationException("'%s' cannot be rotated around '%s'", BLOCK_REGISTRY.getNameForObject(blockState.getBlock()), axis);
        }

        return blockState;
    }

    private PropertyDirection getFacingProperty(final IBlockState blockState) {
        for (final IProperty prop : (Set<IProperty>) blockState.getProperties().keySet()) {
            if (prop.getName().equals("facing")) {
                if (prop instanceof PropertyDirection) {
                    return (PropertyDirection) prop;
                }

                Reference.logger.error("'{}': {} is not an instance of {}", BLOCK_REGISTRY.getNameForObject(blockState.getBlock()), prop.getClass().getSimpleName(), PropertyDirection.class.getSimpleName());
            }
        }

        return null;
    }

    private static EnumFacing getRotatedFacing(final EnumFacing axis, final EnumFacing side) {
        return ROTATION_MATRIX[axis.ordinal()][side.ordinal()];
    }

    static {
        ROTATION_MATRIX[EnumFacing.DOWN.ordinal()] = new EnumFacing[] {
                EnumFacing.DOWN, EnumFacing.UP, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH
        };
        ROTATION_MATRIX[EnumFacing.UP.ordinal()] = new EnumFacing[] {
                EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH
        };
        ROTATION_MATRIX[EnumFacing.NORTH.ordinal()] = new EnumFacing[] {
                EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.UP
        };
        ROTATION_MATRIX[EnumFacing.SOUTH.ordinal()] = new EnumFacing[] {
                EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN
        };
        ROTATION_MATRIX[EnumFacing.WEST.ordinal()] = new EnumFacing[] {
                EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST
        };
        ROTATION_MATRIX[EnumFacing.EAST.ordinal()] = new EnumFacing[] {
                EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.WEST, EnumFacing.EAST
        };
    }

    public static class RotationException extends Exception {
        public RotationException(String message, Object... args) {
            super(String.format(message, args));
        }
    }
}
