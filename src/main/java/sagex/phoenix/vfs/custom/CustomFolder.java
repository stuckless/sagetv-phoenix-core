package sagex.phoenix.vfs.custom;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import sagex.api.UserRecordAPI;
import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.impl.FileMediaFile;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import sagex.phoenix.vfs.sage.SageMediaFile;

public class CustomFolder extends VirtualMediaFolder {
	public static final String FIELD_TITLE = FieldName.Title;

	private static enum Type {
		SAGE, FILE, OTHER
	};

	public static final String CustomFolderPrefix = "phoenix.vfs.custom.";

	public CustomFolder(CustomFolders parent, String table, String title) {
		super(parent, CustomFolderPrefix + table, CustomFolderPrefix + table, title, false);
	}

	@Override
	protected void populateChildren(List<IMediaResource> list) {
		Object records[] = UserRecordAPI.GetAllUserRecords(getId());
		if (records != null && records.length > 0) {
			for (Object rec : records) {
				String type = UserRecordAPI.GetUserRecordData(rec, CustomFile.FIELD_CUSTOM_TYPE);
				String id = UserRecordAPI.GetUserRecordData(rec, CustomFile.FIELD_ID);
				if (Type.SAGE.name().equals(type)) {
					Object file = phoenix.media.GetSageMediaFile(NumberUtils.toInt(id, -1));
					if (file != null) {
						list.add(new SageMediaFile(this, file));
					} else {
						UserRecordAPI.DeleteUserRecord(rec);
					}
				} else if (Type.FILE.name().equals(type)) {
					IMediaFile file = (IMediaFile) FileResourceFactory.createResource(this, new File(id));
					if (file.exists()) {
						list.add(file);
					} else {
						UserRecordAPI.DeleteUserRecord(rec);
					}
				} else {
					// assume record has metadata, etc.
					list.add(new CustomFile(this, rec));
				}
			}
		}
	}

	@Override
	public void addMediaResource(IMediaResource res) {
		if (res instanceof IMediaFolder)
			throw new UnsupportedOperationException("Cannot add Folders to a Custom Folder");

		// this forces the children to load, before adding this child.
		// otherwise we may be dupes
		getChildren();

		Object userrecord = getRecord(res.getId());
		if (userrecord == null) {
			Type type = Type.OTHER;
			if (res instanceof SageMediaFile) {
				type = Type.SAGE;
				userrecord = UserRecordAPI.AddUserRecord(getId(), res.getId());
			} else if (res instanceof FileMediaFile) {
				type = Type.FILE;
				userrecord = UserRecordAPI.AddUserRecord(getId(), res.getId());
			} else if (res instanceof CustomFile) {
				// ignore, since we are already added
				return;
			} else {
				userrecord = UserRecordAPI.AddUserRecord(getId(), res.getId());
				UserRecordAPI.SetUserRecordData(userrecord, CustomFile.FIELD_ID, res.getId());
				UserRecordAPI.SetUserRecordData(userrecord, FieldName.Title, res.getTitle());
				UserRecordAPI.SetUserRecordData(userrecord, CustomFile.FIELD_MEDIA_OBJECT, String.valueOf(res.getMediaObject()));
				CustomFile file = new CustomFile(this, userrecord);
				try {
					MetadataUtil.copyMetadata(((IMediaFile) res).getMetadata(), file.getMetadata());
				} catch (Exception e) {
					log.warn("Failed to copy metadata for custom file", e);
				}
				res = file;
			}
			UserRecordAPI.SetUserRecordData(userrecord, CustomFile.FIELD_CUSTOM_TYPE, type.name());
		}

		super.addMediaResource(res);
	}

	private Object getRecord(String id) {
		return UserRecordAPI.GetUserRecord(getId(), id);
	}

	@Override
	public boolean delete(Hints hints) {
		UserRecordAPI.DeleteAllUserRecords(getId());
		setChanged(true);
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		return true;
	}
}
