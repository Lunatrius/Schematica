package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.SchematicWorld;
import com.github.lunatrius.schematica.lib.Reference;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicFormat {
	public static final Map<String, SchematicFormat> FORMATS = new HashMap<String, SchematicFormat>();
	public static final String FORMAT_CLASSIC = "Classic";
	public static final String FORMAT_ALPHA = "Alpha";
	public static String FORMAT_DEFAULT;

	public abstract SchematicWorld readFromNBT(NBTTagCompound tagCompound);

	public abstract boolean writeToNBT(NBTTagCompound tagCompound, SchematicWorld world);

	public static SchematicWorld readFromFile(File file) {
		try {
			InputStream stream = new FileInputStream(file);
			NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(stream);
			String format = tagCompound.getString("Materials");
			SchematicFormat schematicFormat = FORMATS.get(format);

			if (schematicFormat == null) {
				throw new UnsupportedFormatException(format);
			}

			return schematicFormat.readFromNBT(tagCompound);
		} catch (Exception ex) {
			Reference.logger.error("Failed to read schematic!", ex);
		}

		return null;
	}

	public static SchematicWorld readFromFile(String directory, String filename) {
		return readFromFile(new File(directory, filename));
	}

	public static boolean writeToFile(File file, SchematicWorld world) {
		try {
			NBTTagCompound tagCompound = new NBTTagCompound();

			// FORMATS.get(FORMAT_DEFAULT).writeToNBT(tagCompound, world);
			world.writeToNBT(tagCompound);

			DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

			try {
				Method method = ReflectionHelper.findMethod(NBTTagCompound.class, null, new String[] {
						"func_150298_a", "a"
				}, String.class, NBTBase.class, DataOutput.class);
				method.invoke(null, "Schematic", tagCompound, dataOutputStream);
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
		FORMATS.put(FORMAT_CLASSIC, new SchematicClassic());
		FORMATS.put(FORMAT_ALPHA, new SchematicAlpha());

		FORMAT_DEFAULT = FORMAT_ALPHA;
	}
}
