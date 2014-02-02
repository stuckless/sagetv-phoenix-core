package sagex.phoenix.vfs;

/**
 * A "Dummy" media file is a media file that is NOT a media file, but rather
 * something that is more informational. Ie, for online videos, a dummy file is
 * added to the list to show that the items are loading, or a dummy node is
 * added to the list when there is an error loading items.
 * 
 * @author sls
 * 
 */
public class DummyMediaFile extends VirtualMediaFile {
	public DummyMediaFile(String title) {
		super(title);
	}

	public DummyMediaFile(IMediaFolder parent, String id) {
		super(parent, id);
	}

	public DummyMediaFile(IMediaFolder parent, String id, Object resource, String title) {
		super(parent, id, resource, title);
	}

	@Override
	public boolean isType(int type) {
		if (type == MediaResourceType.DUMMY.value()) {
			return true;
		}

		return super.isType(type);
	}
}
