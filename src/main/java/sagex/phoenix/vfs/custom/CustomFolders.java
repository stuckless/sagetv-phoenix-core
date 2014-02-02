package sagex.phoenix.vfs.custom;

import java.util.List;

import sagex.api.UserRecordAPI;
import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;

public class CustomFolders extends VirtualMediaFolder {
	private static final String ID = "phoenix.vfs.customfoldernames";
	private static final String FIELD_TABLE = "Table";
	private static final String FIELD_TITLE = FieldName.Title;

	public CustomFolders() {
		super(null, ID, ID, "Custom Folders");
	}

	@Override
	protected void populateChildren(List<IMediaResource> list) {
		Object folders[] = UserRecordAPI.GetAllUserRecords(getId());
		if (folders != null && folders.length > 0) {
			for (Object f : folders) {
				list.add(new CustomFolder(this, UserRecordAPI.GetUserRecordData(f, FIELD_TABLE), UserRecordAPI.GetUserRecordData(f,
						FIELD_TITLE)));
			}
		}
	}

	@Override
	public boolean delete(Hints hints) {
		UserRecordAPI.DeleteAllUserRecords(getId());
		setChanged(true);
		return true;
	}

	@Override
	public void addMediaResource(IMediaResource res) {
		super.addMediaResource(res);
	}

	public CustomFolder newCustomFolder(String table, String title) {
		IMediaResource res = getChildById(CustomFolder.CustomFolderPrefix + table);
		if (res != null)
			return (CustomFolder) res;

		Object rec = UserRecordAPI.GetUserRecord(getId(), table);
		if (rec == null) {
			CustomFolder f = new CustomFolder(this, table, title);
			Object cust = UserRecordAPI.AddUserRecord(getId(), table);
			UserRecordAPI.SetUserRecordData(cust, FIELD_TABLE, table);
			UserRecordAPI.SetUserRecordData(cust, FIELD_TITLE, title);
			addMediaResource(f);
			return f;
		} else {
			CustomFolder f = new CustomFolder(this, table, title);
			UserRecordAPI.SetUserRecordData(rec, FIELD_TITLE, title);
			addMediaResource(f);
			return f;
		}
	}
}
