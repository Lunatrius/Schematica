package lunatrius.schematica.util;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class Config {
	public static String getString(Configuration config, String name, String category, String defaultValue, String comment) {
		return get(config, name, category, defaultValue, comment).value;
	}

	public static boolean getBoolean(Configuration config, String name, String category, boolean defaultValue, String comment) {
		return get(config, name, category, defaultValue, comment).getBoolean(false);
	}

	public static int getInt(Configuration config, String name, String category, int defaultValue, int minValue, int maxValue, String comment) {
		return get(config, name, category, defaultValue, minValue, maxValue, comment).getInt(defaultValue);
	}

	public static float getFloat(Configuration config, String name, String category, float defaultValue, float minValue, float maxValue, String comment) {
		return Float.parseFloat(get(config, name, category, defaultValue, minValue, maxValue, comment).value);
	}

	public static Property get(Configuration config, String name, String category, String defaultValue, String comment) {
		Property prop = config.get(name, category, defaultValue);
		prop.comment = comment + " [default: " + defaultValue + "]";
		return prop;
	}

	public static Property get(Configuration config, String name, String category, boolean defaultValue, String comment) {
		Property prop = config.get(name, category, defaultValue);
		prop.comment = comment + " [default: " + defaultValue + "]";
		return prop;
	}

	public static Property get(Configuration config, String name, String category, int defaultValue, int minValue, int maxValue, String comment) {
		Property prop = config.get(name, category, defaultValue);
		prop.comment = comment + " [range: " + minValue + " ~ " + maxValue + ", default: " + defaultValue + "]";
		prop.value = Integer.toString(prop.getInt(defaultValue) < minValue ? minValue : (prop.getInt(defaultValue) > maxValue ? maxValue : prop.getInt(defaultValue)));
		return prop;
	}

	public static Property get(Configuration config, String name, String category, float defaultValue, float minValue, float maxValue, String comment) {
		Property prop = config.get(name, category, Float.toString(defaultValue));
		prop.comment = comment + " [range: " + minValue + " ~ " + maxValue + ", default: " + defaultValue + "]";
		try {
			prop.value = Float.toString(Float.parseFloat(prop.value) < minValue ? minValue : (Float.parseFloat(prop.value) > maxValue ? maxValue : Float.parseFloat(prop.value)));
		} catch (Exception e) {
			prop.value = "0";
			e.printStackTrace();
		}
		return prop;
	}
}
