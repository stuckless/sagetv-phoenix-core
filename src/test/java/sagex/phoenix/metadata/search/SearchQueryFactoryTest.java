package sagex.phoenix.metadata.search;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.impl.FileMediaFile;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import test.InitPhoenix;
import test.junit.lib.FilesTestCase;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by seans on 18/09/16.
 */
public class SearchQueryFactoryTest {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void createQueryFromFilename() throws Exception {
        File mfile = FilesTestCase.makeFile("test/Pokémon Go - s01e02.avi");
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(mfile);

        SearchQuery q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, MediaType.TV, new Hints());
        System.out.println(q);
        assertEquals("Pokémon Go", q.getRawTitle());
    }

    @Test
    public void createQueryFromBluray() throws Exception {
        //  \\SERVER-W1\MOVIES3\Doctor Strange\BDMV

        File mfile = FilesTestCase.makeFile("test/Doctor Strange/BDMV/video.m2ts");
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(mfile);

        // just verify that it thinks we have a bluray file
        assertTrue(mf.isType(MediaResourceType.BLURAY.value()));
        assertEquals("Doctor Strange", mf.getTitle());

        SearchQuery q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, MediaType.MOVIE, new Hints());
        System.out.println(q);
        assertNotNull("Search Querries should never be null!!", q);
        assertEquals("Doctor Strange", q.getRawTitle());
    }
}