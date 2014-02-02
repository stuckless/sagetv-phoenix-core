package sagex.phoenix.upnp;

import java.util.Collection;
import java.util.Set;

import org.fourthline.cling.model.meta.Device;

import sagex.phoenix.Phoenix;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.VirtualOnlineMediaFolder;

/**
 * Shows all UPnP Devices on the network
 * 
 * @author sls
 */
public class UPnPSourceFactory extends Factory<IMediaFolder> {
	public UPnPSourceFactory() {
	}

	@Override
	public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
		VirtualMediaFolder root = new VirtualOnlineMediaFolder("UPnP Devices");

		Collection<Device> devices = Phoenix.getInstance().getUPnPServer().getMediaServers();
		if (devices != null && devices.size() > 0) {
			for (Device d : devices) {
				root.addMediaResource(new UPnPDeviceMediaFolder(root, d));
			}
		}
		return root;
	}
}
