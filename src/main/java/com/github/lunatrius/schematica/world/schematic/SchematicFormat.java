package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.api.event.PostSchematicCaptureEvent;
import com.github.lunatrius.schematica.reference.Names;
import com.github.lunatrius.schematica.reference.Reference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
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

    public static ISchematic readFromFile(final File file) {
        try {
            final NBTTagCompound tagCompound = SchematicUtil.readTagCompoundFromFile(file);
            final SchematicFormat schematicFormat;
            if (tagCompound.hasKey(Names.NBT.MATERIALS)) {
                final String format = tagCompound.getString(Names.NBT.MATERIALS);
                schematicFormat = FORMATS.get(format);

                if (schematicFormat == null) {
                    throw new UnsupportedFormatException(format);
                }
            } else {
                schematicFormat = FORMATS.get(Names.NBT.FORMAT_STRUCTURE);
            }

            return schematicFormat.readFromNBT(tagCompound);
        } catch (final Exception ex) {
            Reference.logger.error("Failed to read schematic!", ex);
        }

        return null;
    }

    public static ISchematic readFromFile(final File directory, final String filename) {
        return readFromFile(new File(directory, filename));
    }

    public static boolean writeToFile(final File file, final ISchematic schematic) {
        try {
            final PostSchematicCaptureEvent event = new PostSchematicCaptureEvent(schematic);
            MinecraftForge.EVENT_BUS.post(event);

            final NBTTagCompound tagCompound = new NBTTagCompound();

            FORMATS.get(FORMAT_DEFAULT).writeToNBT(tagCompound, schematic);

            final DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

            try {
                NBTTagCompound.writeEntry(Names.NBT.ROOT, tagCompound, dataOutputStream);
            } finally {
                dataOutputStream.close();
            }

            return true;
        } catch (final Exception ex) {
            Reference.logger.error("Failed to write schematic!", ex);
        }

        return false;
    }

    public static boolean writeToFile(final File directory, final String filename, final ISchematic schematic) {
        return writeToFile(new File(directory, filename), schematic);
    }

    public static void writeToFileAndNotify(final File file, final ISchematic schematic, final EntityPlayer player) {
        final boolean success = writeToFile(file, schematic);
        final String message = success ? Names.Command.Save.Message.SAVE_SUCCESSFUL : Names.Command.Save.Message.SAVE_FAILED;
        player.sendMessage(new TextComponentTranslation(message, file.getName()));
    }

    static {
        // TODO?
        // FORMATS.put(Names.NBT.FORMAT_CLASSIC, new SchematicClassic());
        FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());
        FORMATS.put(Names.NBT.FORMAT_STRUCTURE, new SchematicStructure());

        FORMAT_DEFAULT = Names.NBT.FORMAT_STRUCTURE;
    }
}
