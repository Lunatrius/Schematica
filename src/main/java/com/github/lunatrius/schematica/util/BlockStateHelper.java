package com.github.lunatrius.schematica.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static List<String> getFormattedProperties(final IBlockState blockState) {
        final List<String> list = new ArrayList<String>();

        final ImmutableSet<Map.Entry<IProperty, Comparable>> properties = blockState.getProperties().entrySet();
        for (final Map.Entry<IProperty, Comparable> entry : properties) {
            final IProperty key = entry.getKey();
            final Comparable value = entry.getValue();

            String formattedValue = value.toString();
            if (value == Boolean.TRUE) {
                formattedValue = EnumChatFormatting.GREEN + formattedValue + EnumChatFormatting.RESET;
            } else if (value == Boolean.FALSE) {
                formattedValue = EnumChatFormatting.RED + formattedValue + EnumChatFormatting.RESET;
            }

            list.add(key.getName() + ": " + formattedValue);
        }

        return list;
    }
}
