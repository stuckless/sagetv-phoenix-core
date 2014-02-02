package sagex.phoenix.metadata.proxy;

import sagex.api.UserRecordAPI;
import sagex.phoenix.metadata.IMetadata;

/**
 * Stores Metadata to the UserRecord area
 * 
 * @author sean
 */
public class UserRecordMetadataProxy extends AbstractMetadataProxy {
	private Object rec = null;
	private String storeId;
	private String id;

	public UserRecordMetadataProxy(String storeId, String id) {
		this.storeId = storeId;
		this.id = id;
	}

	@Override
	public void set(SageProperty key, String value) {
		if (getRecord() == null || "Fanart".equals(key.value()))
			return;
		UserRecordAPI.SetUserRecordData(getRecord(), key.value(), value);
	}

	@Override
	public String get(SageProperty key) {
		if (getRecord() == null)
			return null;
		return UserRecordAPI.GetUserRecordData(getRecord(), key.value());
	}

	private Object getRecord() {
		if (rec == null) {
			rec = UserRecordAPI.GetUserRecord(storeId, id);
		}
		return rec;
	}

	public static IMetadata newInstance(String store, String id) {
		return (IMetadata) java.lang.reflect.Proxy.newProxyInstance(UserRecordMetadataProxy.class.getClassLoader(),
				new Class[] { IMetadata.class }, new UserRecordMetadataProxy(store, id));
	}
}
