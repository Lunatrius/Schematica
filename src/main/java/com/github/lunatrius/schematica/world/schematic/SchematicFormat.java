package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.event.PostSchematicCaptureEvent;
import com.github.lunatrius.schematica.nbt.NBTHelper;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.MinecraftForge;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicFormat {
    public static final Map<String, SchematicFormat> FORMATS = new HashMap<String, SchematicFormat>();
    public static String FORMAT_DEFAULT;

    public abstract ISchematic readFromNBT(NBTTagCompound tagCompound);

    public abstract boolean writeToNBT(NBTTagCompound tagCompound, ISchematic schematic);

    public static ISchematic readFromFile(File file) {
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

    public static ISchematic readFromFile(File directory, String filename) {
        return readFromFile(new File(directory, filename));
    }

    public static boolean writeToFile(File file, ISchematic schematic) {
        try {
            final PostSchematicCaptureEvent event = new PostSchematicCaptureEvent(schematic);
            MinecraftForge.EVENT_BUS.post(event);

            NBTTagCompound tagCompound = new NBTTagCompound();

            FORMATS.get(FORMAT_DEFAULT).writeToNBT(tagCompound, schematic);

            DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

            try {
                NBTTagCompound.writeEntry(Names.NBT.ROOT, tagCompound, dataOutputStream);
            } finally {
                dataOutputStream.close();
            }

            return true;
        } catch (Exception ex) {
            Reference.logger.error("Failed to write schematic!", ex);
        }

        return false;
    }

    public static boolean writeToFile(File directory, String filename, ISchematic schematic) {
        return writeToFile(new File(directory, filename), schematic);
    }

    static {
        FORMATS.put(Names.NBT.FORMAT_CLASSIC, new SchematicClassic());
        FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());

        FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;
    }
}
