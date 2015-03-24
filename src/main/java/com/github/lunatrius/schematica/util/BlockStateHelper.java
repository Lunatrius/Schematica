package com.github.lunatrius.schematica.util;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Set;

public class BlockStateHelper {
    public static IProperty getProperty(final IBlockState blockState, final String name) {
        for (final IProperty prop : (Set<IProperty>) blockState.getProperties().keySet()) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }

        return null;
    }

    public static <T extends Comparable> T getPropertyValue(final IBlockState blockState, final String name) {
        final IProperty property = getProperty(blockState, name);
        if (property == null) {
            throw new IllegalArgumentException(name + " does not exist in " + blockState);
        }

        return (T) blockState.getValue(property);
    }
}
