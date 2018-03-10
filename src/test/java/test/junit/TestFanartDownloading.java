package test.junit;

import java.io.File;
import java.io.FileFilter;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.fanart.FanartStorage;
import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.VirtualMediaFile;
import sagex.util.WaitFor;
import test.InitPhoenix;

import static org.junit.Assert.*;

public class TestFanartDownloading {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InitPhoenix.init(true, true);

        MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
        File f = InitPhoenix.ProjectHome("target/junit/Fanart");
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
        File dlFrom = InitPhoenix.ProjectHome("src/test/java/test/junit/fanart");
        File fromFanart=null;
        for (File f : dlFrom.listFiles()) {
            // should only be a single file
            md.getFanart().add(new MediaArt(MediaArtifactType.POSTER, f.toURI().toString()));
            fromFanart=f;
        }
        assertNotNull(fromFanart);
        assertTrue(fromFanart.length()>0);
        FanartStorage storage = new FanartStorage();
        storage.saveFanart(mf, md.getMediaTitle(), md, null, config.getFanartCentralFolder());

    }

    @Test
    public void testFanartDownloadRemote() throws Exception {
        MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
        IMediaFile mf = new VirtualMediaFile("Lethal Weapon");
        final IMetadata md = MetadataProxy.newInstance();
        md.setMediaType(MediaType.TV.sageValue());
        md.setMediaTitle("Leathal Weapon");
        md.getFanart().add(new MediaArt(MediaArtifactType.POSTER, "http://thetvdb.com/banners/posters/311790-2.jpg"));

        File fanarDir = new File("/home/seans/git/sagetv-phoenix-core/target/junit/Fanart/TV/Leathal Weapon/Posters");
        fanarDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                pathname.delete();
                return false;
            }
        });
        assertEquals(1, md.getFanart().size());
        assertTrue(fanarDir.listFiles()==null || fanarDir.listFiles().length==0);

        FanartStorage storage = new FanartStorage();
        storage.saveFanart(mf, md.getMediaTitle(), md, null, config.getFanartCentralFolder());

        System.out.println("Sleeping...");
        Thread.sleep(5*1000);
        System.out.println("Done Sleeping...");

        assertEquals("Fanart Did not Download", 2, fanarDir.listFiles().length);
    }

}
