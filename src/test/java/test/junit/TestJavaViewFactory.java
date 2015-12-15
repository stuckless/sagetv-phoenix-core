package test.junit;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.db.ParseException;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.XmlViewSerializer;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.groups.TitleGrouper;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;
import sagex.phoenix.vfs.views.ViewPresentation;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestJavaViewFactory {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testRegexTitleFilter() throws ParseException, CloneNotSupportedException, IOException {
        SimpleStubAPI api = new SimpleStubAPI();
        int id = 1;
        Airing mf = api.newMediaFile(id++);
        mf.put("GetMediaTitle", "House");
        mf.put("IsTVFile", true);
        mf.put("GetShowTitle", "House");
        mf.put("GetShowEpisode", "Pilot");
        mf.METADATA.put("Title", "House");
        mf.METADATA.put("MediaType", "TV");
        mf.METADATA.put("SeasonNumber", "2");
        api.addExpression("GetMediaFiles()", new Object[]{mf});

        SageAPI.setProvider(api);

        // create the source
        // Either get an instance of a known source, and clone it,
        Factory<IMediaFolder> source = (Factory<IMediaFolder>) Phoenix.getInstance().getVFSManager().getVFSSourceFactory()
                .getFactory("expression").clone();
        // or Create a new factory instance
        // Factory<IMediaFolder> source = new SageExpressionSourceFactory();
        // source.setName("expression");
        source.getOption("expression").value().set("GetMediaFiles()");

        // create the factory and then add the source
        ViewFactory f = new ViewFactory();
        f.setName("myview");
        f.addFolderSource(source);

        // set the presentation
        ViewPresentation vp1 = new ViewPresentation(0);
        // this should work, but doesn't
        // Grouper g = phoenix.umb.CreateGrouper("firstletter");
        // so, we need to do this
        Grouper g = phoenix.umb.CreateGrouper("regextitle");
        g.getOption("regex").value().set(".");
        vp1.getGroupers().add(g);
        f.addViewPresentations(vp1);

        // create the view
        ViewFolder vf = f.create(null);

        for (IMediaResource r : vf) {
            System.out.println("Title: " + r.getTitle());
        }

        assertTrue("not a folder", vf.getChildren().get(0) instanceof IMediaFolder);
        assertEquals("H", vf.getChildren().get(0).getTitle());

        XmlViewSerializer ser = new XmlViewSerializer();
        ser.serialize(f, System.out);

        // every ViewFolder's presentation items, (sorts, filters, grouper) are
        // configurable
        g = new Grouper(new TitleGrouper());
        vf.getGroupers().clear();
        phoenix.umb.SetGrouper(vf, g);
        for (IMediaResource r : vf) {
            System.out.println("Title2: " + r.getTitle());
        }

        System.out.println("Configurable Options for the grouper");
        for (String s : g.getOptionNames()) {
            System.out.println("Option: " + s + "; CurrentValue: " + g.getOption(s).value());
        }

    }
}
