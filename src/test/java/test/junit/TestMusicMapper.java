package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.music.MusicMapperFactory;
import sagex.phoenix.vfs.ov.XmlFile;
import sagex.phoenix.vfs.ov.XmlFolder;
import sagex.phoenix.vfs.ov.XmlOptions;
import sagex.phoenix.vfs.ov.XmlSourceFactory;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;
import sagex.phoenix.vfs.visitors.DebugVisitor;
import test.InitPhoenix;
import test.junit.lib.StubViewFactory;

public class TestMusicMapper {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testMusicMaper() {
        String url = InitPhoenix.ProjectHome("src/test/java/test/junit/hot-100.xml").toURI().toString();

        XmlSourceFactory factory = new XmlSourceFactory();
        factory.setOptionValue("feedurl", url);
        factory.setOptionValue("MediaType", "Music");
        factory.setOptionValue("EpisodeName-element", "title");
        factory.setOptionValue("EpisodeName-regex", "[0-9]+:\\s([^,]+),\\s*(.*)");

        XmlFolder offline = (XmlFolder) factory.create(null);

        assertEquals(100, offline.getChildren().size());

        // create the offline view and register it
        StubViewFactory.registerView("hot100-offline", offline).setOptionValue(XmlOptions.WAIT_FOR_CHILDREN, "true");
        ViewFolder offlineView = phoenix.umb.CreateView("hot100-offline");
        assertNotNull("failed to find hot100 view??", offlineView);
        DebugVisitor.dump(offlineView, System.out);
        assertEquals(100, offlineView.getChildren().size());

        // create the music library view and register it
        VirtualMediaFolder library = new VirtualMediaFolder("Music");
        library.addMediaResource(XmlFile.newMusicFile(library, "Rihanna Featuring Calvin Harris", "We Found Love"));
        library.addMediaResource(XmlFile.newMusicFile(library, "Maroon 5 Featuring Christina Aguilera", "Moves Like Jagger"));
        library.addMediaResource(XmlFile.newMusicFile(library, "Adele", "Someone Like You"));
        library.addMediaResource(XmlFile.newMusicFile(library, "David Guetta Featuring Usher", "Without You"));
        library.addMediaResource(XmlFile.newMusicFile(library, "Katy Perry", "The One That Got Away"));
        library.addMediaResource(XmlFile.newMusicFile(library, "Brad Paisley", "Camouflage"));
        StubViewFactory.registerView("music", library);
        ViewFolder libraryView = phoenix.umb.CreateView("music");
        assertNotNull("failed to find music library view??", libraryView);
        assertEquals(6, libraryView.getChildren().size());

        // now create the Mapped Factory that builds the actual view
        MusicMapperFactory mapper = new MusicMapperFactory();
        mapper.setName("hot100");
        mapper.getOption("offline-view").value().set("hot100-offline");
        mapper.getOption("library-view").value().set("music");
        mapper.getOption("hide-offline").value().set("true");
        IMediaFolder music = mapper.create(null);
        assertNotNull(music);
        assertEquals(6, music.getChildren().size());
        IMediaFolder music1 = mapper.create(null);
        assertTrue("Did not return a cached view result", music == music1);

        // Second view, but this time include offline files as well
        MusicMapperFactory mapper2 = new MusicMapperFactory();
        mapper2.setName("hot100");
        mapper2.getOption("offline-view").value().set("hot100-offline");
        mapper2.getOption("library-view").value().set("music");
        mapper2.getOption("hide-offline").value().set("false");
        IMediaFolder music2 = mapper2.create(null);
        assertNotNull(music2);
        assertEquals(100, music2.getChildren().size());

    }
}
