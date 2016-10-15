package sagex.phoenix.vfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.*;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import sagex.phoenix.util.BaseBuilder;
import sagex.phoenix.vfs.builder.VFSBuilder;
import sagex.phoenix.vfs.views.ViewFactory;
import test.InitPhoenix;

public class VFSOrganizerTest {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testOrganizing() throws Exception {
        VFSOrganizer o = new VFSOrganizer(Phoenix.getInstance().getVFSDir());
        System.out.println("VFSDIR: " + Phoenix.getInstance().getVFSDir().getAbsolutePath());
        o.organize(getReader("x-vfs.xml"), "x-vfs-1.xml");

        assertEquals(27, o.filters.size());
        assertEquals(6, o.filterGroups.size());
        assertEquals(19, o.tags.size());
        assertEquals(11, o.sorts.size());
        assertEquals(14, o.groups.size());
        assertEquals(10, o.sources.size());
        assertEquals(54, o.views.size());

        // when we organize the second time, with the same file, then nothing
        // should change
        o.organize(getReader("x-vfs.xml"), "x-vfs-2.xml");
        assertEquals(27, o.filters.size());
        assertEquals(6, o.filterGroups.size());
        assertEquals(19, o.tags.size());
        assertEquals(11, o.sorts.size());
        assertEquals(14, o.groups.size());
        assertEquals(10, o.sources.size());
        assertEquals(54, o.views.size());

        // now load another vfs and see if it organized it correctly
        o.organize(getReader("new-tags.xml"), "newtags.xml");
        assertEquals(27, o.filters.size());
        assertEquals(6, o.filterGroups.size());
        assertEquals(20, o.tags.size());
        assertEquals(11, o.sorts.size());
        assertEquals(14, o.groups.size());
        assertEquals(10, o.sources.size());
        assertEquals(54, o.views.size());

        // if the tv tag got updated
        assertEquals("TV Tag", o.tags.get("tv").attributeValue("label"));

        o.writeTo(new OutputStreamWriter(System.out));
    }

    @Test
    public void testVFSWithInvalidEntriesShouldStillLoad() throws Exception {
        VFSOrganizer o = new VFSOrganizer(Phoenix.getInstance().getVFSDir());
        System.out.println("VFSDIR: " + Phoenix.getInstance().getVFSDir().getAbsolutePath());
        o.organize(getReader("x-vfs-somebad.xml"), "x-vfs-somebad-1.xml");

        assertEquals(27, o.filters.size());
        assertEquals(6, o.filterGroups.size());
        assertEquals(19, o.tags.size());
        assertEquals(11, o.sorts.size());
        assertEquals(14, o.groups.size());
        assertEquals(11, o.sources.size()); // one of these is invalid
        assertEquals(55, o.views.size()); // one of these is invalid

        // now load the VFSFile
        BaseBuilder.failOnError=false;
        VFSManager mgr = new VFSManager(Phoenix.getInstance().getVFSDir(), Phoenix.getInstance().getVFSDir());
        VFSBuilder builder = new VFSBuilder(mgr);
        VFSBuilder.registerVFSSources(getInputStream("x-vfs-somebad.xml"), Phoenix.getInstance().getVFSDir(), mgr);
        assertEquals(55, mgr.getVFSViewFactory().getFactories(true).size());
        assertEquals(10, mgr.getVFSSourceFactory().getFactories(true).size());

        ViewFactory viewFactory = mgr.getVFSViewFactory().getFactory("playlists_bad_view");
        assertNotNull(viewFactory);
        assertTrue(viewFactory.hasErrors());
        System.out.println(viewFactory.getErrorMessage());
    }

    private InputStream getInputStream(String resFile) {
        return this.getClass().getResourceAsStream(resFile);
    }
    private Reader getReader(String resFile) {
        return new InputStreamReader(getInputStream(resFile));
    }


}
