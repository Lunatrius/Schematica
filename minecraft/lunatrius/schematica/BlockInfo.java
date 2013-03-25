package lunatrius.schematica;

import java.util.HashMap;
import java.util.Map;

public class BlockInfo {
	private static final Map<String, Integer> ALIAS = new HashMap<String, Integer>();
	private static final Map<Integer, Integer> FLIP = new HashMap<Integer, Integer>();
	private static final Map<Integer, Integer> ROTATION = new HashMap<Integer, Integer>();

	public static boolean addMappingAlias(String key, String value) {
		String k = key;
		int v = parseNumber(value);

		if (v > 0) {
			ALIAS.put(k, v);
			return true;
		}

		return false;
	}

	public static boolean addMappingRotation(String key, String value) {
		return addMapping(key, value, ROTATION);
	}

	public static boolean addMappingFlip(String key, String value) {
		return addMapping(key, value, FLIP);
	}

	private static boolean addMapping(String key, String value, Map<Integer, Integer> map) {
		int k = parseInfo(key);
		int v = parseInfo(value);

		if (k > 0 && v > 0) {
			map.put(k, v);
			return true;
		}

		return false;
	}

	private static int parseInfo(String str) {
		String[] parts = str.split("-");

		int blockId = getBlockId(parts[0]) << 8;
		if (blockId > 0) {
			if (parts.length == 1) {
				return blockId;
			}

			if (parts.length == 2) {
				return blockId | (parseNumber(parts[1]) & 0xF);
			}
		}

		return 0;
	}

	private static int getBlockId(String str) {
		if (ALIAS.containsKey(str)) {
			return ALIAS.get(str);
		}

		return parseNumber(str);
	}

	private static int parseNumber(String str) {
		try {
			return Integer.valueOf(str, 10);
		} catch (NumberFormatException e) {
			Settings.logger.func_98235_b("Could not parse the given number!", e);
		}
		return 0;
	}

	public static int getTransformedMetadataRotation(int blockId, int metadata) {
		return getTransformedMetadata(blockId, metadata, ROTATION);
	}

	public static int getTransformedMetadataFlip(int blockId, int metadata) {
		return getTransformedMetadata(blockId, metadata, FLIP);
	}

	private static int getTransformedMetadata(int blockId, int metadata, Map<Integer, Integer> map) {
		int key = (blockId << 8) | metadata;
		if (map.containsKey(key)) {
			return map.get(key) & 0xF;
		}
		return metadata;
	}
}
