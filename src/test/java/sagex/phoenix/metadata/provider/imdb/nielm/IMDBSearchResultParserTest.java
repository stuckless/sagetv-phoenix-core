package sagex.phoenix.metadata.provider.imdb.nielm;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.provider.nielm.NielmIMDBMetaDataProvider;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.Hints;
import test.InitPhoenix;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class IMDBSearchResultParserTest {
    private static final String TEST_SEARCH_TITLE = "Iron Man 2";

    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testSearchResults() throws MetadataException {
        NielmIMDBMetaDataProvider prov = (NielmIMDBMetaDataProvider) Phoenix.getInstance().getMetadataManager()
                .getProvider("nielm_imdb");
        assertNotNull("Nielm Provider was not installed", prov);

        SearchQuery q = new SearchQuery(new Hints());
        q.set(Field.RAW_TITLE, TEST_SEARCH_TITLE);
        q.set(Field.QUERY, TEST_SEARCH_TITLE);

        List<IMetadataSearchResult> results = prov.search(q);
        assertTrue("Results should have some data!", results.size() > 1);
        for (IMetadataSearchResult r : results) {
            if ("Iron Man 2".equalsIgnoreCase(r.getTitle())) {
                verifyResult(r, "Iron Man 2", 2010, "http://www.imdb.com/title/tt1228705/");
            } else if ("The Man with the Iron Fists".equalsIgnoreCase(r.getTitle())) {
                verifyResult(r, "The Man with the Iron Fists", 2012, "http://www.imdb.com/title/tt1258972/");
            } else if ("Iron Man 3".equalsIgnoreCase(r.getTitle())) {
                verifyResult(r, "Iron Man 3", 2013, "http://www.imdb.com/title/tt1300854/");
            }
        }
    }

    private void verifyResult(IMetadataSearchResult res, String title, int year, String url) {
        assertEquals(title, res.getTitle());
        assertEquals(year, res.getYear());
        assertEquals(url, res.getUrl());
        assertEquals(MediaType.MOVIE, res.getMediaType());
        assertTrue("Invalid Score: " + res.getScore(), res.getScore() > 0.3f);
    }
}
