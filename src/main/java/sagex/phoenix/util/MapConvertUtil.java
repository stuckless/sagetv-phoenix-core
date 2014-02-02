package sagex.phoenix.util;

import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;

public class MapConvertUtil {
	public static String getString(Map<String, String> map, String key, String defValue) {
		if (map == null)
			return defValue;
		String val = map.get(key);
		if (val == null)
			val = defValue;
		return val;
	}

	public static int getInt(Map<String, String> map, String key, int defValue) {
		if (map == null)
			return defValue;
		String val = map.get(key);
		if (val == null)
			return defValue;
		return NumberUtils.toInt(val, defValue);
	}

	public static boolean getBoolean(Map<String, String> map, String key, boolean defValue) {
		if (map == null)
			return defValue;
		String val = map.get(key);
		if (val == null)
			return defValue;
		return BooleanUtils.toBoolean(val);
	}

	public static long getLong(Map<String, String> map, String key, long defValue) {
		if (map == null)
			return defValue;
		String val = map.get(key);
		if (val == null)
			return defValue;
		return NumberUtils.toLong(val, defValue);
	}
}
