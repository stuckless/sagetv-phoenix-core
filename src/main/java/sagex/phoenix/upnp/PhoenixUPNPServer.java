package sagex.phoenix.upnp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.image.ImageUtil;

public class PhoenixUPNPServer {
	public static final DeviceType SUPPORTED_MEDIA_SERVER_TYPE = new UDADeviceType("MediaServer", 1);

	private Logger log = Logger.getLogger(this.getClass());
	private UPnPConfiguration config = GroupProxy.get(UPnPConfiguration.class);
	private UpnpService service;

	public PhoenixUPNPServer() {
	}

	public void init() {
		if (!config.getClientEnabled()) {
			log.info("UPnP Services are not online");
			return;
		}

		try {
			if (config.getClientEnabled()) {
				RegistryListener rl = new DefaultRegistryListener() {

					@Override
					public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
						// TODO Auto-generated method stub
						super.remoteDeviceAdded(registry, device);
					}

					@Override
					public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
						// TODO Auto-generated method stub
						super.remoteDeviceRemoved(registry, device);
					}

					@Override
					public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
						// TODO Auto-generated method stub
						super.remoteDeviceUpdated(registry, device);
					}

				};
				service = new UpnpServiceImpl(rl);
				log.info("Phoenix is listening to UPnP MediaServer devices");
			} else {
				log.info("Phoenix will not load remote UPnP Servers");
				service = new UpnpServiceImpl();
			}

			// find all media servers on the network
			UpnpHeader devType = new DeviceTypeHeader(SUPPORTED_MEDIA_SERVER_TYPE);
			service.getControlPoint().search(devType);

			// add shutdown hook to shutdown the server
			Thread t = new Thread() {
				@Override
				public void run() {
					shutdown();
				}
			};
			t.setDaemon(true);
			Runtime.getRuntime().addShutdownHook(t);

			if (config.getServerEnabled()) {
				log.info("Phoenix is exposing its VFS over UPnP");
				// Add the bound local device to the registry
				service.getRegistry().addDevice(createDevice());
			}

			log.info("Phoenix UPnP Services are online");
		} catch (Exception ex) {
			log.warn("Phoenix UPnP startup Error", ex);
		}
	}

	public void shutdown() {
		if (service != null) {
			service.shutdown();
		}
	}

	/**
	 * Create device for our Phoenix system.
	 * 
	 * @return
	 * @throws ValidationException
	 * @throws LocalServiceBindingException
	 * @throws IOException
	 */
	private LocalDevice createDevice() throws ValidationException, LocalServiceBindingException, IOException {

		DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("Phoenix Media Server 1.0"));

		DeviceType type = new UDADeviceType("MediaServer", 1);

		DeviceDetails details = new DeviceDetails("Phoenix Media Server", new ManufacturerDetails("Phoenix"), new ModelDetails(
				"PhoenixMediaServer", "Phoenix Media Server", "v1"));

		Icon icon = new Icon(ImageUtil.DEFAULT_IMAGE_MIME_TYPE, 48, 48, 8, getClass().getResource(
				"icon." + ImageUtil.DEFAULT_IMAGE_FORMAT));

		LocalService<VFSContentDirectoryService> vfsService = new AnnotationLocalServiceBinder()
				.read(VFSContentDirectoryService.class);

		vfsService.setManager(new DefaultServiceManager(vfsService, VFSContentDirectoryService.class));

		return new LocalDevice(identity, type, details, icon, vfsService);

		/*
		 * Several services can be bound to the same device: return new
		 * LocalDevice( identity, type, details, icon, new LocalService[]
		 * {switchPowerService, myOtherService} );
		 */
	}

	public Collection<Device> getMediaServers() {
		if (service == null) {
			log.warn("getMediaServers(): UPnP Service is not active.");
			return null;
		}
		return service.getRegistry().getDevices(SUPPORTED_MEDIA_SERVER_TYPE);
	}

	public Collection<Device> getMediaServers(String regex) {
		if (service == null) {
			log.warn("getMediaServers(): UPnP Service is not active.");
			return null;
		}

		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		List<Device> reply = new ArrayList<Device>();
		Collection<Device> devs = service.getRegistry().getDevices(SUPPORTED_MEDIA_SERVER_TYPE);
		if (devs != null) {
			for (Device d : devs) {
				Matcher m = p.matcher(d.getDetails().getFriendlyName());
				if (m.find()) {
					reply.add(d);
				}
			}
		}
		return reply;
	}

	public UpnpService getService() {
		return service;
	}

	public void executeAsync(ActionCallback action) {
		if (service == null) {
			log.warn("executeAsync(): UPnP Service is not active.");
			return;
		}
		service.getControlPoint().execute(action);
	}

	public void executeAndWait(ActionCallback action) {
		if (service == null) {
			log.warn("executeAndWait(): UPnP Service is not active.");
			return;
		}
		action.setControlPoint(service.getControlPoint());
		action.run();
	}
}
