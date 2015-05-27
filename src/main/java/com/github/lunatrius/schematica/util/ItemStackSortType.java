package com.github.lunatrius.schematica.util;

import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum ItemStackSortType {
    NAME_ASC("name", "\u2191", new Comparator<ItemStack>() {
        @Override
        public int compare(final ItemStack itemStackA, final ItemStack itemStackB) {
            final String nameA = itemStackA.getItem().getItemStackDisplayName(itemStackA);
            final String nameB = itemStackB.getItem().getItemStackDisplayName(itemStackB);

            return nameA.compareTo(nameB);
        }
    }),
    NAME_DESC("name", "\u2193", new Comparator<ItemStack>() {
        @Override
        public int compare(final ItemStack itemStackA, final ItemStack itemStackB) {
            final String nameA = itemStackA.getItem().getItemStackDisplayName(itemStackA);
            final String nameB = itemStackB.getItem().getItemStackDisplayName(itemStackB);

            return nameB.compareTo(nameA);
        }
    }),
    SIZE_ASC("amount", "\u2191", new Comparator<ItemStack>() {
        @Override
        public int compare(final ItemStack itemStackA, final ItemStack itemStackB) {
            return itemStackA.stackSize - itemStackB.stackSize;
        }
    }),
    SIZE_DESC("amount", "\u2193", new Comparator<ItemStack>() {
        @Override
        public int compare(final ItemStack itemStackA, final ItemStack itemStackB) {
            return itemStackB.stackSize - itemStackA.stackSize;
        }
    });

    private final Comparator<ItemStack> comparator;

    public final String label;
    public final String glyph;

    private ItemStackSortType(final String label, final String glyph, final Comparator<ItemStack> comparator) {
        this.label = label;
        this.glyph = glyph;
        this.comparator = comparator;
    }

    public void sort(final List<ItemStack> blockList) {
        try {
            Collections.sort(blockList, this.comparator);
        } catch (final Exception e) {
            Reference.logger.error("Could not sort the block list!", e);
        }
    }

    public ItemStackSortType next() {
        final ItemStackSortType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ItemStackSortType fromString(final String name) {
        try {
            return valueOf(name);
        } catch (final Exception ignored) {
        }

        return NAME_ASC;
    }
}
