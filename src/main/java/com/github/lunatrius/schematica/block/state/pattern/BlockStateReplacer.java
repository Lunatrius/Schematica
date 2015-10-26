package com.github.lunatrius.schematica.block.state.pattern;

import com.github.lunatrius.core.exceptions.LocalizedException;
import com.github.lunatrius.schematica.reference.Names;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BlockStateReplacer {
    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
    private final IBlockState defaultReplacement;

    private BlockStateReplacer(final IBlockState defaultReplacement) {
        this.defaultReplacement = defaultReplacement;
    }

    public IBlockState getReplacement(final IBlockState original, final Map<IProperty, Comparable> properties) {
        IBlockState replacement = this.defaultReplacement;

        replacement = applyProperties(replacement, original.getProperties());
        replacement = applyProperties(replacement, properties);

        return replacement;
    }

    private IBlockState applyProperties(IBlockState blockState, final Map<IProperty, Comparable> properties) {
        for (final Map.Entry<IProperty, Comparable> entry : properties.entrySet()) {
            try {
                blockState = blockState.withProperty(entry.getKey(), entry.getValue());
            } catch (final IllegalArgumentException ignored) {
            }
        }

        return blockState;
    }

    public static BlockStateReplacer forBlockState(final IBlockState replacement) {
        return new BlockStateReplacer(replacement);
    }

    public static BlockStateHelper getMatcher(final BlockStateInfo blockStateInfo) {
        final BlockStateHelper matcher = BlockStateHelper.forBlock(blockStateInfo.block);
        for (final Map.Entry<IProperty, Comparable> entry : blockStateInfo.stateData.entrySet()) {
            matcher.where(entry.getKey(), new Predicate<Comparable>() {
                @Override
                public boolean apply(final Comparable input) {
                    return input != null && input.equals(entry.getValue());
                }
            });
        }

        return matcher;
    }

    public static BlockStateInfo fromString(final String input) throws LocalizedException {
        final int start = input.indexOf('[');
        final int end = input.indexOf(']');

        final String blockName;
        final String stateData;
        if (start > -1 && end > -1) {
            blockName = input.substring(0, start);
            stateData = input.substring(start + 1, end);
        } else {
            blockName = input;
            stateData = "";
        }

        final Block block = BLOCK_REGISTRY.getRaw(blockName);
        if (block == null) {
            throw new LocalizedException(Names.Messages.INVALID_BLOCK, blockName);
        }

        final Map<IProperty, Comparable> propertyData = parsePropertyData(block.getDefaultState(), stateData, true);
        return new BlockStateInfo(block, propertyData);
    }

    public static Map<IProperty, Comparable> parsePropertyData(final IBlockState blockState, final String stateData, final boolean strict) throws LocalizedException {
        final HashMap<IProperty, Comparable> map = new HashMap<IProperty, Comparable>();
        if (stateData == null || stateData.length() == 0) {
            return map;
        }

        final String[] propertyPairs = stateData.split(",");
        for (final String propertyPair : propertyPairs) {
            final String[] split = propertyPair.split("=");
            if (split.length != 2) {
                throw new LocalizedException(Names.Messages.INVALID_PROPERTY, propertyPair);
            }

            putMatchingProperty(map, blockState, split[0], split[1], strict);
        }

        return map;
    }

    private static boolean putMatchingProperty(final Map<IProperty, Comparable> map, final IBlockState blockState, final String name, final String value, final boolean strict) throws LocalizedException {
        final ImmutableSet<IProperty> properties = blockState.getProperties().keySet();
        for (final IProperty property : properties) {
            if (property.getName().equalsIgnoreCase(name)) {
                final Collection<Comparable> allowedValues = property.getAllowedValues();
                for (final Comparable allowedValue : allowedValues) {
                    if (String.valueOf(allowedValue).equalsIgnoreCase(value)) {
                        map.put(property, allowedValue);
                        return true;
                    }
                }
            }
        }

        if (strict) {
            throw new LocalizedException(Names.Messages.INVALID_PROPERTY_FOR_BLOCK, name + "=" + value, BLOCK_REGISTRY.getNameForObject(blockState.getBlock()));
        }

        return false;
    }

    public static class BlockStateInfo {
        public final Block block;
        public final Map<IProperty, Comparable> stateData;

        public BlockStateInfo(final Block block, final Map<IProperty, Comparable> stateData) {
            this.block = block;
            this.stateData = stateData;
        }
    }
}
