package sagex.phoenix.json;

import org.apache.commons.lang3.math.NumberUtils;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

/**
 * Created by seans on 09/07/16.
 */
public class JSON {
    public interface ArrayVisitor {
        void visitItem(int i, JSONObject item);
    }

    public static <T> T get(String key, JSONObject jo) {
        if (key==null) return null;
        if (jo==null) return null;

        try {
            String keyParts[] = key.split("\\s*\\.\\s*");
            if (keyParts.length == 1) {
                return (T) jo.get(key);
            } else {
                for (int i = 0; i < keyParts.length - 1; i++) {
                    jo = jo.getJSONObject(keyParts[i]);
                }
                return (T) jo.get(keyParts[keyParts.length - 1]);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static int getInt(String key, JSONObject jo) {
        return getInt(key, jo, 0);
    }

    public static int getInt(String key, JSONObject jo, int defValue) {
        Object n = get(key, jo);
        if (n==null) return defValue;
        if (n instanceof Number) {
            return ((Number)n).intValue();
        }
        return NumberUtils.toInt(n.toString(), defValue);
    }

    public static String getString(String key, JSONObject jo) {
        Object o = get(key, jo);
        if (o == null) return null;
        if (o instanceof String) {
            return (String)o;
        }
        return o.toString();
    }

    public static float getFloat(String key, JSONObject jo) {
        Number n = get(key, jo);
        if (n==null) return 0f;
        return n.floatValue();
    }

    public static void each(String key, JSONObject jo, ArrayVisitor visitor) {
        JSONArray arr = get(key, jo);
        if (arr!=null) {
            for (int i=0;i<arr.length();i++) {
                try {
                    visitor.visitItem(i, arr.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
