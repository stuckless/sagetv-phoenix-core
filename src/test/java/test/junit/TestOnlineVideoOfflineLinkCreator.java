package test.junit;

import java.io.File;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.upnp.OnlineVideosLinkCreator;
import sagex.phoenix.vfs.VirtualOnlineMediaFile;
import sagex.phoenix.vfs.VirtualOnlineMediaFolder;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;
import test.InitPhoenix;

public class TestOnlineVideoOfflineLinkCreator {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testLinkCreator() {
		// SageOnlineVideosFactory factory = new SageOnlineVideosFactory();
		// Set<ConfigurableOption> opts = new TreeSet<ConfigurableOption>();
		// opts.add(new ConfigurableOption("videodir", new
		// File("src/test/java/test/junit/OnlineVideos").getPath()));
		// IMediaFolder folder = factory.create(opts);

		final VirtualOnlineMediaFolder folder = new VirtualOnlineMediaFolder("Online Videos");
		VirtualOnlineMediaFolder season = new VirtualOnlineMediaFolder(folder, "Season 1");
		VirtualOnlineMediaFile file = new VirtualOnlineMediaFile(season, "01: My Town");
		folder.addMediaResource(season);
		season.addMediaResource(file);

		file.getMetadata().setMediaUrl("http://192.168.1.10/playon/12312312312321.mpg");

		ViewFactory view = new ViewFactory() {
			@Override
			public ViewFolder create(Set<ConfigurableOption> options) {
				return new ViewFolder(this, 0, null, folder);
			}
		};
		view.setName("myview");

		OnlineVideosLinkCreator linkCreator = new OnlineVideosLinkCreator(false);
		linkCreator.makeOfflineLinks(view.create(null), new File("target/junit/OFFLINE_VIDEOS"));
	}
}
