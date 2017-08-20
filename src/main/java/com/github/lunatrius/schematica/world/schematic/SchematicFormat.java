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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

public abstract class SchematicFormat {
    // LinkedHashMap to ensure defined iteration order
    public static final Map<String, SchematicFormat> FORMATS = new LinkedHashMap<String, SchematicFormat>();
    public static String FORMAT_DEFAULT;

    public abstract ISchematic readFromNBT(NBTTagCompound tagCompound);

    public abstract boolean writeToNBT(NBTTagCompound tagCompound, ISchematic schematic);

    /**
     * Gets the translation key used for this format.
     */
    public abstract String getName();

    /**
     * Gets the file extension used for this format, including the leading dot.
     */
    public abstract String getExtension();

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

    /**
     * Writes the given schematic.
     *
     * @param file The file to write to
     * @param format The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
     * @param schematic The schematic to write
     * @return True if successful
     */
    public static boolean writeToFile(final File file, @Nullable String format, final ISchematic schematic) {
        try {
            if (format == null) {
                format = FORMAT_DEFAULT;
            }

            if (!FORMATS.containsKey(format)) {
                throw new UnsupportedFormatException(format);
            }

            final PostSchematicCaptureEvent event = new PostSchematicCaptureEvent(schematic);
            MinecraftForge.EVENT_BUS.post(event);

            final NBTTagCompound tagCompound = new NBTTagCompound();

            FORMATS.get(format).writeToNBT(tagCompound, schematic);

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

    /**
     * Writes the given schematic.
     *
     * @param directory The directory to write in
     * @param filename The filename (including the extension) to write to
     * @param format The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
     * @param schematic The schematic to write
     * @return True if successful
     */
    public static boolean writeToFile(final File directory, final String filename, @Nullable final String format, final ISchematic schematic) {
        return writeToFile(new File(directory, filename), format, schematic);
    }

    /**
     * Writes the given schematic, notifying the player when finished.
     *
     * @param file The file to write to
     * @param format The format to use, or null for {@linkplain #FORMAT_DEFAULT the default}
     * @param schematic The schematic to write
     * @param player The player to notify
     */
    public static void writeToFileAndNotify(final File file, @Nullable final String format, final ISchematic schematic, final EntityPlayer player) {
        final boolean success = writeToFile(file, format, schematic);
        final String message = success ? Names.Command.Save.Message.SAVE_SUCCESSFUL : Names.Command.Save.Message.SAVE_FAILED;
        player.sendMessage(new TextComponentTranslation(message, file.getName()));
    }

    /**
     * Gets a schematic format name translation key for the given format ID.
     *
     * If an invalid format is chosen, logs a warning and returns a key stating
     * that it's invalid.
     *
     * @param format The format.
     */
    public static String getFormatName(final String format) {
        if (!FORMATS.containsKey(format)) {
            Reference.logger.warn("No format with id {}; returning invalid for name", format, new UnsupportedFormatException(format).fillInStackTrace());
            return Names.Formats.INVALID;
        }
        return FORMATS.get(format).getName();
    }

    /**
     * Gets the extension used by the given format.
     *
     * If the format is invalid, returns the default format's extension.
     *
     * @param format The format (or null to use {@link #FORMAT_DEFAULT the default}).
     */
    public static String getExtension(@Nullable String format) {
        if (format == null) {
            format = FORMAT_DEFAULT;
        }
        if (!FORMATS.containsKey(format)) {
            Reference.logger.warn("No format with id {}; returning default extension", format, new UnsupportedFormatException(format).fillInStackTrace());
            format = FORMAT_DEFAULT;
        }
        return FORMATS.get(format).getExtension();
    }

    static {
        // TODO?
        // FORMATS.put(Names.NBT.FORMAT_CLASSIC, new SchematicClassic());
        FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());
        FORMATS.put(Names.NBT.FORMAT_STRUCTURE, new SchematicStructure());

        FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;
    }
}
