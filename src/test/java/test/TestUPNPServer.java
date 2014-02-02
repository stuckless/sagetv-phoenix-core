package test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.fourthline.cling.model.meta.Device;

import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.upnp.UPnPDeviceMediaFolder;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.views.OnlineViewFolder;
import sagex.phoenix.vfs.views.ViewFolder;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.StubViewFactory;

public class TestUPNPServer {
	public static void main(String[] args) throws Exception {
		try {
			SimpleStubAPI api = new SimpleStubAPI();
			api.overrideAPI("GetUIContextName", null);
			api.overrideAPI("GetUIContextNames", null);
			api.getProperties().put("phoenix/upnp/clientEnabled", "true");
			SageAPI.setProvider(api);

			InitPhoenix.init(true, false); // don't override the SageAPI so we
											// set false

			System.out.println("Waiting for servers...");
			Collection<Device> devs = null;
			while (true) {
				devs = Phoenix.getInstance().getUPnPServer().getMediaServers("playon");
				if (devs.size() > 0) {
					break;
				}
				Thread.sleep(1000);
				System.out.println("Checking...");
			}

			Device dev = null;
			for (Device d : Phoenix.getInstance().getUPnPServer().getMediaServers()) {
				System.out.println("Device: " + d.getDetails().getFriendlyName());
				if (d.getDetails().getFriendlyName().contains("PlayOn")) {
					dev = d;
				}
			}

			if (dev != null) {
				System.out.println("Creating Folder for device");
				IMediaFolder mf = new UPnPDeviceMediaFolder(null, dev);
				for (IMediaResource r : mf.getChildren()) {
					System.out.println("UPNP Resource: " + r.getTitle() + " [" + r.getId() + "]");
					if (r.getTitle().equals("YouTube")) {
						System.out.println("** Begin Dumping YouTube **");
						for (IMediaResource rr : ((IMediaFolder) r)) {
							System.out.println("rr: " + rr.getTitle());
						}
						System.out.println("** End Dumping YouTube **");
					}
				}

				IMediaFolder mf2 = (IMediaFolder) ((VirtualMediaFolder) mf).findChild("YouTube/Recently Featured");
				IMediaFile r = (IMediaFile) mf2.getChildren().get(0);
				System.out.println("Title: " + r.getTitle());
				System.out.println("Title: " + r.getMetadata().getEpisodeName());
				System.out.println("Title: " + r.getMetadata().getRelativePathWithTitle());

				IMetadata md = ((IMediaFile) r).getMetadata();
				System.out.println("Resolution: " + md.getFormatVideoResolution());
				System.out.println("Duration: " + md.getDuration());
				System.out.println("RunningTime: " + md.getRunningTime());
				System.out.println("MediaType: " + md.getMediaType());
				System.out.println("URL: " + md.getMediaUrl());
				System.out.println("Misc: " + md.getMisc());
				System.out.println("Year: " + md.getYear());
				System.out.println("IsType:  VIDEO: " + r.isType(MediaResourceType.VIDEO.value()));
				System.out.println("IsType: ONLINE: " + r.isType(MediaResourceType.ONLINE.value()));
				System.out.println("");

				System.out.println("** TESTing ViewFolder ***");
				StubViewFactory.registerView("upnp_test", mf2);

				ViewFolder folder = phoenix.umb.CreateView("upnp_test");
				assertTrue("Not an Online View Folder", (folder instanceof OnlineViewFolder));

				// firest time get call getChildren(), should 1 item with please
				// wait..
				List<IMediaResource> ch = folder.getChildren();
				assertEquals(1, ch.size());

				// get the item and then test for please wait
				IMediaFile mf3 = (IMediaFile) ch.get(0);
				assertTrue("Not a please wait item: " + mf3.getTitle(), mf3.getTitle().toLowerCase().contains("please wait"));

				// now force to wait
				assertTrue("Folder didn't load in 5 seconds", ((OnlineViewFolder) folder).isLoaded(5000));

				// test that we have children
				ch = folder.getChildren();
				assertTrue("Folder Size was not more than 0: " + ch.size(), ch.size() > 0);

				// test that we have an item
				System.out.println("ITEM: " + ch.get(0).getTitle());
			}
		} finally {
			System.out.println("Shutting down");
			Phoenix.getInstance().getUPnPServer().shutdown();
			System.out.println("Done");
		}
	}
}
