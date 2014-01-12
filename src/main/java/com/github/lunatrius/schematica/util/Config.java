package com.github.lunatrius.schematica.util;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class Config {
	private static final String FORMAT_NORMAL = "%1$s [default: %2$s]";
	private static final String FORMAT_RANGE = "%1$s [range: %2$s ~ %3$s, default: %4$s]";

	public static String getString(Configuration config, String category, String key, String defaultValue, String comment) {
		return get(config, category, key, defaultValue, comment).getString();
	}

	public static boolean getBoolean(Configuration config, String category, String key, boolean defaultValue, String comment) {
		return get(config, category, key, defaultValue, comment).getBoolean(defaultValue);
	}

	public static int getInt(Configuration config, String category, String key, int defaultValue, int minValue, int maxValue, String comment) {
		return get(config, category, key, defaultValue, minValue, maxValue, comment).getInt(defaultValue);
	}

	public static double getDouble(Configuration config, String category, String key, double defaultValue, double minValue, double maxValue, String comment) {
		return get(config, category, key, defaultValue, minValue, maxValue, comment).getDouble(defaultValue);
	}

	public static String[] getStringList(Configuration config, String category, String key, String[] defaultValue, String comment) {
		return get(config, category, key, defaultValue, comment).getStringList();
	}

	public static boolean[] getBooleanList(Configuration config, String category, String key, boolean[] defaultValue, String comment) {
		return get(config, category, key, defaultValue, comment).getBooleanList();
	}

	public static int[] getIntList(Configuration config, String category, String key, int[] defaultValue, String comment) {
		return get(config, category, key, defaultValue, comment).getIntList();
	}

	public static double[] getDoubleList(Configuration config, String category, String key, double[] defaultValue, String comment) {
		return get(config, category, key, defaultValue, comment).getDoubleList();
	}

	public static Property get(Configuration config, String category, String key, String defaultValue, String comment) {
		Property property = config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, defaultValue);
		return property;
	}

	public static Property get(Configuration config, String category, String key, boolean defaultValue, String comment) {
		Property property = config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, defaultValue);
		return property;
	}

	public static Property get(Configuration config, String category, String key, int defaultValue, int minValue, int maxValue, String comment) {
		Property property = config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_RANGE, comment, minValue, maxValue, defaultValue);
		int value = property.getInt(defaultValue);
		property.set(value < minValue ? minValue : (value > maxValue ? maxValue : value));
		return property;
	}

	public static Property get(Configuration config, String category, String key, double defaultValue, double minValue, double maxValue, String comment) {
		Property property = config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_RANGE, comment, minValue, maxValue, defaultValue);
		double value = property.getDouble(defaultValue);
		property.set(value < minValue ? minValue : (value > maxValue ? maxValue : value));
		return property;
	}

	public static Property get(Configuration config, String category, String key, String[] defaultValue, String comment) {
		Property property = config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, getDefaultListString(property));
		return property;
	}

	public static Property get(Configuration config, String category, String key, boolean[] defaultValue, String comment) {
		Property property = config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, getDefaultListString(property));
		return property;
	}

	public static Property get(Configuration config, String category, String key, int[] defaultValue, String comment) {
		Property property = config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, getDefaultListString(property));
		return property;
	}

	public static Property get(Configuration config, String category, String key, double[] defaultValue, String comment) {
		Property property = config.get(category, key, defaultValue);
		property.comment = String.format(FORMAT_NORMAL, comment, getDefaultListString(property));
		return property;
	}

	private static String getDefaultListString(Property property) {
		String[] defaultValues = property.getStringList();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < defaultValues.length; i++) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(defaultValues[i]);
		}
		return sb.toString();
	}
}
