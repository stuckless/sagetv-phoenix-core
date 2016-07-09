package test.junit;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.junit.lib.Utils.makeDir;
import static test.junit.lib.Utils.makeFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.easymock.IAnswer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import phoenix.impl.ConfigurableOptionsAPI;
import phoenix.impl.MediaBrowserAPI;
import phoenix.impl.ViewAPI;
import sagex.ISageAPIProvider;
import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.factory.ConfigurableOption.ListValue;
import sagex.phoenix.factory.FactoryRegistry;
import sagex.phoenix.util.Hints;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;
import sagex.phoenix.vfs.VFSOrganizer;
import sagex.phoenix.vfs.VirtualMediaFile;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.builder.VFSBuilder;
import sagex.phoenix.vfs.filters.FilePathFilter;
import sagex.phoenix.vfs.filters.Filter;
import sagex.phoenix.vfs.filters.FilterFactory;
import sagex.phoenix.vfs.filters.GenresFilter;
import sagex.phoenix.vfs.filters.HomeVideosFilter;
import sagex.phoenix.vfs.filters.MetadataFieldFilter;
import sagex.phoenix.vfs.filters.TitleFilter;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.groups.GroupingFactory;
import sagex.phoenix.vfs.groups.IGrouper;
import sagex.phoenix.vfs.groups.MetadataFieldGrouper;
import sagex.phoenix.vfs.groups.SeasonGrouper;
import sagex.phoenix.vfs.impl.FileMediaFile;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import sagex.phoenix.vfs.sorters.JavascriptComparator;
import sagex.phoenix.vfs.sorters.MetadataFieldSorter;
import sagex.phoenix.vfs.sorters.Sorter;
import sagex.phoenix.vfs.sorters.TitleSorter;
import sagex.phoenix.vfs.sources.MediaFolderSourceFactory;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;
import sagex.phoenix.vfs.views.ViewItem;
import sagex.phoenix.vfs.views.ViewPresentation;
import sagex.phoenix.vfs.visitors.PrintTreeVisitor;
import test.InitPhoenix;
import test.SimpleSTDOUTVisitor;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

public class TestMediaBrowser {

    public TestMediaBrowser() {
    }

    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    private VirtualMediaFile newMF(String title) {
        VirtualMediaFile mf = new VirtualMediaFile(title);
        return mf;
    }

    @Test
    public void testMediaFolderFilters() {
        Filter filter = new Filter() {
            public boolean canAccept(IMediaResource res) {
                return res.getTitle().equals(getValue());
            }
        };
        // need to set the value option, since we are dynamically creating an
        // unregistered filter.
        filter.setLabel("My Filter");
        filter.addOption(new ConfigurableOption(Filter.OPT_VALUE));
        filter.setValue("Test");
        assertEquals("Test", filter.getOption(Filter.OPT_VALUE).value().get());

        VirtualMediaFolder folder = new VirtualMediaFolder(null, "Sample Folder");
        folder.addMediaResource(newMF("Test"));
        folder.addMediaResource(newMF("Test1"));
        folder.addMediaResource(newMF("Test2"));
        folder.addMediaResource(newMF("Test3"));

        ViewFolder view = new ViewFolder(null, 0, null, folder);
        view.getTags().add("video");
        assertEquals("Incorrect Child Count", 4, view.getChildren().size());

        // get all filters, and dump them.
        Set<FilterFactory> filters = Phoenix.getInstance().getVFSManager().getVFSFilterFactory().getFactories(view.getTags());
        assertTrue(filters.size() > 0);
        for (FilterFactory f : filters) {
            System.out.println("Available Filter: " + f.getLabel());
        }

        view.setFilter(filter);
        assertEquals(1, view.getFilters().size());
        assertEquals("Incorrect Child Count", 1, view.getChildren().size());

        view.removeFilter(filter);
        assertEquals(0, view.getFilters().size());
        assertEquals("Incorrect Child Count", 4, view.getChildren().size());

        view.setFilter(filter);
        assertEquals(1, view.getFilters().size());
        assertEquals("Incorrect Child Count", 1, view.getChildren().size());

        filter.getOption(Filter.OPT_SCOPE).value().setValue("exclude");
        filter.setChanged(true);
        view.refresh();
        assertEquals(1, view.getFilters().size());
        assertEquals("Incorrect Child Count", 3, view.getChildren().size());

        filter.getOption(Filter.OPT_SCOPE).value().setValue("include");
        filter.setChanged(true);
        view.refresh();
        assertEquals(1, view.getFilters().size());
        assertEquals("Incorrect Child Count", 1, view.getChildren().size());
    }

    @Test
    public void testGroups() {
        VirtualMediaFolder folder = new VirtualMediaFolder("TV");
        VirtualMediaFile f = null;
        f = new VirtualMediaFile(folder, "Futurama-S1E1", "Futurama-S1E1", "Futurama");
        f.getMetadata().setRelativePathWithTitle("Futurama");
        f.getMetadata().setSeasonNumber(1);
        f.getMetadata().setEpisodeNumber(1);
        f.getMetadata().setEpisodeName("Episode 1");
        folder.getChildren().add(f);

        // grouping should group basedon the letters and numbers only, and
        // ignore case,
        // so prove that here
        f = new VirtualMediaFile(folder, "Futurama-S1E2", "Futurama-S1E2", "futurama");
        f.getMetadata().setRelativePathWithTitle("Futurama");
        f.getMetadata().setSeasonNumber(1);
        f.getMetadata().setEpisodeNumber(2);
        f.getMetadata().setEpisodeName("Episode 2");
        folder.getChildren().add(f);

        f = new VirtualMediaFile(folder, "Futurama-S1E3", "Futurama-S1E3", "Futurama!");
        f.getMetadata().setRelativePathWithTitle("Futurama");
        f.getMetadata().setSeasonNumber(1);
        f.getMetadata().setEpisodeNumber(3);
        f.getMetadata().setEpisodeName("Episode 3");
        folder.getChildren().add(f);

        f = new VirtualMediaFile(folder, "Futurama-S2E1", "Futurama-S2E1", "Futurama");
        f.getMetadata().setRelativePathWithTitle("Futurama");
        f.getMetadata().setSeasonNumber(2);
        f.getMetadata().setEpisodeNumber(1);
        f.getMetadata().setEpisodeName("Episode 1");
        folder.getChildren().add(f);

        f = new VirtualMediaFile(folder, "House-S1E1", "House-S1E1", "House");
        f.getMetadata().setRelativePathWithTitle("House");
        f.getMetadata().setSeasonNumber(1);
        f.getMetadata().setEpisodeNumber(1);
        f.getMetadata().setEpisodeName("Episode 1");
        folder.getChildren().add(f);

        assertEquals(5, folder.getChildren().size());
        Loggers.VFS_LOG.setLevel(Level.DEBUG);

        IGrouper gpr = new IGrouper() {
            @Override
            public String getGroupName(IMediaResource res) {
                if (res instanceof IMediaFile) {
                    System.out.println("RES: " + res);
                    String g = ((IMediaFile) res).getMetadata().getRelativePathWithTitle();
                    System.out.println("GROUP: " + g);
                    return g;
                } else {
                    return null;
                }
            }
        };

        ViewFactory fact = new ViewFactory();
        ViewPresentation vp = new ViewPresentation();
        vp.getGroupers().add(new Grouper(gpr));
        vp.setLevel(0);
        fact.addViewPresentations(vp);
        vp = new ViewPresentation();
        vp.getGroupers().add(new Grouper(new SeasonGrouper()));
        vp.setLevel(1);
        fact.addViewPresentations(vp);
        fact.addViewPresentations(new ViewPresentation(2));

        assertTrue(fact.hasViewPresentation(0));
        assertTrue(fact.hasViewPresentation(1));
        assertTrue(fact.hasViewPresentation(2));

        ViewFolder view = new ViewFolder(fact, 0, null, folder);
        view.accept(new SimpleSTDOUTVisitor(), null, IMediaResource.DEEP_UNLIMITED);
        assertEquals(1, view.getGroupers().size());
        assertEquals(2, view.getChildren().size());

        Iterator<IMediaResource> iter = view.getChildren().iterator();
        IMediaResource res = iter.next();
        assertEquals("Futurama", res.getTitle());
        System.out.println("--- " + res.getClass().getName());
        assertTrue("Not a ViewFolder!", res instanceof ViewFolder);

        res = iter.next();
        assertEquals("House", res.getTitle());
        assertTrue("Not a ViewFolder: ", res instanceof ViewFolder);

        iter = view.getChildren().iterator();
        res = iter.next();
        assertEquals("Futurama", res.getTitle());
        assertTrue("Not a Folder!", res instanceof ViewFolder);

        // check that we have 2 child items in Futurama for Season 1 and Season
        // 2
        ViewFolder futuramaItems = (ViewFolder) res;
        System.out.println("*** Factory: " + futuramaItems.getViewFactory().getViewPresentations().size());
        System.out.println("*** Groupers: " + futuramaItems.getViewFactory().getViewPresentation(1).getGroupers().size());
        System.out.println("*** Groupers: " + futuramaItems.getGroupers().size());
        System.out.println("*** Level: " + futuramaItems.getPresentation().getLevel());
        if (futuramaItems.getChildren().size() != 2) {
            System.out.println("*** Invalid Grouping ****");
            dumpResources(futuramaItems.getChildren());
            fail("Folder has too many/too few items: " + futuramaItems.getChildren().size());
        }

        // first items in futuram should be folder with 3 items
        Iterator<IMediaResource> futureIter = futuramaItems.getChildren().iterator();
        IMediaFolder season1 = (IMediaFolder) futureIter.next();
        assertEquals("Season 01", season1.getTitle());
        if (season1.getChildren().size() != 3) {
            dumpResources(season1.getChildren());
            fail("Should be 3 children");
        }

        IMediaFolder singleItem = (IMediaFolder) futureIter.next();
        assertEquals("Season 02", singleItem.getTitle());
        IMediaFile file = (IMediaFile) singleItem.getChildren().get(0);
        assertEquals(2, file.getMetadata().getSeasonNumber());
        assertEquals(1, file.getMetadata().getEpisodeNumber());

        res = iter.next();
        assertEquals("House", res.getTitle());
        assertTrue("Not a Folder!", res instanceof ViewFolder);

        Grouper grouper = view.getGroupers().get(0);
        view.removeGrouper(grouper);
        view.refresh();
        assertEquals(5, view.getChildren().size());

        view.setGrouper(grouper);
        assertEquals(2, view.getChildren().size());
    }

    @Test
    public void testGroupsSingleLevel() {
        VirtualMediaFolder folder = new VirtualMediaFolder("TV");
        VirtualMediaFile f = null;
        f = new VirtualMediaFile(folder, "Futurama-S1E1", "Futurama-S1E1", "Futurama");
        f.getMetadata().setRelativePathWithTitle("Futurama");
        f.getMetadata().setSeasonNumber(1);
        f.getMetadata().setEpisodeNumber(1);
        f.getMetadata().setEpisodeName("Episode 1");
        folder.getChildren().add(f);

        f = new VirtualMediaFile(folder, "Futurama-S1E2", "Futurama-S1E2", "Futurama");
        f.getMetadata().setRelativePathWithTitle("Futurama");
        f.getMetadata().setSeasonNumber(1);
        f.getMetadata().setEpisodeNumber(2);
        f.getMetadata().setEpisodeName("Episode 2");
        folder.getChildren().add(f);

        f = new VirtualMediaFile(folder, "Futurama-S1E3", "Futurama-S1E3", "Futurama");
        f.getMetadata().setRelativePathWithTitle("Futurama");
        f.getMetadata().setSeasonNumber(1);
        f.getMetadata().setEpisodeNumber(3);
        f.getMetadata().setEpisodeName("Episode 3");
        folder.getChildren().add(f);

        f = new VirtualMediaFile(folder, "Futurama-S2E1", "Futurama-S2E1", "Futurama");
        f.getMetadata().setRelativePathWithTitle("Futurama");
        f.getMetadata().setSeasonNumber(2);
        f.getMetadata().setEpisodeNumber(1);
        f.getMetadata().setEpisodeName("Episode 1");
        folder.getChildren().add(f);

        f = new VirtualMediaFile(folder, "House-S1E1", "House-S1E1", "House");
        f.getMetadata().setRelativePathWithTitle("House");
        f.getMetadata().setSeasonNumber(1);
        f.getMetadata().setEpisodeNumber(1);
        f.getMetadata().setEpisodeName("Episode 1");
        folder.getChildren().add(f);

        assertEquals(5, folder.getChildren().size());
        Loggers.VFS_LOG.setLevel(Level.DEBUG);

        IGrouper gpr = new IGrouper() {
            @Override
            public String getGroupName(IMediaResource res) {
                if (res instanceof IMediaFile) {
                    String g = ((IMediaFile) res).getMetadata().getRelativePathWithTitle();
                    return g;
                } else {
                    return null;
                }
            }
        };

        ViewFactory fact = new ViewFactory();
        ViewPresentation vp = new ViewPresentation();
        vp.getGroupers().add(new Grouper(gpr));
        vp.setLevel(0);
        fact.addViewPresentations(vp);

        assertTrue(fact.hasViewPresentation(0));

        ViewFolder view = new ViewFolder(fact, 0, null, folder);
        // view.accept(new SimpleSTDOUTVisitor(), null,
        // IMediaResource.DEEP_UNLIMITED);
        assertEquals(1, view.getGroupers().size());
        assertEquals(2, view.getChildren().size());

        Iterator<IMediaResource> iter = view.getChildren().iterator();
        IMediaResource res = iter.next();
        assertEquals("Futurama", res.getTitle());
        System.out.println("--- " + res.getClass().getName());
        assertTrue("Not a ViewFolder!", res instanceof ViewFolder);

        res = iter.next();
        assertEquals("House", res.getTitle());
        assertTrue("Not a ViewFolder: ", res instanceof ViewFolder);

        iter = view.getChildren().iterator();
        res = iter.next();
        assertEquals("Futurama", res.getTitle());
        assertTrue("Not a Folder!", res instanceof ViewFolder);

        // check that we have 2 child items in Futurama for Season 1 and Season
        // 2
        ViewFolder futuramaItems = (ViewFolder) res;
        if (futuramaItems.getChildren().size() != 4) {
            System.out.println("*** Invalid Grouping ****");
            // dumpResources(futuramaItems.getChildren());
            fail("Folder has too many/too few items: " + futuramaItems.getChildren().size());
        }
    }

    private void dumpResources(List<IMediaResource> children) {
        for (IMediaResource r : children) {
            System.out.println("Item: " + r.getTitle() + "; Folder: " + (r instanceof IMediaFolder));
        }
    }

    @Test
    public void testSorters() {
        VirtualMediaFolder folder = new VirtualMediaFolder("Folder");
        VirtualMediaFile f = null;
        f = new VirtualMediaFile(folder, "B", "B", "B");
        folder.getChildren().add(f);

        folder.getChildren().add(new VirtualMediaFolder(folder, "Sub Folder B", null, "Sub Folder B"));

        f = new VirtualMediaFile(folder, "C", "C", "C");
        folder.getChildren().add(f);

        folder.getChildren().add(new VirtualMediaFolder(folder, "Sub Folder A", null, "Sub Folder A"));
        VirtualMediaFolder subc = new VirtualMediaFolder(folder, "Sub Folder C", null, "Sub Folder C");
        f = new VirtualMediaFile(subc, "item2", "item2", "item2");
        subc.addMediaResource(f);
        f = new VirtualMediaFile(subc, "item1", "item1", "item1");
        subc.addMediaResource(f);
        f = new VirtualMediaFile(subc, "item3", "item3", "item3");
        subc.addMediaResource(f);
        folder.getChildren().add(subc);

        f = new VirtualMediaFile(folder, "A", "A", "A");
        folder.getChildren().add(f);

        System.out.println("===== All Files ====");
        for (IMediaResource r : folder) {
            System.out.println("Title: " + r.getTitle());
        }
        assertEquals(6, folder.getChildren().size());

        // grab simple title sorter
        Sorter sorter = Phoenix.getInstance().getVFSManager().getVFSSortFactory().getFactory("title").create(null);

        ViewFolder view = new ViewFolder(null, 0, null, folder);
        view.setSorter(sorter);
        assertEquals(1, view.getSorters().size());
        view.refresh();
        assertEquals(6, view.getChildren().size());

        System.out.println("===== Sorted Files ====");
        for (IMediaResource r : view) {
            System.out.println("Title: " + r.getTitle());
        }

        Iterator<IMediaResource> iter = view.getChildren().iterator();
        assertEquals("Sub Folder A", iter.next().getTitle());
        assertEquals("Sub Folder B", iter.next().getTitle());
        assertEquals("Sub Folder C", iter.next().getTitle());
        assertEquals("A", iter.next().getTitle());
        assertEquals("B", iter.next().getTitle());
        assertEquals("C", iter.next().getTitle());

        IMediaFolder r = (IMediaFolder) view.getChildren().get(2);
        assertEquals("Sub Folder C", r.getTitle());
        assertTrue("ViewFolder folders must be a ViewFolder", r instanceof ViewFolder);
        iter = r.getChildren().iterator();
        assertEquals("item1", iter.next().getTitle());
        assertEquals("item2", iter.next().getTitle());
        assertEquals("item3", iter.next().getTitle());

        view.removeSorter(sorter);
        assertEquals(0, view.getSorters().size());
        view.refresh();
        // can't really test for unsorted behaviour

        view.setSorter(sorter);
        view.refresh();
        iter = view.getChildren().iterator();
        assertEquals("Sub Folder A", iter.next().getTitle());
        assertEquals("Sub Folder B", iter.next().getTitle());
        assertEquals("Sub Folder C", iter.next().getTitle());
        assertEquals("A", iter.next().getTitle());
        assertEquals("B", iter.next().getTitle());
        assertEquals("C", iter.next().getTitle());

        sorter.getOption(Sorter.OPT_FOLDERS_FIRST).value().setValue("false");
        sorter.setChanged(true);
        view.refresh();
        iter = view.getChildren().iterator();
        assertEquals("A", iter.next().getTitle());
        assertEquals("B", iter.next().getTitle());
        assertEquals("C", iter.next().getTitle());
        assertEquals("Sub Folder A", iter.next().getTitle());
        assertEquals("Sub Folder B", iter.next().getTitle());
        assertEquals("Sub Folder C", iter.next().getTitle());

        sorter.getOption(Sorter.OPT_SORT_ORDER).value().setValue(Sorter.SORT_DESC);
        sorter.setChanged(true);
        view.refresh();
        iter = view.getChildren().iterator();
        assertEquals("Sub Folder C", iter.next().getTitle());
        assertEquals("Sub Folder B", iter.next().getTitle());
        assertEquals("Sub Folder A", iter.next().getTitle());
        assertEquals("C", iter.next().getTitle());
        assertEquals("B", iter.next().getTitle());
        assertEquals("A", iter.next().getTitle());

        sorter.getOption(Sorter.OPT_FOLDERS_FIRST).value().setValue("true");
        sorter.setChanged(true);
        view.refresh();
        iter = view.getChildren().iterator();
        assertEquals("Sub Folder C", iter.next().getTitle());
        assertEquals("Sub Folder B", iter.next().getTitle());
        assertEquals("Sub Folder A", iter.next().getTitle());
        assertEquals("C", iter.next().getTitle());
        assertEquals("B", iter.next().getTitle());
        assertEquals("A", iter.next().getTitle());

    }

    @Test
    public void testSortersJustDirs() {
        VirtualMediaFolder folder = new VirtualMediaFolder("Folder");

        folder.getChildren().add(new VirtualMediaFolder(folder, "videos"));
        folder.getChildren().add(new VirtualMediaFolder(folder, "pictures"));
        folder.getChildren().add(new VirtualMediaFolder(folder, "music"));
        folder.getChildren().add(new VirtualMediaFolder(folder, "New"));
        folder.getChildren().add(new VirtualMediaFolder(folder, "SampleDir"));
        folder.getChildren().add(new VirtualMediaFolder(folder, "LOST"));
        folder.getChildren().add(new VirtualMediaFolder(folder, "TmpTV"));

        System.out.println("===== All Dirs ====");
        for (IMediaResource r : folder) {
            System.out.println("Title: " + r.getTitle());
        }
        assertEquals(7, folder.getChildren().size());

        ViewFolder view = new ViewFolder(null, 0, null, folder);
        Sorter sorter = Phoenix.getInstance().getVFSManager().getVFSSortFactory().getFactory("title").create(null);
        view.setSorter(sorter);
        view.refresh();
        assertEquals(7, view.getChildren().size());

        System.out.println("===== Sorted Files ====");
        for (IMediaResource r : view) {
            System.out.println("Title: " + r.getTitle());
        }

        Iterator<IMediaResource> iter = view.getChildren().iterator();
        assertEquals("LOST", iter.next().getTitle());
        assertEquals("music", iter.next().getTitle());
        assertEquals("New", iter.next().getTitle());
        assertEquals("pictures", iter.next().getTitle());
        assertEquals("SampleDir", iter.next().getTitle());
        assertEquals("TmpTV", iter.next().getTitle());
        assertEquals("videos", iter.next().getTitle());

        view.removeSorter(sorter);
        view.refresh();
        iter = view.getChildren().iterator();
        assertEquals("videos", iter.next().getTitle());
        assertEquals("pictures", iter.next().getTitle());
        assertEquals("music", iter.next().getTitle());
        assertEquals("New", iter.next().getTitle());
        assertEquals("SampleDir", iter.next().getTitle());
        assertEquals("LOST", iter.next().getTitle());
        assertEquals("TmpTV", iter.next().getTitle());

        view.setSorter(sorter);
        sorter.getOption(Sorter.OPT_SORT_ORDER).value().setValue(Sorter.SORT_DESC);
        sorter.setChanged(true);
        view.refresh();
        iter = view.getChildren().iterator();
        assertEquals("videos", iter.next().getTitle());
        assertEquals("TmpTV", iter.next().getTitle());
        assertEquals("SampleDir", iter.next().getTitle());
        assertEquals("pictures", iter.next().getTitle());
        assertEquals("New", iter.next().getTitle());
        assertEquals("music", iter.next().getTitle());
        assertEquals("LOST", iter.next().getTitle());
    }

    @Test
    public void testCombinedView() throws Exception {
        final List imports = new ArrayList();
        imports.add(makeDir("import1"));
        imports.add(makeDir("import2"));
        imports.add(makeDir("import3"));

        final List files = new ArrayList();
        files.add(makeFile("import1/DVDs/Series1/S1E1.avi", true));
        files.add(makeFile("import2/DVDs/Series1/S1E2.avi", true));
        files.add(makeFile("import3/DVDs/Series2/S1E1.avi", true));
        files.add(makeFile("import2/Shows/ShowA.avi", true));

        ISageAPIProvider prov = createNiceMock(ISageAPIProvider.class);
        expect(prov.callService("GetVideoLibraryImportPaths", null)).andAnswer(new IAnswer<Object[]>() {
            public Object[] answer() throws Throwable {
                System.out.println("*** Returning Library Import Paths ****");
                return imports.toArray(new File[]{});
            }
        }).anyTimes();

        expect(prov.callService(eq("GetFileForSegment"), (Object[]) anyObject())).andAnswer(new IAnswer<File>() {
            public File answer() throws Throwable {
                // Mental Note about IAnswer
                // getCurrentArguments() return the arguments that are passed in
                // as an array
                // getCurrentArguments()[0] is the String sage command
                // getCurrentArguments()[1] is the Object[] of args that are
                // passed to the Sage Command
                File f = (File) (((Object[]) getCurrentArguments()[1])[0]);
                return f;
            }
        }).anyTimes();

        expect(prov.callService(eq("GetMediaFiles"), aryEq(new Object[]{"VL"}))).andAnswer(new IAnswer<Object[]>() {
            public Object[] answer() throws Throwable {
                return files.toArray(new File[]{});
            }
        }).anyTimes();

        expect(prov.callService(eq("GetShowTitle"), (Object[]) anyObject())).andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                return ((File) ((Object[]) getCurrentArguments()[1])[0]).getName();
            }
        }).anyTimes();

        expect(prov.callService(eq("GetMediaFileForFilePath"), (Object[]) anyObject())).andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                File file = (File) ((Object[]) getCurrentArguments()[1])[0];
                return file;
            }
        }).anyTimes();

        replay(prov);
        SageAPI.setProvider(prov);

        Loggers.VFS_LOG.setLevel(Level.DEBUG);
        IMediaFolder folder = phoenix.api.GetSageSourcesMediaFolder(true, "VL");
        System.out.println("\n\n*** Dumping Combined View: " + folder.getClass().getName());
        IMediaResourceVisitor vis = new SimpleSTDOUTVisitor();
        // DebugFolderWalker.walk(folder, true, vis);
        folder.accept(vis, null, 0);
        assertEquals("Combined did not work!", 2, folder.getChildren().size());
    }

    @Test
    public void testJavascriptSorter() throws ScriptException {
        VirtualMediaFile mf1 = new VirtualMediaFile("1");
        VirtualMediaFile mf2 = new VirtualMediaFile("2");

        String sorter = "function compare(o1,o2) {return o1.getTitle().compareTo(o2.getTitle());}";
        System.out.println("MT1: " + phoenix.media.GetTitle(mf1));
        System.out.println("MT2: " + phoenix.media.GetTitle(mf2));
        JavascriptComparator comparator = new JavascriptComparator(sorter);
        assertTrue(comparator.compare(mf1, mf2) != 0);
    }

    @Test
    public void testVisitors() {
        VirtualMediaFolder p = new VirtualMediaFolder("Parent");
        p.addMediaResource(new VirtualMediaFile("Child1"));
        p.addMediaResource(new VirtualMediaFile("Child2"));
        p.addMediaResource(new VirtualMediaFile("Child3"));

        VirtualMediaFolder p2 = new VirtualMediaFolder("ChildFolder");
        p2.addMediaResource(new VirtualMediaFile("Child4"));
        p2.addMediaResource(new VirtualMediaFile("Child5"));
        p2.addMediaResource(new VirtualMediaFile("Child6"));

        p.addMediaResource(p2);

        CountVisitor vis = new CountVisitor();
        vis.reset();
        p.accept(vis, null, IMediaResource.DEEP_UNLIMITED);
        assertEquals("Did not visit all folders", 2, vis.folders);
        assertEquals("Did not visit all files", 6, vis.files);
        assertEquals("Incorrect Folder Order", "Parent", vis.folderList.get(0).getTitle());
        assertEquals("Incorrect Folder Order", "ChildFolder", vis.folderList.get(1).getTitle());
        assertEquals("Incorrect Folder Order", "Child1", vis.fileList.get(0).getTitle());
        assertEquals("Incorrect Folder Order", "Child6", vis.fileList.get(5).getTitle());

        vis.reset();
        p.accept(vis, null, 0);
        assertEquals("Did not visit all folders", 1, vis.folders);
        assertEquals("Did not visit all files", 0, vis.files);

        vis.reset();
        p.accept(vis, null, 1);
        assertEquals("Did not visit all folders", 2, vis.folders);
        assertEquals("Did not visit all files", 3, vis.files);
        assertEquals("Incorrect Folder Order", "Parent", vis.folderList.get(0).getTitle());
        assertEquals("Incorrect Folder Order", "ChildFolder", vis.folderList.get(1).getTitle());
    }

    @Test
    public void testVirtuals() {
        VirtualMediaFile mf = new VirtualMediaFile("Test");
        assertTrue("ID is null", mf.getId() != null);
        assertTrue("Title is null", mf.getTitle() != null);
        assertEquals("Incorrect Title", "Test", mf.getTitle());
        assertTrue("File is null", mf.getFiles() != null);
        assertTrue("AlbumInfo is null", mf.getAlbumInfo() != null);
        assertTrue("Metadata is null", mf.getMetadata() != null);
        assertTrue("MediaObject is null", mf.getMediaObject() != null);

        VirtualMediaFolder fold = new VirtualMediaFolder("TestFolder");
        assertTrue("ID is null", fold.getId() != null);
        assertTrue("Title is null", fold.getTitle() != null);
        assertEquals("Incorrect Title", "TestFolder", fold.getTitle());
        assertTrue("Children is null", fold.getChildren() != null);
        assertTrue("MediaObject is null", mf.getMediaObject() != null);
    }

    @Test
    public void testConfigurationOptionsAPI() {
        ConfigurableOption co = new ConfigurableOption("test", "label", "one", DataType.string, true, ListSelection.single,
                "one:One,two:Two,three:Three");

        List<ListValue> values = co.getListValues();
        assertEquals(3, values.size());
        assertEquals("One", values.get(0).getName());
        assertEquals("one", values.get(0).getValue());
        assertEquals("Two", values.get(1).getName());
        assertEquals("two", values.get(1).getValue());
        assertEquals("Three", values.get(2).getName());
        assertEquals("three", values.get(2).getValue());

        ConfigurableOptionsAPI api = new ConfigurableOptionsAPI();
        assertEquals("one", api.GetValue(co));
        assertEquals("label", api.GetLabel(co));
        assertTrue(api.IsList(co));
        assertTrue(api.IsToggle(co));
        assertTrue(api.IsListItemSelected(co, co.getListValues().get(0)));

        api.Toggle(co);
        assertEquals("two", api.GetValue(co));
        assertTrue(api.IsListItemSelected(co, co.getListValues().get(1)));

        api.Toggle(co);
        assertEquals("three", api.GetValue(co));
        assertTrue(api.IsListItemSelected(co, co.getListValues().get(2)));

        api.Toggle(co);
        assertEquals("one", api.GetValue(co));
        assertTrue(api.IsListItemSelected(co, co.getListValues().get(0)));
    }

    @Test
    public void testMediaBrowserAPI() throws Exception {
        // create a view and presentation
        ViewFactory viewFact = new ViewFactory();
        viewFact.getOption(viewFact.OPT_LABEL).value().set("Test");
        ViewPresentation l1 = new ViewPresentation(0);

        TitleFilter filter = new TitleFilter();
        filter.setValue("M1");
        l1.setFilter(filter);

        Sorter sorter = new Sorter(new TitleSorter());
        l1.getSorters().add(sorter);
        l1.getHints().add("test");

        ViewPresentation l2 = new ViewPresentation(1);
        l2.getSorters().add(sorter);

        viewFact.addViewPresentations(l1);
        viewFact.addViewPresentations(l2);

        // build up the factory
        VirtualMediaFolder folder = new VirtualMediaFolder("Movies");
        folder.addMediaResource(new VirtualMediaFile(folder, "M1", "M1", "M1"));
        folder.addMediaResource(new VirtualMediaFile(folder, "M3", "M3", "M3"));
        folder.addMediaResource(new VirtualMediaFile(folder, "M2", "M2", "M2"));
        viewFact.addFolderSource(new MediaFolderSourceFactory(folder));

        // factory is now configured, as if we read it from xml, so lets begin
        // testing

        MediaBrowserAPI browserAPI = new MediaBrowserAPI();
        ViewAPI viewAPI = new ViewAPI();
        ConfigurableOptionsAPI confAPI = new ConfigurableOptionsAPI();

        ViewFolder vf = (ViewFolder) viewAPI.Create(viewFact);
        assertEquals(1, browserAPI.GetChildCount(vf));

        List<Filter> filters = viewAPI.GetFilters(vf);
        assertEquals(1, filters.size());

        Filter f = filters.get(0);

        // get filter options
        // simulate rendering the Filters in the STV
        List<ConfigurableOption> opts = viewAPI.GetOptions(f);
        for (ConfigurableOption opt : opts) {
            System.out.println("Filter Option: " + confAPI.GetName(opt) + "; " + confAPI.GetLabel(opt));
            assertNotNull(confAPI.GetLabel(opt));
            assertNotNull(confAPI.GetName(opt));
        }

        // TODO: recognize that each Filter/Sorter/Grouper needs to have an
        // a default action
        // HasDefaultAction(Filter);
        // DoAction(Filter)
        //
        // TODO: recognize that we need to have a default "Title" for each
        // Filter
        // GetTitle(Filter): Returns a formatted Title, like,
        // "Include/Exclude: Include"
        // UPDATE: No need for the Action/Title apis
        // BUT, it might be good to have some Filter/Sort apis for getting
        // label of sort-mode, filter-mode, filter-value, etc

        confAPI.Toggle(opts.get(0));
        viewAPI.SetChanged(f);
        viewAPI.Refresh(vf);

        // we just toggled the filter, so this should inverse the set
        assertEquals(2, browserAPI.GetChildCount(vf));

        // filtering the options appears to work using the apis,
        // moving onto sort

        List<Sorter> sorters = viewAPI.GetSorters(vf);
        assertEquals(1, sorters.size());
        Sorter s = sorters.get(0);

        // simular rendering of sorters in the STV
        opts = viewAPI.GetOptions(s);
        for (ConfigurableOption opt : opts) {
            System.out.println("Sorter Option: " + confAPI.GetName(opt) + "; " + confAPI.GetLabel(opt));
            assertNotNull(confAPI.GetLabel(opt));
            assertNotNull(confAPI.GetName(opt));
        }

        // verify asc sort order.
        assertEquals("M2", browserAPI.GetChild(vf, 0).getTitle());
        assertEquals("M3", browserAPI.GetChild(vf, 1).getTitle());

        // toggle sort order
        confAPI.Toggle(opts.get(0));
        viewAPI.SetChanged(s);
        viewAPI.Refresh(vf);

        assertEquals("M3", browserAPI.GetChild(vf, 0).getTitle());
        assertEquals("M2", browserAPI.GetChild(vf, 1).getTitle());

        // verify hints...
        assertTrue(viewAPI.HasHint("test", vf));
        assertFalse(viewAPI.HasHint("do no exist", vf));

        // remove filter, check order, get list of new filters, add new fitler

        // clear the sorters, and now test if when we clone, that we have the
        // original sorters back
        vf.getPresentation().getSorters().clear();
        ViewFolder vf2 = (ViewFolder) viewAPI.Create(viewFact);
        assertEquals(1, vf2.getPresentation().getSorters().size());
    }

    @Test
    public void testCreateViewAPIs() {
        // create new view
        // create view view clone
        // configure new view
        // save new view
    }

    @Test
    public void testVisibility() {
        SimpleStubAPI api = new SimpleStubAPI();
        api.overrideAPI("GetUIContextName", null);
        api.overrideAPI("GetUIContextNames", null);

        // this expression evaluates to false
        api.addExpression("GetProperty(\"xxx_enabled\",\"false\")==\"true\"", "false");
        api.addExpression("phoenix_config_GetProperty(\"phoenix/core/enableAdvancedViews\")", false);
        ISageAPIProvider old = SageAPI.getProvider();
        SageAPI.setProvider(api);

        ViewFactory fact = new ViewFactory();
        fact.setName("test");
        fact.addTag("test");
        fact.getOption(ViewFactory.OPT_VISIBLE).value().set("false");
        FactoryRegistry<ViewFactory> fr = new FactoryRegistry<ViewFactory>("views");
        fr.addFactory(fact);

        fact = new ViewFactory();
        fact.setName("tv");
        fact.addTag("tv");
        fact.getOption(ViewFactory.OPT_VISIBLE).value().set("true");
        fr.addFactory(fact);

        // should return all factories
        List<ViewFactory> list = fr.getFactories(true);
        assertEquals(2, list.size());
        assertFalse("Should Not Be Visible: " + list.get(0), list.get(0).isVisible());
        assertTrue(list.get(1).isVisible());

        list = fr.getFactories();
        assertEquals(1, list.size());

        // since the are not visible, should return 0 tags
        Set<ViewFactory> list2 = fr.getFactories("test");
        assertEquals(0, list2.size());

        // should return 1 tag, since it is visible
        list2 = fr.getFactories("tv");
        assertEquals(1, list2.size());
        assertEquals("tv", list2.iterator().next().getName());

        // should return 1 tag, since it is visible, we are asking for invisible
        // ones as well
        list2 = fr.getFactories("test", true);
        assertEquals(1, list2.size());
        assertEquals("test", list2.iterator().next().getName());

        fact = new ViewFactory();
        fact.setName("xxx");
        fact.addTag("xxx");
        fact.getOption(ViewFactory.OPT_VISIBLE).value().setValue("${GetProperty(\"xxx_enabled\",\"false\")==\"true\"}");
        assertFalse(fact.isVisible());
        fr.addFactory(fact);

        // should be none, since it is hidden
        list2 = fr.getFactories("xxx", false);
        assertEquals(0, list2.size());

        // should be 1, since it is hidden, since include invisible is true
        list2 = fr.getFactories("xxx", true);
        assertEquals(1, list2.size());

        // this expression evaluates to true, this changes things so that
        // we should now be returning 1 result for xxx since it is not hidden
        api.addExpression("GetProperty(\"xxx_enabled\",\"false\")==\"true\"", "true");
        list2 = fr.getFactories("xxx", false);
        assertEquals(1, list2.size());

        List<ViewFactory> factories = phoenix.umb.GetViewFactories();
        for (ViewFactory vf : factories) {
            System.out.println("View: " + vf.getName() + "; " + vf.isVisible());
            assertTrue("Should only return visible views", vf.isVisible());
        }

        api.addExpression("phoenix_config_GetProperty(\"phoenix/core/enableAdvancedViews\")", false);
        Set<ViewFactory> factories2 = phoenix.umb.GetViewFactories("online");
        for (ViewFactory vf : factories2) {
            System.out.println("! Online View: " + vf.getName() + "; " + vf.isVisible() + "; " + phoenix.umb.IsVisible(vf));
        }
        assertEquals(0, factories2.size());

        SageAPI.setProvider(old);
    }

    @Test
    public void testGenreFilter() {
        FileMediaFile mf = (FileMediaFile) FileResourceFactory.createResource(new File("/tmp/file.avi"));
        mf.getMetadata().getGenres().add("Movie");
        mf.getMetadata().getGenres().add("Action");

        Filter f = new GenresFilter();
        f.setValue("movie.*|film.*");
        f.getOption(GenresFilter.OPT_USE_REGEX).value().setValue("true");
        f.setChanged(true);

        assertEquals(true, f.accept(mf));

        f.setValue("film.*");
        f.setChanged(true);
        assertEquals(false, f.accept(mf));

        f.setValue("movie.*|film.*");
        f.getOption(GenresFilter.OPT_USE_REGEX).value().setValue("false");
        f.setChanged(true);
        assertEquals(false, f.accept(mf));

        f.setValue("Action");
        f.getOption(GenresFilter.OPT_USE_REGEX).value().setValue("false");
        f.setChanged(true);
        assertEquals(true, f.accept(mf));

        f.setValue("Action/Adventure");
        f.getOption(GenresFilter.OPT_USE_REGEX).value().setValue("false");
        f.setChanged(true);
        assertEquals(false, f.accept(mf));
    }

    @Test
    public void testNestedOptionsInFilter() throws ParserConfigurationException, SAXException, IOException {
        FileMediaFile mf = (FileMediaFile) FileResourceFactory.createResource(new File("/tmp/Movies/New/Beach Ball/file.avi"));
        FileMediaFile mf2 = (FileMediaFile) FileResourceFactory.createResource(new File("/tmp/Movies/Wow/Beach Ball/file.avi"));

        // test using options in the filter
        String filter = "<!DOCTYPE vfs SYSTEM \"vfs.dtd\">\r\n" + "<vfs>\r\n" + "   <filters>\r\n"
                + "        <item-group name=\"testfilterexclude2\" label=\"Movies Only\" mode=\"and\">\r\n"
                + "          <filter by=\"filepath\" scope=\"exclude\">\r\n" + "             <option name=\"value\">New</option>\n"
                + "          </filter>\r\n" + "        </item-group>\r\n" + "   </filters>\r\n" + "</vfs>\r\n" + "";

        VFSBuilder b = new VFSBuilder(Phoenix.getInstance().getVFSManager());
        b.registerVFSSources(filter, Phoenix.getInstance().getVFSDir(), Phoenix.getInstance().getVFSManager());

        Filter f = phoenix.umb.CreateFilter("testfilterexclude2");
        assertEquals(false, f.accept(mf));
        assertEquals(true, f.accept(mf2));
    }

    @Test
    public void testAbilityToOverrideFiltersInViews() throws Exception {
        File mfdir = makeDir("TestOverride/Movies/New/");
        FileMediaFile mf = (FileMediaFile) FileResourceFactory.createResource(makeFile("TestOverride/Movies/New/File1.avi"));
        FileMediaFile mf2 = (FileMediaFile) FileResourceFactory.createResource(makeFile("TestOverride/Movies/New/File2.avi"));

        String view = "<!DOCTYPE vfs SYSTEM \"vfs.dtd\">\r\n" + "<vfs>" + "   <filters>\r\n"
                + "        <item-group name=\"testoverride\" label=\"Test Override\" mode=\"and\">\r\n"
                + "          <filter by=\"filepath\" scope=\"exclude\">\r\n"
                + "             <option name=\"value\">File1</option>\n" + "          </filter>\r\n" + "        </item-group>\r\n"
                + "   </filters>\r\n" + "  <views>" + "		<view name=\"test_override_view\" label=\"Test Override\">\r\n"
                + "			<description>Test Override</description>\r\n" + "			<source name=\"filesystem\">\r\n"
                + "             <option name=\"dir\">" + mfdir.getAbsolutePath() + "</option>\n" + "           </source>"
                + "			<filter by=\"testoverride\"/>\r\n" + "		</view>" + "  </views>" + "</vfs>";

        VFSOrganizer org = new VFSOrganizer(Phoenix.getInstance().getVFSDir());
        org.organize(new StringReader(view), "view1");

        // Create a new filter using the same name.
        // the previous view should be using this new filter when
        // create view is called, lets see if that happens
        String filter = "<!DOCTYPE vfs SYSTEM \"vfs.dtd\">\r\n" + "<vfs>\r\n" + "   <filters>\r\n"
                + "        <item-group name=\"testoverride\" label=\"Overriding it again\" mode=\"and\">\r\n"
                + "          <filter by=\"filepath\" scope=\"exclude\">\r\n" + "             <option name=\"value\">BOB</option>\n"
                + "          </filter>\r\n" + "        </item-group>\r\n" + "   </filters>\r\n" + "</vfs>\r\n" + "";

        org.organize(new StringReader(filter), "filter");

        StringWriter w = new StringWriter();
        org.writeTo(w);

        VFSBuilder b = new VFSBuilder(Phoenix.getInstance().getVFSManager());
        b.registerVFSSources(w.getBuffer().toString(), Phoenix.getInstance().getVFSDir(), Phoenix.getInstance().getVFSManager());

        ViewFolder v = phoenix.umb.CreateView("test_override_view");
        // TODO: Fix this, this is still broken
        assertEquals(2, v.getChildren().size());
    }

    @Test
    public void testAbilityToOverrideReferencesViewsInViews() throws Exception {
        File mfdir = makeDir("TestOverride/Movies/New/");
        FileMediaFile mf = (FileMediaFile) FileResourceFactory.createResource(makeFile("TestOverride/Movies/New/File1.avi"));
        FileMediaFile mf2 = (FileMediaFile) FileResourceFactory.createResource(makeFile("TestOverride/Movies/New/File2.avi"));

        VFSOrganizer org = new VFSOrganizer(Phoenix.getInstance().getVFSDir());
        String view = "<!DOCTYPE vfs SYSTEM \"vfs.dtd\">\r\n" + "<vfs>" + "   <filters>\r\n"
                + "        <item-group name=\"testoverride\" label=\"Test Override\" mode=\"and\">\r\n"
                + "          <filter by=\"filepath\" scope=\"exclude\">\r\n"
                + "             <option name=\"value\">File1</option>\n" + "          </filter>\r\n" + "        </item-group>\r\n"
                + "   </filters>\r\n" + "  <views>" + "		<view name=\"test_override_view\" label=\"Test Override\">\r\n"
                + "			<description>Test Override</description>\r\n" + "			<source name=\"filesystem\">\r\n"
                + "             <option name=\"dir\">" + mfdir.getAbsolutePath() + "</option>\n" + "           </source>"
                + "			<filter by=\"testoverride\"/>\r\n" + "		</view>"
                + "		<view name=\"test_override_view2\" label=\"Test Override2\" flat=\"true\">\r\n"
                + "			<description>Test Override 2</description>\r\n" + "			<view-source name=\"test_override_view\"/>\r\n"
                + "		</view>" + "  </views>" + "</vfs>";

        org.organize(new StringReader(view), "view1");

        // Create a new view, that overrides the first view, and removes the
        // filter.
        // the net result is that our second view should now use the new source
        String views = "<!DOCTYPE vfs SYSTEM \"vfs.dtd\">\r\n" + "<vfs>\r\n" + "  <views>"
                + "		<view name=\"test_override_view\" label=\"Test Override\">\r\n"
                + "			<description>Test Override</description>\r\n" + "			<source name=\"filesystem\">\r\n"
                + "             <option name=\"dir\">" + mfdir.getAbsolutePath() + "</option>\n" + "           </source>"
                + "		</view>" + "  </views>" + "</vfs>\r\n" + "";

        org.organize(new StringReader(views), "view2");
        StringWriter w = new StringWriter();
        org.writeTo(w);

        VFSBuilder b = new VFSBuilder(Phoenix.getInstance().getVFSManager());
        b.registerVFSSources(w.getBuffer().toString(), Phoenix.getInstance().getVFSDir(), Phoenix.getInstance().getVFSManager());

        // if the view references works correctly, then we should now be using
        // the new
        // view without filtering as our new view source
        ViewFolder v = phoenix.umb.CreateView("test_override_view2");
        assertEquals(2, v.getChildren().size());
    }

    @Test
    public void testFilterCloning() throws ParserConfigurationException, SAXException, IOException {
        String view = "<!DOCTYPE vfs SYSTEM \"vfs.dtd\">\r\n" + "<vfs>" + "   <filters>\r\n"
                + "        <item-group name=\"testclone\" label=\"Test Override\" mode=\"and\">\r\n"
                + "          <filter by=\"metadata\" scope=\"include\">\r\n"
                + "             <option name=\"field\">MediaType</option>\n" + "             <option name=\"value\">TV</option>\n"
                + "          </filter>\r\n" + "        </item-group>\r\n" + "   </filters>\r\n" + "</vfs>";

        assertTrue("VFS Manager Filters not loaded??", Phoenix.getInstance().getVFSManager().getVFSFilterFactory().getFactories()
                .size() > 0);

        VFSBuilder b = new VFSBuilder(Phoenix.getInstance().getVFSManager());
        b.registerVFSSources(view, Phoenix.getInstance().getVFSDir(), Phoenix.getInstance().getVFSManager());

        VirtualMediaFile vmf = new VirtualMediaFile("Test");
        vmf.getMetadata().setMediaType("TV");
        assertEquals("TV", vmf.getMetadata().getMediaType());

        Filter f = phoenix.umb.CreateFilter("testclone");
        assertTrue("Did Not Accept TV", f.accept(vmf));
    }

    private static class Recording {
        private static int ids = 0;
        public int id;
        public String name;
        public String ep;
        public boolean deleted = false;

        public Recording(String name, String ep) {
            this.id = ids++;
            this.name = name;
            this.ep = ep;
        }
    }

    @Test
    public void testGroupingAPI() {
        final Recording[] recordings = new Recording[]{new Recording("House", "EP1"), new Recording("House", "EP2"),
                new Recording("House", "EP3"), new Recording("Bones", "EP1"), new Recording("Bones", "EP2"),};

        ISageAPIProvider prov = new ISageAPIProvider() {
            @Override
            public Object callService(String context, String name, Object[] args) throws Exception {
                if ("GetMediaFiles".equals(name) || "EvaluateExpression".equals(name)) {
                    List<Recording> items = new ArrayList<Recording>(Arrays.asList(recordings));
                    for (Iterator<Recording> i = items.iterator(); i.hasNext(); ) {
                        Recording r = i.next();
                        if (r.deleted) {
                            i.remove();
                        }
                    }
                    return items.toArray(new Recording[]{});
                } else if ("GetMediaFileID".equals(name) || "GetAiringID".equals(name)) {
                    return ((Recording) args[0]).id;
                } else if ("IsTVFile".equals(name) || "IsAiringObject".equals(name) || "IsMediaFileObject".equals(name)) {
                    return true;
                } else if ("GetShowEpisode".equals(name)) {
                    return ((Recording) args[0]).ep;
                } else if ("GetShowExternalID".equals(name)) {
                    return "EP" + ((Recording) args[0]).id;
                } else if ("GetShowTitle".equals(name) || "GetMediaTitle".equals(name)) {
                    return ((Recording) args[0]).name;
                } else if ("GetShowCategory".equals(name) || "GetMediaFileMetadata".equals(name)) {
                    return null;
                } else {
                    System.out.println("Unhandled API: " + name);
                }
                return null;
            }

            @Override
            public Object callService(String name, Object[] args) throws Exception {
                return callService(null, name, args);
            }
        };

        SageAPI.setProvider(prov);

        ViewFolder view = phoenix.umb.CreateView("phoenix.view.source.mediafiles");
        assertNotNull("Didn't create recordings view", view);
        assertEquals(5, view.getChildren().size());

        GroupingFactory gf = null;
        Set<GroupingFactory> groupers = phoenix.umb.GetAvailableGroupers(view);
        for (GroupingFactory f : groupers) {
            System.out.println("Grouper: " + f.getName());
            if ("title".equals(f.getName())) {
                gf = f;
            }
        }
        assertNotNull("No title grouper", gf);
        phoenix.umb.SetGrouper(view, (Grouper) phoenix.umb.Create(gf));
        assertEquals(2, view.getChildren().size());

        // test if the GetGrouper api works
        Grouper g = phoenix.umb.GetGrouper("title", view);
        assertNotNull(g);
        assertEquals("title", g.getName());

        g = phoenix.umb.GetGrouper("mytitlegrouper", view);
        assertNull(g);

        Sorter s = phoenix.umb.GetSoter("mysorter", view);
        assertNull(s);
    }

    @Test
    public void testDeleteChildrenOfViewRemovesFolderFromViewAsWell() {
        final Recording[] recordings = new Recording[]{new Recording("House", "EP1"), new Recording("House", "EP2"),
                new Recording("House", "EP3"), new Recording("Bones", "EP1"), new Recording("Bones", "EP2"),};

        ISageAPIProvider prov = new ISageAPIProvider() {
            @Override
            public Object callService(String context, String name, Object[] args) throws Exception {
                if ("GetMediaFiles".equals(name) || "EvaluateExpression".equals(name)) {
                    System.out.println("** GetMediaFiles");
                    List<Recording> items = new ArrayList<Recording>(Arrays.asList(recordings));
                    for (Iterator<Recording> i = items.iterator(); i.hasNext(); ) {
                        Recording r = i.next();
                        if (r.deleted) {
                            System.out.println("*** Removing: " + r.name);
                            i.remove();
                        }
                    }
                    return items.toArray(new Recording[]{});
                } else if ("GetMediaFileID".equals(name) || "GetAiringID".equals(name)) {
                    return ((Recording) args[0]).id;
                } else if ("IsTVFile".equals(name) || "IsAiringObject".equals(name) || "IsMediaFileObject".equals(name)) {
                    return true;
                } else if ("GetShowEpisode".equals(name)) {
                    return ((Recording) args[0]).ep;
                } else if ("GetShowExternalID".equals(name)) {
                    return "EP" + ((Recording) args[0]).id;
                } else if ("GetShowTitle".equals(name) || "GetMediaTitle".equals(name)) {
                    return ((Recording) args[0]).name;
                } else if ("GetShowCategory".equals(name) || "GetMediaFileMetadata".equals(name)) {
                    return null;
                } else if ("DeleteFile".equals(name)) {
                    System.out.println("Deleting: " + ((Recording) args[0]).name);
                    ((Recording) args[0]).deleted = true;
                    return true;
                } else if ("GetProperty".equals(name)) {
                    return System.getProperty(name, "");
                } else {
                    System.out.println("Unhandled API: " + name);
                }
                return null;
            }

            @Override
            public Object callService(String name, Object[] args) throws Exception {
                return callService(null, name, args);
            }
        };

        SageAPI.setProvider(prov);

        ViewFolder view = phoenix.umb.CreateView("phoenix.view.source.mediafiles");
        assertNotNull("Didn't create recordings view", view);
        assertEquals(5, view.getChildren().size());

        GroupingFactory gf = null;
        Set<GroupingFactory> groupers = phoenix.umb.GetAvailableGroupers(view);
        for (GroupingFactory f : groupers) {
            System.out.println("Grouper: " + f.getName());
            if ("title".equals(f.getName())) {
                gf = f;
            }
        }
        assertNotNull("No title grouper", gf);
        phoenix.umb.SetGrouper(view, (Grouper) phoenix.umb.Create(gf));
        assertEquals(2, view.getChildren().size());

        IMediaFolder chf = (IMediaFolder) view.getChild("Bones");
        // delete the 2 children of bones..
        chf.getChildren().get(0).delete(new Hints());
        chf.getChildren().get(0).delete(new Hints());

        assertEquals(0, chf.getChildren().size());

        chf = (IMediaFolder) view.getChild("Bones");
        view.accept(new PrintTreeVisitor(new PrintWriter(System.out)), null, IMediaFolder.DEEP_UNLIMITED);

        // now test the view to see if we still have bones
        assertEquals(1, view.getChildren().size());
    }

    @Test
    public void testDeleteFile() {
        final Recording[] recordings = new Recording[]{new Recording("House", "EP1"), new Recording("House", "EP2"),
                new Recording("House", "EP3"), new Recording("Bones", "EP1"), new Recording("Bones", "EP2"),};

        ISageAPIProvider prov = new ISageAPIProvider() {
            @Override
            public Object callService(String context, String name, Object[] args) throws Exception {
                if ("GetMediaFiles".equals(name) || "EvaluateExpression".equals(name)) {
                    System.out.println("** Getting files");
                    List<Recording> items = new ArrayList<Recording>(Arrays.asList(recordings));
                    for (Iterator<Recording> i = items.iterator(); i.hasNext(); ) {
                        Recording r = i.next();
                        if (r.deleted) {
                            System.out.println("** Removing");
                            i.remove();
                        }
                    }
                    return items.toArray(new Recording[]{});
                } else if ("GetMediaFileID".equals(name) || "GetAiringID".equals(name)) {
                    return ((Recording) args[0]).id;
                } else if ("IsTVFile".equals(name) || "IsAiringObject".equals(name) || "IsMediaFileObject".equals(name)) {
                    return true;
                } else if ("GetShowEpisode".equals(name)) {
                    return ((Recording) args[0]).ep;
                } else if ("GetShowExternalID".equals(name)) {
                    return "EP" + ((Recording) args[0]).id;
                } else if ("GetShowTitle".equals(name) || "GetMediaTitle".equals(name)) {
                    return ((Recording) args[0]).name;
                } else if ("GetShowCategory".equals(name) || "GetMediaFileMetadata".equals(name)) {
                    return null;
                } else if ("DeleteFile".equals(name)) {
                    System.out.println("**Deleted Called");
                    ((Recording) args[0]).deleted = true;
                    return true;
                } else if ("DeleteFileWithoutPrejudice".equals(name)) {
                    System.out.println("**DeletedWithoutPred Called");
                    // we are not going to allow it
                    return false;
                } else {
                    System.out.println("Unhandled API: " + name);
                }
                return null;
            }

            @Override
            public Object callService(String name, Object[] args) throws Exception {
                return callService(null, name, args);
            }
        };

        SageAPI.setProvider(prov);

        ViewFolder view = phoenix.umb.CreateView("phoenix.view.source.mediafiles");
        assertNotNull("Didn't create recordings view", view);
        assertEquals(5, view.getChildren().size());

        ViewItem vi = (ViewItem) view.getChildren().get(1);
        assertNotNull(vi);
        Hints hints = new Hints();
        boolean deleted = vi.delete(hints);
        assertTrue("Did not delete file", deleted);
        assertEquals(4, view.getChildren().size());
    }

    @Test
    public void testDeleteFileRemoveFromparent() {
        VirtualMediaFolder root = new VirtualMediaFolder("Root");
        VirtualMediaFolder parent = new VirtualMediaFolder(root, "Parent");
        root.addMediaResource(parent);
        VirtualMediaFile file = new VirtualMediaFile(parent, "Child");
        parent.addMediaResource(file);
        assertEquals(1, root.getChildren().size());
        assertEquals(1, parent.getChildren().size());
        file.delete(new Hints());
        assertEquals(0, parent.getChildren().size());
        // root folder should be empty as well
        assertEquals(0, root.getChildren().size());
    }

    @Test
    public void testBookmarks() {
        final Recording[] recordings = new Recording[]{new Recording("House", "EP1"), new Recording("House", "EP2"),
                new Recording("House", "EP3"), new Recording("Bones", "EP1"), new Recording("Bones", "EP2"),};

        ISageAPIProvider prov = new ISageAPIProvider() {
            @Override
            public Object callService(String context, String name, Object[] args) throws Exception {
                if ("GetMediaFiles".equals(name) || "EvaluateExpression".equals(name)) {
                    List<Recording> items = new ArrayList<Recording>(Arrays.asList(recordings));
                    for (Iterator<Recording> i = items.iterator(); i.hasNext(); ) {
                        Recording r = i.next();
                        if (r.deleted) {
                            i.remove();
                        }
                    }
                    return items.toArray(new Recording[]{});
                } else if ("GetMediaFileID".equals(name) || "GetAiringID".equals(name)) {
                    return ((Recording) args[0]).id;
                } else if ("IsTVFile".equals(name) || "IsAiringObject".equals(name) || "IsMediaFileObject".equals(name)) {
                    return true;
                } else if ("GetShowEpisode".equals(name)) {
                    return ((Recording) args[0]).ep;
                } else if ("GetShowExternalID".equals(name)) {
                    return "EP" + ((Recording) args[0]).id;
                } else if ("GetShowTitle".equals(name) || "GetMediaTitle".equals(name)) {
                    return ((Recording) args[0]).name;
                } else if ("GetShowCategory".equals(name) || "GetMediaFileMetadata".equals(name)) {
                    return null;
                } else if ("DeleteFile".equals(name)) {
                    ((Recording) args[0]).deleted = true;
                    return true;
                } else if ("DeleteFileWithoutPrejudice".equals(name)) {
                    // we are not going to allow it
                    return false;
                } else if ("GetProperty".equals(name)) {
                    return System.getProperty(name, "");
                } else {
                    System.out.println("Unhandled API: " + name);
                }
                return null;
            }

            @Override
            public Object callService(String name, Object[] args) throws Exception {
                return callService(null, name, args);
            }
        };

        SageAPI.setProvider(prov);

        ViewFolder view = phoenix.umb.CreateView("phoenix.view.default.allTV");
        assertNotNull("Didn't create recordings view", view);
        assertTrue(view.getChildren().size() > 0);
        dumpResources(view.getChildren());

        view = phoenix.umb.CreateView("phoenix.view.default.allTV", "{bookmark:'/House'}");
        assertNotNull("Didn't create recordings view", view);
        assertEquals(3, view.getChildren().size());
        assertTrue(view.getParent() != null);

        view = phoenix.umb.CreateView("phoenix.view.default.allTV", "{root:'/Bones'}");
        assertNotNull("Didn't create recordings view", view);
        assertEquals(2, view.getChildren().size());
        assertTrue(view.getParent() == null);
    }

    @Test
    public void testMetadataGrouper() {
        SimpleStubAPI api = new SimpleStubAPI();
        api.overrideAPI("IsTVFile", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,
        api.overrideAPI("GetShowEpisode", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,
        api.overrideAPI("GetShowTitle", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,

        SageAPI.setProvider(api);
        Airing a = api.newMediaFile(100);
        a.put("GetMediaTitle", "Title 1");
        a.METADATA.put("Year", "2009");

        a = api.newMediaFile(101);
        a.put("GetMediaTitle", "Title 2");
        a.METADATA.put("Year", "2008");

        a = api.newMediaFile(102);
        a.put("GetMediaTitle", "Title 3");
        a.METADATA.put("Year", "2009");

        api.addExpression("GetMediaFiles()", api.mediafiles.values().toArray());

        ViewFolder f = phoenix.umb.CreateView("phoenix.view.source.mediafiles");
        assertEquals(3, f.getChildren().size());

        Grouper g = new Grouper(new MetadataFieldGrouper());
        g.getOption(MetadataFieldGrouper.OPT_METADATA_FIELD).value().setValue("Year");
        g.setChanged(true);
        f.setGrouper(g);
        f.setChanged();
        assertEquals(2, f.getChildren().size());
        assertEquals("2009", f.getChild("2009").getTitle());
        assertEquals("2008", f.getChild("2008").getTitle());
        assertEquals(2, ((IMediaFolder) f.getChild("2009")).getChildren().size());

        assertNotNull(Phoenix.getInstance().getVFSManager().getVFSGroupFactory().getFactory("metadata"));
    }

    @Test
    public void testMetadataFilter() {
        SimpleStubAPI api = new SimpleStubAPI();
        api.overrideAPI("IsTVFile", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,
        api.overrideAPI("GetShowEpisode", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,
        api.overrideAPI("GetShowTitle", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,

        SageAPI.setProvider(api);
        Airing a = api.newMediaFile(100);
        a.put("GetMediaTitle", "Title 1");

        a.METADATA.put("Year", "2009");

        a = api.newMediaFile(101);
        a.put("GetMediaTitle", "Title 2");
        a.METADATA.put("Year", "2008");

        a = api.newMediaFile(102);
        a.put("GetMediaTitle", "Title 3");
        a.METADATA.put("Year", "2009");

        api.addExpression("GetMediaFiles()", api.mediafiles.values().toArray());

        ViewFolder f = phoenix.umb.CreateView("phoenix.view.source.mediafiles");
        assertEquals(3, f.getChildren().size());

        Filter filt = new MetadataFieldFilter();
        filt.getOption(MetadataFieldGrouper.OPT_METADATA_FIELD).value().setValue("Year");
        filt.getOption(MetadataFieldFilter.OPT_VALUE).value().setValue("2009");
        filt.getOption(MetadataFieldFilter.OPT_COMPARE_AS_NUMBER).value().setValue("true");
        filt.setChanged(true);
        f.setFilter(filt);
        f.setChanged();
        assertEquals(2, f.getChildren().size());
        assertEquals("Title 1", f.getChildren().get(0).getTitle());
        assertEquals("Title 3", f.getChildren().get(1).getTitle());
        f.removeFilter(filt);

        // test string filter
        filt = new MetadataFieldFilter();
        filt.getOption(MetadataFieldGrouper.OPT_METADATA_FIELD).value().setValue("Year");
        filt.getOption(MetadataFieldFilter.OPT_VALUE).value().setValue("2008");
        filt.getOption(MetadataFieldFilter.OPT_COMPARE_AS_NUMBER).value().setValue("false");
        filt.setChanged(true);
        f.setFilter(filt);
        f.setChanged();
        assertEquals(1, f.getChildren().size());
        assertEquals("Title 2", f.getChildren().get(0).getTitle());
        f.removeFilter(filt);

        assertNotNull(Phoenix.getInstance().getVFSManager().getVFSFilterFactory().getFactory("metadata"));
    }

    @Test
    public void testMetadataSorter() {
        SimpleStubAPI api = new SimpleStubAPI();
        api.overrideAPI("IsTVFile", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,
        api.overrideAPI("GetShowEpisode", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,
        api.overrideAPI("GetShowTitle", null); // consider overriding api;
        // {GetMediaTitle=Title 1,
        // IsAiringObject=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=100}:
        // test.junit.lib.SimpleStubAPI$Airing,

        SageAPI.setProvider(api);
        Airing a = api.newMediaFile(100);
        a.put("GetMediaTitle", "Title 1");

        a.METADATA.put("Year", "2009");

        a = api.newMediaFile(101);
        a.put("GetMediaTitle", "Title 2");
        a.METADATA.put("Year", "2008");

        a = api.newMediaFile(102);
        a.put("GetMediaTitle", "Title 3");
        a.METADATA.put("Year", "2010");

        api.addExpression("GetMediaFiles()", api.mediafiles.values().toArray());
        ViewFolder f = phoenix.umb.CreateView("phoenix.view.source.mediafiles");

        assertEquals(3, f.getChildren().size());

        Sorter s = new Sorter(new MetadataFieldSorter());
        s.getOption(MetadataFieldSorter.OPT_METADATA_FIELD).value().setValue("Year");
        s.getOption(MetadataFieldFilter.OPT_COMPARE_AS_NUMBER).value().setValue("true");
        s.setChanged(true);
        f.setSorter(s);
        f.setChanged();
        assertEquals(3, f.getChildren().size());
        assertEquals("Title 2", f.getChildren().get(0).getTitle());
        assertEquals("Title 1", f.getChildren().get(1).getTitle());
        assertEquals("Title 3", f.getChildren().get(2).getTitle());

        // test string sorter, desc
        f.removeSorter(s);
        s = new Sorter(new MetadataFieldSorter());
        s.getOption(MetadataFieldSorter.OPT_METADATA_FIELD).value().setValue("Year");
        s.getOption(MetadataFieldFilter.OPT_COMPARE_AS_NUMBER).value().setValue("false");
        s.getOption(Sorter.OPT_SORT_ORDER).value().setValue(Sorter.SORT_DESC);
        s.setChanged(true);
        f.setSorter(s);
        f.setChanged();
        assertEquals(3, f.getChildren().size());
        assertEquals("Title 3", f.getChildren().get(0).getTitle());
        assertEquals("Title 1", f.getChildren().get(1).getTitle());
        assertEquals("Title 2", f.getChildren().get(2).getTitle());

        assertNotNull(Phoenix.getInstance().getVFSManager().getVFSSortFactory().getFactory("metadata"));
    }

    @Test
    public void testFiltPathFilter() {
        FileMediaFile mf = (FileMediaFile) FileResourceFactory.createResource(new File("/tmp/path1/path2/file.avi"));

        Filter f = new FilePathFilter();
        f.setValue("path1|path2");
        f.getOption(FilePathFilter.OPT_USE_REGEX).value().setValue("true");
        f.setChanged(true);
        assertEquals(true, f.accept(mf));

        f.getOption(FilePathFilter.OPT_USE_REGEX).value().setValue("false");
        f.setChanged(true);
        assertEquals(false, f.accept(mf));

        f.setValue("/path1/path2");
        f.getOption(FilePathFilter.OPT_USE_REGEX).value().setValue("false");
        f.setChanged(true);
        assertEquals(false, f.accept(mf));

        f.setValue("/tmp/path1/path2");
        f.getOption(FilePathFilter.OPT_USE_REGEX).value().setValue("false");
        f.setChanged(true);
        assertEquals(true, f.accept(mf));

    }

    @Test
    public void testHomeVideosFilter() {
        ISageAPIProvider old = SageAPI.getProvider();
        try {
            SimpleStubAPI api = new SimpleStubAPI();
            api.LOG_MISSING_GETPROPERTY = true;
            api.getProperties().put("phoenix/homevideos/folders", "/tmp/path1/homemovies1/;/tmp/path1/homemovies2/");
            SageAPI.setProvider(api);

            Filter f = new HomeVideosFilter();
            assertEquals(false, f.accept(createFile("/tmp/path1/path2/file.avi")));
            assertEquals(true, f.accept(createFile("/tmp/path1/homemovies1/file.avi")));
            assertEquals(true, f.accept(createFile("/tmp/path1/homemovies2/file.avi")));
            assertEquals(false, f.accept(createFile("/tmp/path2/homemovies1/file.avi")));
            assertEquals(true, f.accept(createFile("/tmp/path1/homemovies2/subdir/file.avi")));
        } finally {
            SageAPI.setProvider(old);
        }
    }

    private FileMediaFile createFile(String file) {
        return (FileMediaFile) FileResourceFactory.createResource(new File(file));
    }
}
