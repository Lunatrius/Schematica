package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicFormat {
    public static final Map<String, SchematicFormat> FORMATS = new HashMap<String, SchematicFormat>();
    public static String FORMAT_DEFAULT;

    public abstract SchematicWorld readFromNBT(NBTTagCompound tagCompound);

    public abstract boolean writeToNBT(NBTTagCompound tagCompound, SchematicWorld world);

    public static SchematicWorld readFromFile(File file) {
        try {
            final NBTTagCompound tagCompound = SchematicUtil.readTagCompoundFromFile(file);
            final String format = tagCompound.getString(Names.NBT.MATERIALS);
            final SchematicFormat schematicFormat = FORMATS.get(format);

            if (schematicFormat == null) {
                throw new UnsupportedFormatException(format);
            }

            return schematicFormat.readFromNBT(tagCompound);
        } catch (Exception ex) {
            Reference.logger.error("Failed to read schematic!", ex);
        }

        return null;
    }

    public static SchematicWorld readFromFile(File directory, String filename) {
        return readFromFile(new File(directory, filename));
    }

    public static boolean writeToFile(File file, SchematicWorld world) {
        try {
            NBTTagCompound tagCompound = new NBTTagCompound();

            FORMATS.get(FORMAT_DEFAULT).writeToNBT(tagCompound, world);

            DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

            try {
                NBTTagCompound.func_150298_a(Names.NBT.ROOT, tagCompound, dataOutputStream);
            } finally {
                dataOutputStream.close();
            }

            return true;
        } catch (Exception ex) {
            Reference.logger.error("Failed to write schematic!", ex);
        }

        return false;
    }

    public static boolean writeToFile(File directory, String filename, SchematicWorld world) {
        return writeToFile(new File(directory, filename), world);
    }

    static {
        FORMATS.put(Names.NBT.FORMAT_CLASSIC, new SchematicClassic());
        FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());

        FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;
    }
}
