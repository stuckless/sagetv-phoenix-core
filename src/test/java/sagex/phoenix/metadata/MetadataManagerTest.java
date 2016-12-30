package sagex.phoenix.metadata;

import org.junit.BeforeClass;
import org.junit.Test;
import sage.SageProperties;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import test.InitPhoenix;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by seans on 27/12/16.
 */
public class MetadataManagerTest {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(false, true);
    }

    @Test
    public void searchByExisting() throws Exception {
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File("test.avi"));
        IMetadata md = MetadataUtil.createMetadata();
        md.setMediaProviderDataID("201088");
        md.setMediaProviderID("tmdb");
        IMetadata mdNew = Phoenix.getInstance().getMetadataManager().searchByExisting(mf, md);
        assertTrue(mdNew.getFanart().size()>0);
        assertEquals("Blackhat", mdNew.getMediaTitle());
    }

    @Test
    public void searchByExistingMissingIDButHasIMDB() throws Exception {
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File("test.avi"));
        IMetadata md = MetadataUtil.createMetadata();
        md.setMediaProviderDataID("");
        md.setIMDBID("tt2717822");
        md.setMediaProviderID("tmdb");
        IMetadata mdNew = Phoenix.getInstance().getMetadataManager().searchByExisting(mf, md);
        assertTrue(mdNew.getFanart().size()>0);
        assertEquals("Blackhat", mdNew.getMediaTitle());
    }


    @Test
    public void searchByExistingUsingIMDB() throws Exception {
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File("test.avi"));
        IMetadata md = MetadataUtil.createMetadata();
        md.setMediaProviderDataID("tt2717822");
        md.setMediaProviderID("imdb");
        IMetadata mdNew = Phoenix.getInstance().getMetadataManager().searchByExisting(mf, md);
        assertTrue(mdNew.getFanart().size()>0);
        assertEquals("Blackhat", mdNew.getMediaTitle());

    }

}