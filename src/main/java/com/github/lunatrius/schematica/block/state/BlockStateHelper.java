package com.github.lunatrius.schematica.block.state;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockStateHelper {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends Comparable<T>> IProperty<T> getProperty(final IBlockState blockState, final String name) {
        for (final IProperty prop : blockState.getPropertyKeys()) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }

        return null;
    }

    public static <T extends Comparable<T>> T getPropertyValue(final IBlockState blockState, final String name) {
        final IProperty<T> property = getProperty(blockState, name);
        if (property == null) {
            throw new IllegalArgumentException(name + " does not exist in " + blockState);
        }

        return blockState.getValue(property);
    }

    @SuppressWarnings({ "rawtypes" })
    public static List<String> getFormattedProperties(final IBlockState blockState) {
        final List<String> list = new ArrayList<String>();

        for (final Map.Entry<IProperty<?>, Comparable<?>> entry : blockState.getProperties().entrySet()) {
            final IProperty key = entry.getKey();
            final Comparable value = entry.getValue();

            String formattedValue = value.toString();
            if (Boolean.TRUE.equals(value)) {
                formattedValue = TextFormatting.GREEN + formattedValue + TextFormatting.RESET;
            } else if (Boolean.FALSE.equals(value)) {
                formattedValue = TextFormatting.RED + formattedValue + TextFormatting.RESET;
            }

            list.add(key.getName() + ": " + formattedValue);
        }

        return list;
    }

    public static boolean areBlockStatesEqual(final IBlockState blockStateA, final IBlockState blockStateB) {
        if (blockStateA == blockStateB) {
            return true;
        }

        final Block blockA = blockStateA.getBlock();
        final Block blockB = blockStateB.getBlock();

        return blockA == blockB && blockA.getMetaFromState(blockStateA) == blockB.getMetaFromState(blockStateB);
    }
}
