package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class SchematicUtil {
    public static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.GRASS);

    public static NBTTagCompound readTagCompoundFromFile(final File file) throws IOException {
        try {
            return CompressedStreamTools.readCompressed(new FileInputStream(file));
        } catch (final Exception ex) {
            Reference.logger.warn("Failed compressed read, trying normal read...", ex);
            return CompressedStreamTools.read(file);
        }
    }

    public static ItemStack getIconFromName(final String iconName) {
        ResourceLocation rl = null;
        int damage = 0;

        final String[] parts = iconName.split(",");
        if (parts.length >= 1) {
            rl = new ResourceLocation(parts[0]);
            if (parts.length >= 2) {
                try {
                    damage = Integer.parseInt(parts[1]);
                } catch (final NumberFormatException ignored) {
                }
            }
        }

        if (rl == null) {
            return DEFAULT_ICON.copy();
        }

        final ItemStack block = new ItemStack(Block.REGISTRY.getObject(rl), 1, damage);
        if (!block.isEmpty()) {
            return block;
        }

        final ItemStack item = new ItemStack(Item.REGISTRY.getObject(rl), 1, damage);
        if (!item.isEmpty()) {
            return item;
        }

        return DEFAULT_ICON.copy();
    }

    public static ItemStack getIconFromNBT(final NBTTagCompound tagCompound) {
        ItemStack icon = DEFAULT_ICON.copy();

        if (tagCompound != null && tagCompound.hasKey(Names.NBT.ICON)) {
            icon.deserializeNBT(tagCompound.getCompoundTag(Names.NBT.ICON));

            if (icon.isEmpty()) {
                icon = DEFAULT_ICON.copy();
            }
        }

        return icon;
    }

    public static ItemStack getIconFromFile(final File file) {
        try {
            return getIconFromNBT(readTagCompoundFromFile(file));
        } catch (final Exception e) {
            Reference.logger.error("Failed to read schematic icon!", e);
        }

        return DEFAULT_ICON.copy();
    }
}
