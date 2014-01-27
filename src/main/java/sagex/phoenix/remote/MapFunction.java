package sagex.phoenix.remote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sagex.phoenix.util.Function;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

public class MapFunction implements Function<String, Map> {
	public MapFunction() {
	}

	@Override
	public Map apply(String data) {
		Map map = new HashMap();
        JSONObject jo;
		try {
			jo = new JSONObject(data);
	        for (Iterator i = jo.keys(); i.hasNext();) {
	            String k = (String) i.next();
	            Object v = jo.opt(k);
	            if (v instanceof JSONArray) {
	            	JSONArray a = ((JSONArray)v);
	            	String s[] = new String[a.length()];
	            	for (int j=0;j<s.length;j++) {
	            		s[j] = a.optString(j);
	            	}
	            	v = s;
	            } else {
	            	v = jo.optString(k);
	            }
	            map.put(k, v);
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return map;
	}
}
