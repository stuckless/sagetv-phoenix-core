package sagex.phoenix.upnp;

import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.model.container.Container;

import sagex.phoenix.vfs.IMediaFolder;

public class UPnPMediaFolder extends BaseUPnPMediaFolder {
	public UPnPMediaFolder(IMediaFolder parent, Service service, Container c) {
		super(parent, c.getId(), c, c.getTitle());
		setService(service);
		setBrowseId(c.getId());
	}
}
