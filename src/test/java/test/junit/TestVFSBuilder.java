package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.math.NumberUtils;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import sagex.ISageAPIProvider;
import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.common.SystemConfigurationFileManager.ConfigurationType;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.ViewStateSerializer;
import sagex.phoenix.vfs.builder.VFSBuilder;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.sorters.Sorter;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;
import sagex.phoenix.vfs.views.ViewPresentation;
import test.InitPhoenix;
import test.PhoenixTestBase;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

public class TestVFSBuilder extends PhoenixTestBase {

    @Test
    public void testXMLBuilder() throws Throwable {
        testRegistry("Source", Phoenix.getInstance().getVFSManager().getVFSSourceFactory().getFactories());
        assertTrue(Phoenix.getInstance().getVFSManager().getVFSSourceFactory().getFactories().size() > 2);

        testRegistry("Sort", Phoenix.getInstance().getVFSManager().getVFSSortFactory().getFactories());
        assertTrue(Phoenix.getInstance().getVFSManager().getVFSSortFactory().getFactories().size() > 2);

        testRegistry("Filter", Phoenix.getInstance().getVFSManager().getVFSFilterFactory().getFactories());
        assertTrue(Phoenix.getInstance().getVFSManager().getVFSFilterFactory().getFactories().size() > 2);

        testRegistry("Group", Phoenix.getInstance().getVFSManager().getVFSGroupFactory().getFactories());
        assertTrue(Phoenix.getInstance().getVFSManager().getVFSGroupFactory().getFactories().size() > 2);

        testRegistry("View", Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories());
        assertTrue(Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories().size() > 2);
    }

    private void testRegistry(String registry, List factories) {
        assertNotNull(factories);
        assertTrue(registry + " Registry is empty!", factories.size() > 0);
        for (Object o : factories) {
            Factory f = (Factory) o;
            System.out.println("Factory: " + f + " for " + registry);
            assertNotNull(f.getName());
            assertNotNull(f.getLabel());
        }
    }

    @Test
    public void testTags() {
        // should be none for that label
        Set fact = Phoenix.getInstance().getVFSManager().getVFSSourceFactory().getFactories("sdfsdfsd");
        assertEquals(0, fact.size());

        fact = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories("tv");
        for (Object f : fact) {
            System.out.println("tag:tv; Factory: " + ((Factory) f).getName());
        }
        // 4 tvs, tv, archivedtv, all, and filesystem
        assertTrue("Didn't load enough tags", fact.size() > 1);

        // 5 tvs, (tv, archivedtv, all, and filesystem) + 3 dvds (but 2 are
        // overlapping so 5 total)
        fact = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactories("tv, dvd");
        for (Object f : fact) {
            System.out.println("tag:tv,dvd; Factory: " + ((Factory) f).getName());
        }
        assertTrue("Didn't load enough factories", fact.size() > 5);

        Set<String> tags = Phoenix.getInstance().getVFSManager().getTags(true);
        for (String t : tags) {
            System.out.println("Tag: " + t + "; " + Phoenix.getInstance().getVFSManager().getTagLabel(t));
        }
        assertTrue("Didn't load enough tags", tags.size() > 10);

        tags = Phoenix.getInstance().getVFSManager().getTags(false);
        for (String t : tags) {
            System.out.println("Visible Tag: " + t + "; " + Phoenix.getInstance().getVFSManager().getTagLabel(t));
        }
        assertTrue("didn't load enough tags", tags.size() > 5);
        assertEquals("TV", Phoenix.getInstance().getVFSManager().getTagLabel("tv"));
        assertEquals("DVD", Phoenix.getInstance().getVFSManager().getTagLabel("dvd"));
    }

    @Test
    public void testSources() throws Exception {
        ISageAPIProvider prov = createNiceMock(ISageAPIProvider.class);
        expect(prov.callService(eq("GetMediaFiles"), aryEq(new Object[]{"T"}))).andAnswer(new IAnswer<Object[]>() {
            public Object[] answer() throws Throwable {
                return new Object[]{"1", "2"};
            }
        });

        expect(prov.callService(eq("GetMediaFiles"), aryEq(new Object[]{"TL"}))).andAnswer(new IAnswer<Object[]>() {
            public Object[] answer() throws Throwable {
                System.out.println("GETMEDIAFILES called");
                return new Object[]{"3", "4"};
            }
        });

        expect(prov.callService(eq("GetShowTitle"), (Object[]) anyObject())).andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                return "Title " + ((Object[]) getCurrentArguments()[1])[0];
            }
        }).anyTimes();

        expect(prov.callService(eq("GetShowEpisode"), (Object[]) anyObject())).andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                return "Title " + ((Object[]) getCurrentArguments()[1])[0];
            }
        }).anyTimes();

        expect(prov.callService(eq("GetMediaFileID"), (Object[]) anyObject())).andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                return NumberUtils.toInt((String) ((Object[]) getCurrentArguments()[1])[0]);
            }
        }).anyTimes();

        replay(prov);
        SageAPI.setProvider(prov);
        System.out.println("Creating View: archivedrecordings...");
        ViewFactory source = Phoenix.getInstance().getVFSManager().getVFSViewFactory()
                .getFactory("phoenix.view.source.sagearchivedrecordings");
        assertNotNull("No view: archivedrecordings", source);
        System.out.println("View: " + source);
        assertEquals(1, source.getFolderSources().size());
        IMediaFolder folder = source.create(null);
        assertNotNull("Failed to create folder!", folder);
        assertEquals(2, folder.getChildren().size());
        for (int i = 0; i < 2; i++) {
            System.out.println("RES: " + folder.getChildren().get(i));
            assertEquals("Title " + (i + 3), folder.getChildren().get(i).getTitle());
        }

        ViewFactory viewfact = Phoenix.getInstance().getVFSManager().getVFSViewFactory()
                .getFactory("phoenix.view.source.sagerecordings");
        assertNotNull(viewfact);
        assertTrue("View Factories should have tags.  This one doesn't.", viewfact.getTags().size() > 0);
        for (String s : viewfact.getTags()) {
            System.out.println("(((--- View Factory Tag: " + s);
        }

        ViewFolder view = viewfact.create(null);
        System.out.println("1View Has HashCode: " + view.hashCode());
        System.out.println("2View Has HashCode: " + view.hashCode());
        assertTrue("Views created using a factory, should have tags.  " + view.getTitle() + " doesn't.", view.getTags().size() > 0);
        System.out.println("View Has HashCode: " + view.hashCode());
        for (String s : view.getTags()) {
            System.out.println("(((--- Tag: " + s);
        }

        // assertTrue("Folder does not have 'tv' tag, and it should.",
        // phoenix.media.HasTag(view, "tv"));
        // assertTrue("Folder has 'music' tag and it should not.",
        // !phoenix.api.HasTag(view, "music"));
    }

    @Test
    public void testViewPresenterParsing() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("Testing tvseason");
        ViewFactory vf = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactory("phoenix.view.default.allTVseasons");
        assertNotNull(vf);
        Collection<ViewPresentation> presentations = vf.getViewPresentations();
        assertEquals(3, presentations.size());

        Iterator<ViewPresentation> i = presentations.iterator();
        ViewPresentation vp = i.next();
        assertTrue(vp.getFilters().size() == 0);
        assertTrue(vp.getSorters().size() == 1);
        assertTrue(vp.getGroupers().size() == 1);

        vp = i.next();
        assertTrue(vp.getFilters().size() == 0);
        assertTrue(vp.getSorters().size() == 1);
        assertTrue(vp.getGroupers().size() == 1);
        assertTrue(vp.getHints().size() == 0);

        vp = i.next();
        assertTrue(vp.getFilters().size() == 0);
        assertTrue(vp.getSorters().size() == 1);
        assertTrue(vp.getGroupers().size() == 0);
        assertTrue(vp.getHints().size() == 0);
    }

    @Test
    public void testGroupDefaultFolderName() throws ParserConfigurationException, SAXException, IOException {
        String viewxml = "<!DOCTYPE vfs SYSTEM \"vfs.dtd\">\r\n" + "<vfs><views>"
                + "  <view name=\"videosgroupedbygenre\" label=\"All Videos Grouped by Genre\">\r\n"
                + "			<description>Show Videos grouped by Year/Genre/etc</description>\r\n" + "			<tag value=\"video\"/>\r\n"
                + "			<tag value=\"web\"/>			\r\n" + "			<view-source name=\"phoenix.view.primary.importedmovies\"/>\r\n" + "\r\n"
                + "			<presentation>\r\n" + "				<group by=\"genre\">\r\n"
                + "					<option name=\"empty-foldername\" value=\"No Genre\"/>\r\n" + "				</group>\r\n"
                + "				<sort by=\"title\">\r\n" + "					<option name=\"sort-order\" value=\"asc\"/>\r\n" + "				</sort>\r\n"
                + "			</presentation>\r\n" + "		</view></views></vfs>		\r\n" + "";

        VFSBuilder.registerVFSSources(viewxml, Phoenix.getInstance().getVFSManager().getSystemFiles().getDir(), Phoenix
                .getInstance().getVFSManager());

        ViewFactory vf = Phoenix.getInstance().getVFSManager().getVFSViewFactory().getFactory("videosgroupedbygenre");
        ViewFolder f = vf.create(null);
        List<Grouper> groups = f.getPresentation().getGroupers();
        assertEquals(1, groups.size());
        Grouper g = groups.get(0);
        assertEquals("genre", g.getFactoryId());
        String folderanme = g.getOption(g.OPT_EMPTY_GROUPNAME).getString(null);
        assertNotNull(folderanme);
        System.out.println("Default Folder Name: " + folderanme);

        for (IMediaResource r : f) {
            System.out.println("Item: " + r.getTitle());
        }
    }

    @Test
    public void testParsingSingleView() {
        VFSManager m = Phoenix.getInstance().getVFSManager();
        m.visitConfigurationFile(ConfigurationType.System, InitPhoenix.ProjectHome("src/test/java/test/junit/TestVFS.xml"));
        ViewFactory x = m.getVFSViewFactory().getFactory("seansrecordings");
        assertNotNull(x);
    }

    private void setResordings() {
        SimpleStubAPI api = new SimpleStubAPI();
        Airing a = api.newMediaFile(0);
        a.put("IsTVFile", true);
        a.put("GetShowCategory", "TV");
        a.put("GetShowExternalID", "EP0000001"); // populate API
        a.put("GetShowTitle", "House"); // populate API
        a.put("GetShowEpisode", "House"); // populate API
        a.put("GetMediaTitle", "House"); // populate API
        a.put("GetAiringTitle", "House"); // populate API
        a.put("GetOriginalAiringDate", Calendar.getInstance().getTime().getTime()); // populate
        // API
        a.put("GetShowYear", "206"); // populate API
        a.put("GetSegmentFiles", null); // populate API
        a.METADATA.put("MediaType", "TV");

        Object airings[] = {a};

        api.addExpression("phoenix_util_ToArray(phoenix_util_RemoveAll(GetMediaFiles(\"T\"), GetMediaFiles(\"TL\")))", airings); // consider
        // adding
        // implementing
        // expression
        // with
        // real
        // return
        // value

        SageAPI.setProvider(api);
    }

    @Test
    public void testSaveViewState() throws IOException {
        setResordings();

        VFSManager m = Phoenix.getInstance().getVFSManager();
        m.visitConfigurationFile(ConfigurationType.System, InitPhoenix.ProjectHome("src/test/java/test/junit/TestVFS.xml"));
        ViewFactory x = m.getVFSViewFactory().getFactory("seansrecordings");
        assertNotNull(x);
        ViewFolder view = x.create(null);
        ViewStateSerializer ser = new ViewStateSerializer();
        String state = ser.getState(view);
        assertNotNull(state);
        System.out.println(state);

        // new state
        // {"presentations":[{"sorters":[{"name":"title","options":[{"name":"sort-order","value":"asc"},{"name":"folders-first","value":"true"},{"name":"ignore-the","value":"false"},{"name":"ignore-all","value":"false"}]}],"groupers":[{"name":"show","options":[]}]}],"view":"seansrecordings"}
        String newState = "{\"presentations\":[{\"sorters\":[{\"name\":\"title\",\"options\":[{\"name\":\"sort-order\",\"value\":\"desc\"},{\"name\":\"folders-first\",\"value\":\"false\"},{\"name\":\"ignore-the\",\"value\":\"false\"},{\"name\":\"ignore-all\",\"value\":\"false\"}]}],\"groupers\":[{\"name\":\"show\",\"options\":[]}]}],\"view\":\"seansrecordings\"}";
        ser.setState(newState, view);

        List l = view.getChildren();
        assertTrue(l.size() > 0);
        Sorter s = view.getViewFactory().getViewPresentation(0).getSorters().get(0);
        System.out.println("Changed: " + s.isChanged());
        System.out.println("TODO: Save View State");
        // assertEquals("desc",
        // s.getOption(Sorter.OPT_SORT_ORDER).getString(null));
        // assertFalse(s.isAscending());
        // assertFalse(s.isFoldersFirst());
    }

    @Test
    public void testHasTagOnSorter() throws IOException {
        VFSManager m = Phoenix.getInstance().getVFSManager();
        m.clear();
        m.visitConfigurationFile(ConfigurationType.System, InitPhoenix.ProjectHome("src/test/java/test/junit/TestVFS.xml"));

        Sorter s = phoenix.umb.CreateSorter("MySort");
        assertNotNull(s);
        assertTrue(phoenix.umb.HasTag(s, "video"));
        assertTrue(phoenix.umb.HasTag(s, "tv"));
        assertTrue(phoenix.umb.HasTag(s, "music"));
        assertFalse(phoenix.umb.HasTag(s, "tag-not-exist"));
    }

}
