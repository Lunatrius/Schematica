package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class SchematicUtil {
    public static NBTTagCompound readTagCompoundFromFile(File file) throws IOException {
        try {
            return CompressedStreamTools.readCompressed(new FileInputStream(file));
        } catch (Exception ex) {
            Reference.logger.warn("Failed compressed read, trying normal read...", ex);
            return CompressedStreamTools.read(file);
        }
    }

    public static ItemStack getIconFromName(String iconName) {
        ItemStack icon;
        String name = "";
        int damage = 0;

        String[] parts = iconName.split(",");
        if (parts.length >= 1) {
            name = parts[0];
            if (parts.length >= 2) {
                try {
                    damage = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        icon = new ItemStack(GameData.getBlockRegistry().getObject(name), 1, damage);
        if (icon.getItem() != null) {
            return icon;
        }

        icon = new ItemStack(GameData.getItemRegistry().getObject(name), 1, damage);
        if (icon.getItem() != null) {
            return icon;
        }

        return SchematicWorld.DEFAULT_ICON.copy();
    }

    public static ItemStack getIconFromNBT(NBTTagCompound tagCompound) {
        ItemStack icon = SchematicWorld.DEFAULT_ICON.copy();

        if (tagCompound != null && tagCompound.hasKey(Names.NBT.ICON)) {
            icon.readFromNBT(tagCompound.getCompoundTag(Names.NBT.ICON));

            if (icon.getItem() == null) {
                icon = SchematicWorld.DEFAULT_ICON.copy();
            }
        }

        return icon;
    }

    public static ItemStack getIconFromFile(File file) {
        try {
            return getIconFromNBT(readTagCompoundFromFile(file));
        } catch (Exception e) {
            Reference.logger.error("Failed to read schematic icon!", e);
        }

        return SchematicWorld.DEFAULT_ICON.copy();
    }
}
