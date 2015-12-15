package test.junit;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.ov.XmlFile;
import sagex.phoenix.vfs.sources.SageOnlineVideosFactory;
import test.InitPhoenix;

public class TestOnlineVideos {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testOnlineVideos() {
        if (true) {
            System.out.println("** TODO: IMPLEMENT ONLINE VIDEOS TEST **");
            // return;
        }

        SageOnlineVideosFactory factory = new SageOnlineVideosFactory();
        Set<ConfigurableOption> opts = new TreeSet<ConfigurableOption>();
        opts.add(new ConfigurableOption("videodir", new File("../../src/test/java/test/junit/OnlineVideos").getPath()));

        IMediaFolder folder = factory.create(opts);

        // DebugFolderWalker debug = new DebugFolderWalker(true);
        // String s = debug.toString(folder);
        // System.out.println(s);
        for (IMediaResource r : folder) {
            System.out.println("OnlineVideos: " + r.getTitle() + "; ");
        }

        IMediaFolder comedy = (IMediaFolder) folder.getChild("Comedy");
        IMediaFolder dilbert = (IMediaFolder) comedy.getChild("Dilbert Animated Cartoons");
        System.out.println("Dilbert: " + dilbert.getTitle() + "; " + dilbert.getClass().getName());
        for (IMediaResource r : dilbert) {
            XmlFile rmf = (XmlFile) r;
            System.out.println("Item: " + rmf.getTitle() + "; " + rmf.getMetadata().getMediaUrl() + "; " + rmf.getId());
        }
    }
}
