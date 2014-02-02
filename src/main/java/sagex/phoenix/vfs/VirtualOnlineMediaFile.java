package sagex.phoenix.vfs;

/**
 * Convenience Virtual File that identifies itself an an online video file.
 * 
 * @author sls
 */
public class VirtualOnlineMediaFile extends VirtualMediaFile implements HasPlayableUrl {
	public VirtualOnlineMediaFile(String title) {
		super(title);
	}

	public VirtualOnlineMediaFile(IMediaFolder parent, String id) {
		super(parent, id);
	}

	public VirtualOnlineMediaFile(IMediaFolder parent, String id, Object resource, String title) {
		super(parent, id, resource, title);
	}

	@Override
	public boolean isType(int type) {
		if (type == MediaResourceType.ONLINE.value()) {
			return true;
		}
		return super.isType(type);
	}

	@Override
	public String getUrl() {
		return getMetadata().getMediaUrl();
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public long getEndTime() {
		return getMetadata().getDuration();
	}
}
