package sagex.phoenix.upnp;

import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.item.Item;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.vfs.HasPlayableUrl;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualOnlineMediaFile;

public class UPnPMediaFile extends VirtualOnlineMediaFile implements HasPlayableUrl {
	public UPnPMediaFile(IMediaFolder parent, Service service, Item i) {
		super(parent, i.getId(), i, i.getTitle());
	}

	@Override
	public boolean isType(int type) {
		// TODO: Do some checking for music and tv files
		if (type == MediaResourceType.FILE.value()) {
			return true;
		} else if (type == MediaResourceType.ONLINE.value()) {
			return true;
		} else if (type == MediaResourceType.ANY_VIDEO.value()) {
			return true;
		} else if (type == MediaResourceType.VIDEO.value()) {
			return true;
		}
		return super.isType(type);
	}

	@Override
	protected IMetadata createMetadata() {
		return new UPnPMetadata(this);
	}

	@Override
	public String getUrl() {
		return getMetadata().getMediaUrl();
	}
}
