package sagex.phoenix.vfs.custom;

import org.apache.commons.lang.BooleanUtils;

import sagex.api.UserRecordAPI;
import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.proxy.UserRecordMetadataProxy;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualMediaFile;

public class CustomFile extends VirtualMediaFile {
	public static final String FIELD_ID = "ID";
	public static final String FIELD_CUSTOM_TYPE = "CustomType";
	public static final String FIELD_MEDIA_OBJECT = "MediaObject";
	private String objectRef = null;
	public CustomFile(CustomFolder parent, Object record) {
		super(parent, UserRecordAPI.GetUserRecordData(record, FIELD_ID), record, UserRecordAPI.GetUserRecordData(record, FieldName.Title));
		objectRef = UserRecordAPI.GetUserRecordData(record, FIELD_MEDIA_OBJECT);
	}

	@Override
	protected IMetadata createMetadata() {
		return UserRecordMetadataProxy.newInstance(getParent().getId(), getId());
	}

	@Override
	public boolean delete(Hints hints) {
		UserRecordAPI.DeleteUserRecord(getMediaObject());
		return super.delete(hints);
	}

	@Override
	public boolean isType(int type) {
		if (type == MediaResourceType.ONLINE.value()) {
			if (objectRef!=null && objectRef.startsWith("http")) {
				return true;
			}
		}
		return super.isType(type);
	}

	@Override
	public boolean isFlagSet(Flag flag) {
		return BooleanUtils.toBoolean(UserRecordAPI.GetUserRecordData(getMediaObject(), "FLAG_" + flag.name()));
	}

	@Override
	public void setFlag(Flag flag, boolean val) {
		UserRecordAPI.SetUserRecordData(getMediaObject(), "FLAG_" + flag.name(), String.valueOf(val));
	}
}
