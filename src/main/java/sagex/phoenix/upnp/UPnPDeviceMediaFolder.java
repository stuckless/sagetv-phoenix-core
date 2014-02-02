package sagex.phoenix.upnp;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.UDAServiceType;

import sagex.phoenix.vfs.IMediaFolder;

public class UPnPDeviceMediaFolder extends BaseUPnPMediaFolder {
	public UPnPDeviceMediaFolder(IMediaFolder parent, Device device) {
		super(parent, device.getIdentity().getUdn().getIdentifierString(), device, device.getDetails().getFriendlyName());
		setService(device.findService(new UDAServiceType("ContentDirectory")));
		setBrowseId("0");
	}
}
