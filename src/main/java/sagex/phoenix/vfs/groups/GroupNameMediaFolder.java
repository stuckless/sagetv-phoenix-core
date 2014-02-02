package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.VirtualMediaFolder;

public class GroupNameMediaFolder extends VirtualMediaFolder {
	public GroupNameMediaFolder(IMediaFolder parent, String title) {
		super(parent, title, null, title, true);
	}
}
