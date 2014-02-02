package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class TitleGrouper implements IGrouper {

	public TitleGrouper() {
	}

	@Override
	public String getGroupName(IMediaResource res) {
		String group = null;
		if (res instanceof IMediaFile) {
			group = ((IMediaFile) res).getMetadata().getMediaTitle();
		}

		if (group == null || group.isEmpty()) {
			return res.getTitle();
		} else {
			return group;
		}
	}
}
