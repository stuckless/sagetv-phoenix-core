package sagex.phoenix.metadata.search;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
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

}