package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualMediaFile;
import sagex.phoenix.vfs.custom.CustomFolder;
import sagex.phoenix.vfs.custom.CustomFolders;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;

public class TestCustomFolders {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void TestFolders() {
        SimpleStubAPI api = new SimpleStubAPI();
        SageAPI.setProvider(api);

        CustomFolders folders = new CustomFolders();
        assertEquals(0, folders.getChildren().size());

        CustomFolder f = folders.newCustomFolder("myfolder", "My Custom Folder");
        assertEquals(1, folders.getChildren().size());
        for (IMediaResource r : folders.getChildren()) {
            System.out.println("ID: " + r.getId());
        }

        folders = new CustomFolders();
        assertEquals(1, folders.getChildren().size());
        for (IMediaResource r : folders.getChildren()) {
            System.out.println("TITLE: " + r.getTitle());
        }

        f = folders.newCustomFolder("myfolder2", "My Custom Folder2");
        assertEquals(2, folders.getChildren().size());
        for (IMediaResource r : folders.getChildren()) {
            System.out.println("ID: " + r.getId());
        }

        folders = new CustomFolders();
        assertEquals(2, folders.getChildren().size());
        for (IMediaResource r : folders.getChildren()) {
            System.out.println("TITLE: " + r.getTitle());
        }

        // adding same folder does not create a new record
        f = folders.newCustomFolder("myfolder2", "My Custom Folder2");
        assertEquals(2, folders.getChildren().size());
        for (IMediaResource r : folders.getChildren()) {
            System.out.println("ID: " + r.getId());
        }

        // now let's add some children
        VirtualMediaFile vmf = new VirtualMediaFile("My Custom File");
        vmf.getMetadata().setMediaType("TV");
        vmf.getMetadata().setDescription("My Description");
        f.addMediaResource(vmf);
        for (IMediaResource r : f) {
            System.out.println("CHILD: " + f.getId());
        }
        assertEquals(1, f.getChildren().size());
        IMediaFile r1 = (IMediaFile) f.getChildren().get(0);
        assertEquals("My Custom File", r1.getTitle());
        assertEquals("TV", r1.getMetadata().getMediaType());
        assertEquals("My Description", r1.getMetadata().getDescription());
        assertTrue(!r1.isType(MediaResourceType.ONLINE.value()));

        // test an online video
        vmf = new VirtualMediaFile(null, "http://www.youtube.com/watch?v=Ogtp4SzTCGs&feature=grec_index",
                "http://www.youtube.com/watch?v=Ogtp4SzTCGs&feature=grec_index", "Phoenix is Rising");
        vmf.getMetadata().setMediaType("Movie");
        vmf.getMetadata().setDescription("Phoenix is Rising Teaser");
        f.addMediaResource(vmf);
        for (IMediaResource r : f) {
            System.out.println("CHILD: " + f.getId());
        }
        assertEquals(2, f.getChildren().size());
        r1 = (IMediaFile) f.getChildren().get(1);
        assertEquals("Phoenix is Rising", r1.getTitle());
        assertEquals("Movie", r1.getMetadata().getMediaType());
        assertEquals("Phoenix is Rising Teaser", r1.getMetadata().getDescription());
        assertTrue(r1.isType(MediaResourceType.ONLINE.value()));

    }
}
