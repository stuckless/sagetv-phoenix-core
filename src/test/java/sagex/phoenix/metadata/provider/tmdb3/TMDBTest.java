package sagex.phoenix.metadata.provider.tmdb3;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataManager;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import test.InitPhoenix;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by seans on 04/03/17.
 */
public class TMDBTest {
    static MetadataManager mgr;

    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);

        mgr = Phoenix.getInstance().getMetadataManager();
    }



    @Test
    public void testTMDBLookup() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.MOVIE, "Iron Man 2", "2010");
        List<IMetadataSearchResult> results = mgr.search("tmdb", query);
        assertTrue("Search for Iron Man 2 return nothing!", results.size() > 0);

        // ensure we get iron man
        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        assertEquals("10138", result.getId());
        assertEquals("tmdb", result.getProviderId());
        assertEquals(MediaType.MOVIE, result.getMediaType());
        assertEquals(2010, result.getYear());
        assertEquals("Iron Man 2", result.getTitle());

        // get the metadata, validate it
        IMetadata md = mgr.getMetdata(result);
        assertEquals("Iron Man 2", md.getMediaTitle());
        assertTrue(md.getActors().size() > 10);

        assertNotNull(md.getDescription());
        assertTrue(md.getDirectors().size() > 0);
        assertEquals("Iron Man 2", md.getEpisodeName());

        assertTrue(md.getFanart().size() > 1); // should have more than just a
        // poster

        assertTrue(md.getGenres().size() > 0);

        assertEquals("tt1228705", md.getIMDBID());
        assertEquals("10138", md.getMediaProviderDataID());
        assertEquals("tmdb", md.getMediaProviderID());
        assertEquals("Iron Man 2", md.getMediaTitle());
        assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
        // date is too volatile for testing
        //assertEquals(DateUtils.parseDate("2010-05-07").getTime(), md.getOriginalAirDate().getTime());
        assertEquals("PG-13", md.getRated());
        // no extended ratings in tmdb
        // assertTrue(md.getExtendedRatings().length()>4);
        assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("124"), md.getRunningTime());
        assertEquals("Iron Man 2", md.getEpisodeName());
        assertNull(md.getRelativePathWithTitle());

        assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);
        assertTrue(md.getWriters().size() > 0);
        assertEquals(2010, md.getYear());
    }

    @Test
    public void testTMDBLookupBYID() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.MOVIE, "XXXXX", "2010");
        query.set(SearchQuery.Field.PROVIDER, "tmdb");
        query.set(SearchQuery.Field.ID, "10138");

        List<IMetadataSearchResult> results = mgr.search("tmdb", query);
        assertTrue("Search for Iron Man 2 return nothing!", results.size() > 0);
        for (IMetadataSearchResult r : results) {
            System.out.println("Result: " + r);
        }

        // ensure we get iron man
        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        assertEquals("10138", result.getId());
        assertEquals("tmdb", result.getProviderId());
        assertEquals(MediaType.MOVIE, result.getMediaType());
        assertEquals(2010, result.getYear());
        assertEquals("Iron Man 2", result.getTitle());

        // get the metadata, validate it
        IMetadata md = mgr.getMetdata(result);
        assertEquals("Iron Man 2", md.getMediaTitle());
        assertTrue(md.getActors().size() > 10);
        assertTrue("Missing 'Robert Downey Jr.'", MetadataUtil.getActor("Robert Downey Jr.", md.getActors()) != null);

        assertNotNull(md.getDescription());
        assertTrue(md.getDirectors().size() > 0);
        assertEquals("Iron Man 2", md.getEpisodeName());

        assertTrue(md.getFanart().size() > 1); // should have more than just a
        // poster

        assertTrue(md.getGenres().size() > 0);

        assertEquals("tt1228705", md.getIMDBID());
        assertEquals("10138", md.getMediaProviderDataID());
        assertEquals("tmdb", md.getMediaProviderID());
        assertEquals("Iron Man 2", md.getMediaTitle());
        assertEquals(MediaType.MOVIE.sageValue(), md.getMediaType());
        // remove date, since it's too volatile for tests
        //assertEquals(DateUtils.parseDate("2010-05-07").getTime(), md.getOriginalAirDate().getTime());
        assertEquals("PG-13", md.getRated());
        // no extended ratings in tmdb
        // assertTrue(md.getExtendedRatings().length()>4);
        assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("124"), md.getRunningTime());
        assertEquals("Iron Man 2", md.getEpisodeName());
        assertNull(md.getRelativePathWithTitle());

        assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);
        assertTrue(md.getWriters().size() > 0);
        assertEquals(2010, md.getYear());
    }



    @Test
    public void testTMDBLookupWithSpecialCharacters() throws Exception {
        testSearch("tmdb", MediaType.MOVIE, "A Bug's Life", null, "A Bug's Life", "1998");
        testSearch("tmdb", MediaType.MOVIE, "The Incredibles", null, "The Incredibles", "2004");
        testSearch("tmdb", MediaType.MOVIE, "Monsters, Inc.", null, "Monsters, Inc.", "2001");
    }

    public void testSearch(String provider, MediaType type, String title, String year, String expectedTitle, String expectedYear)
            throws MetadataException {
        SearchQuery query = new SearchQuery(type, title, year);
        List<IMetadataSearchResult> results = mgr.search(provider, query);
        assertTrue("Search for " + title + " return nothing!", results.size() > 0);
        IMetadataSearchResult r = results.get(0);
        System.out.printf("Title: %s; %d; %s\n", r.getTitle(), r.getYear(), r.getScore());
        assertEquals(expectedTitle, r.getTitle());
        assertEquals(expectedYear, String.valueOf(r.getYear()));
    }


}
