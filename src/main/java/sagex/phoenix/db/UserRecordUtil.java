package sagex.phoenix.db;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import sagex.api.UserRecordAPI;

/**
 * Simple UserRecord utils to working with Sage's {@link UserRecordAPI}
 *  
 * @author sean
 */
public class UserRecordUtil {
	public static String getField(String store, String key, String field) {
		Object rec = UserRecordAPI.GetUserRecord(store, key);
		if (rec==null) return null;
		return UserRecordAPI.GetUserRecordData(rec, field);
	}

	public static String getField(String store, String key, String field, String def) {
		Object rec = UserRecordAPI.GetUserRecord(store, key);
		if (rec==null) return def;
		String val = UserRecordAPI.GetUserRecordData(rec, field);
		if (StringUtils.isEmpty(val)) return def;
		return val;
	}

	public static boolean getBoolean(String store, String key, String field, boolean def) {
		String val = getField(store, key, field);
		if (StringUtils.isEmpty(val)) return def;
		return BooleanUtils.toBoolean(val);
	}
	
	public static String getField(Object store, String field) {
		if (store==null) return null;
		return UserRecordAPI.GetUserRecordData(store, field);
	}
	
	public static void setField(String store, String key, String field, String value) {
		Object rec = UserRecordAPI.GetUserRecord(store, key);
		if (rec==null) {
			rec = UserRecordAPI.AddUserRecord(store, key);
		}
		
		UserRecordAPI.SetUserRecordData(rec, field, value);
	}

	public static void clearField(String store, String key, String field) {
		Object rec = UserRecordAPI.GetUserRecord(store, key);
		if (rec==null) {
			return;
		}
		UserRecordAPI.SetUserRecordData(rec, field, null);
	}

	public static void setField(String store, String key, String field, boolean value) {
		Object rec = UserRecordAPI.GetUserRecord(store, key);
		if (rec==null) {
			rec = UserRecordAPI.AddUserRecord(store, key);
		}
		
		UserRecordAPI.SetUserRecordData(rec, field, String.valueOf(value));
	}

	public static void setField(Object store, String field, String value) {
		if (store==null) {
			return;
		}
		
		UserRecordAPI.SetUserRecordData(store, field, value);
	}
}
