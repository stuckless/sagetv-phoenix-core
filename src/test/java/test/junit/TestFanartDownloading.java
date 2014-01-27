package test.junit;


import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.fanart.FanartStorage;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.VirtualMediaFile;
import test.InitPhoenix;

public class TestFanartDownloading {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	    InitPhoenix.init(true,true);
	    
	    MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
	    File f = new File("target/junit/Fanart");
	    if (!f.exists()) {
	    	f.mkdirs();
	    }
	    config.setCentralFanartFolder(f.getAbsolutePath());
	}

	@Test
	public void testFanartDownload() throws Exception {
	    MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
		IMediaFile mf = new VirtualMediaFile("Iron Man 2");
		final IMetadata md = MetadataProxy.newInstance();
		md.setMediaType(MediaType.MOVIE.sageValue());
		md.setMediaTitle("Iron Man 2");
		File dlFrom = new File("src/test/java/test/junit/fanart");
		for (File f : dlFrom.listFiles()) {
			md.getFanart().add(new MediaArt(MediaArtifactType.POSTER, f.toURI().toString()));
		}
		FanartStorage storage = new FanartStorage();
		storage.saveFanart(mf, md.getMediaTitle(), md, null, config.getFanartCentralFolder());
	}
}
