package sagex.phoenix.metadata.provider.tmdb3;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.provider.tmdb.TMDBMetadataProvider;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DateUtils;
import test.InitPhoenix;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by jusjoken on 9/19/2021.
 * Added to test TMDB used for TV searching rather than TVDB
 */
public class TMDBTVTest {
    static MetadataManager mgr;

    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);

        mgr = Phoenix.getInstance().getMetadataManager();
    }

    @Test
    public void testFactoryLoading() {
        for (IMetadataProvider p : mgr.getProviders()) {
            System.out.println("Provider: " + p);
        }

        IMetadataProvider prov;

        prov = mgr.getProvider("tmdb");
        assertNotNull("Failed to load tmdb provider!", prov);
        assertNotNull(prov.getInfo().getName());
        assertNotNull(prov.getInfo().getDescription());
        assertEquals(MediaType.TV, prov.getInfo().getSupportedSearchTypes().get(0));

        assertTrue("getProviders() failed", mgr.getProviders().size() > 0);
        for (IMetadataProvider p : mgr.getProviders()) {
            System.out.println("Provider: " + p.getInfo().getId() + "; Name: " + p.getInfo().getName());
        }
    }

    @Test
    public void testTVDBApostrope() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "South Park", null);
        query.set(SearchQuery.Field.PROVIDER, "tmdb");
        //query.set(Field.ID, "75897");
        query.set(SearchQuery.Field.QUERY, "South Park");
        query.set(SearchQuery.Field.SEASON, "19");
        query.set(SearchQuery.Field.EPISODE, "4");

        List<IMetadataSearchResult> results = mgr.search("tmdb", query);
        for (IMetadataSearchResult r : results) {
            System.out.println("Result: " + r);
        }

        // ensure we get iron man
        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
//		assertEquals("10138", result.getId());
//		assertEquals("tmdb", result.getProviderId());
//		assertEquals(MediaType.MOVIE, result.getMediaType());
//		assertEquals(2010, result.getYear());
//		assertEquals("Iron Man 2", result.getTitle());


        IMetadata md = mgr.getMetdata(result);
        System.out.println(md.getMediaTitle());
        System.out.println(md.getEpisodeName());
        System.out.println(md.getDescription());
    }


    @Test
    public void testTVDBBySeasonEpisode() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
        query.set(SearchQuery.Field.SEASON, "2");
        query.set(SearchQuery.Field.EPISODE, "7");

        testTVDBMetadata(query);
    }

    @Test
    public void testTVDBByID() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "GGGGG", "2004");
        query.set(SearchQuery.Field.PROVIDER, "tmdb");
        query.set(SearchQuery.Field.ID, "1408");

        query.set(SearchQuery.Field.SEASON, "2");
        query.set(SearchQuery.Field.EPISODE, "7");

        testTVDBMetadata(query);
    }

    @Test
    public void testTVDBByTitle() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
        //SearchQuery query = new SearchQuery(MediaType.TV, "The Simpsons", "1989");
        query.set(SearchQuery.Field.EPISODE_TITLE, "Hunting");

        testTVDBMetadata(query);
    }

    @Test
    public void testTVDBByTitleLargeSeasons() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "The Simpsons", "1989");
        query.set(SearchQuery.Field.EPISODE_TITLE, "Treehouse of Horror");

        testTVDBMetadataLargeSeasons(query);
    }

    @Test
    public void testTVDBSeriesFanartOnly() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
        query.set(SearchQuery.Field.EPISODE_TITLE, "123ASDASD999DSSDSD");

        List<IMetadataSearchResult> results = mgr.search("tmdb", query);
        assertTrue("Search for returned nothing!", results.size() > 0);
        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        assertEquals(2004, result.getYear());
        assertEquals("1408", result.getId());
        assertEquals("tmdb", result.getProviderId());
        assertEquals(MediaType.TV, result.getMediaType());
        assertEquals("House", result.getTitle());
        // tmdb does not pass the url - ignore for this test
        //assertTrue(result.getUrl()!=null && result.getUrl().contains("1408"));

        IMetadata md = mgr.getMetdata(result);
        assertTrue("Failed to get Series Only Fanart", md.getFanart().size() > 0);
        for (IMediaArt ma : md.getFanart()) {
            System.out.println("Fanart: " + ma.getDownloadUrl());
            assertTrue("Should not have season in fanart", ma.getSeason() == 0);
            assertTrue(ma.getDownloadUrl(), ma.getDownloadUrl().startsWith("http"));
        }
    }

    @Test
    public void testTVDBByDate() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
        query.set(SearchQuery.Field.EPISODE_DATE, "2005-11-22");

        testTVDBMetadata(query);
    }

    private void testTVDBMetadata(SearchQuery query) throws Exception {
        List<IMetadataSearchResult> results = mgr.search("tmdb", query);
        //System.out.println("***testTVDBMetadata*** RESULTS:" + results);
        assertTrue("Search for returned nothing!", results.size() > 0);

        //System.out.println("***testTVDBMetadata*** QUERY:" + query);

        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        assertEquals(2004, result.getYear());
        assertEquals("1408", result.getId());
        assertEquals("tmdb", result.getProviderId());
        assertEquals(MediaType.TV, result.getMediaType());
        assertEquals("House", result.getTitle());

        // get the metadata, validate it
        //System.out.println("***testTVDBMetadata*** RESULT:" + result);
        IMetadata md = mgr.getMetdata(result);
        //System.out.println("***testTVDBMetadata*** MD EpisodeName:" + md.getEpisodeName());
        //System.out.println("***testTVDBMetadata*** MD MediaTitle:" + md.getMediaTitle());
        assertEquals("House", md.getMediaTitle());
        assertTrue(md.getActors().size() > 2);
        assertEquals("Hugh Laurie", md.getActors().get(0).getName());
        //assertEquals("Dr. Gregory House", md.getActors().get(0).getRole());

        //System.out.println("***testTVDBMetadata*** getDescription:" + md.getDescription() + " for episode:" + md.getEpisodeName() + " for Show:" + md.getMediaTitle());
        assertNotNull(md.getDescription());
        assertTrue(md.getExecutiveProducers().size() > 0);
        //System.out.println("***testTVDBMetadata*** getRelativePathWithTitle:" + md.getRelativePathWithTitle());
        assertEquals("House", md.getRelativePathWithTitle());

        // episode specicif stuff
        assertEquals(2, md.getSeasonNumber());
        assertEquals(7, md.getEpisodeNumber());
        assertEquals("Hunting", md.getEpisodeName());
        assertTrue(md.getGuests().size() > 0);
        assertEquals("Currie Graham", md.getGuests().get(0).getName());

        assertTrue(md.getFanart().size() > 1); // should have more than just a
        for (IMediaArt ma: md.getFanart()) {
            System.out.println("Fanart: " + ma);
            assertTrue(ma.getDownloadUrl(), ma.getDownloadUrl().startsWith("http"));
        }

        // poster

        assertTrue(md.getGenres().size() > 0);
        assertTrue("Genres should have Drama but has: " + md.getGenres(), md.getGenres().contains("Drama"));

        assertEquals("tt0412142", md.getIMDBID());
        assertEquals("1408", md.getMediaProviderDataID());
        assertEquals("tmdb", md.getMediaProviderID());
        assertEquals("House", md.getMediaTitle());
        assertEquals(MediaType.TV.sageValue(), md.getMediaType());
        assertEquals(DateUtils.parseDate("2005-11-22").getTime(), md.getOriginalAirDate().getTime());
        assertEquals("TVM", md.getParentalRating());
        assertEquals("TVM", md.getRated());

        assertEquals(0, md.getYear());
        // no extended ratings in tmdb
        // assertTrue(md.getExtendedRatings().length()>4);
        assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("44"), md.getRunningTime());
        assertEquals("House", md.getRelativePathWithTitle());
        assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);
        assertTrue(md.getWriters().size() > 0);
    }

    private void testTVDBMetadataLargeSeasons(SearchQuery query) throws Exception {
        List<IMetadataSearchResult> results = mgr.search("tmdb", query);
        //System.out.println("***testTVDBMetadata*** RESULTS:" + results);
        assertTrue("Search for returned nothing!", results.size() > 0);

        //System.out.println("***testTVDBMetadata*** QUERY:" + query);

        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        assertEquals(1989, result.getYear());
        assertEquals("456", result.getId());
        assertEquals("tmdb", result.getProviderId());
        assertEquals(MediaType.TV, result.getMediaType());
        assertEquals("The Simpsons", result.getTitle());

        // get the metadata, validate it
        //System.out.println("***testTVDBMetadata*** RESULT:" + result);
        IMetadata md = mgr.getMetdata(result);
        //System.out.println("***testTVDBMetadata*** MD EpisodeName:" + md.getEpisodeName());
        //System.out.println("***testTVDBMetadata*** MD MediaTitle:" + md.getMediaTitle());
        assertEquals("The Simpsons", md.getMediaTitle());

        assertNotNull(md.getDescription());

        // episode specicif stuff
        assertEquals(2, md.getSeasonNumber());
        assertEquals(3, md.getEpisodeNumber());
        assertEquals("Treehouse of Horror", md.getEpisodeName());
    }

    @Test
    public void testTvSeriesInfo() throws MetadataException {
        IMetadataProvider prov = mgr.getProvider("tmdb");
        assertTrue(prov instanceof TMDBMetadataProvider);
        TMDBMetadataProvider tv = (TMDBMetadataProvider) prov;
        IMetadataSearchResult res = mgr.createResultForId("tmdb", "1408");
        ISeriesInfo info = tv.getTmdbtv().getSeriesInfo(res.getId());

        assertNull("Series ID Must never have a value, unless it is a sagetv series info id", info.getSeriesInfoID());

        // show is cancelled, so no AirDOW
        //assertEquals("Monday", info.getAirDOW());
        // assertEquals("9:00 PM", info.getAirHrMin());
        assertEquals("TVM", info.getContentRating());
        assertTrue(info.getDescription() != null && info.getDescription().length() > 0);
        // assertEquals(null, info.getFinaleDate());
        // assertEquals(, info.getHistory());
        assertTrue(((String) info.getImage()).length() > 0);
        assertEquals("FOX", info.getNetwork());
        assertTrue(info.getPremiereDate().length() > 0);
        assertEquals("House", info.getTitle());
        assertTrue(info.getUserRating() > 30);
        //assertEquals("EP00688359", info.getZap2ItID());
        assertTrue(info.getGenres().size() > 0);
        assertTrue(info.getCast().size() > 0);

        for (String g : info.getGenres()) {
            System.out.println("Genre: " + g);
        }

        for (ICastMember cm : info.getCast()) {
            System.out.println("Cast: " + cm);
        }
    }

    @Test
    public void testTVDBProblemMatchers() {
        testTVDBTitle("American Dad!", "1433", "American Dad!");
        testTVDBTitle("American Dad", "1433", "American Dad!");
    }

    private void testTVDBTitle(String search, String resultId, String resultTitle) {
        try {
            SearchQuery query = new SearchQuery(MediaType.TV, search, null);
            query.set(SearchQuery.Field.SEASON, "1");
            query.set(SearchQuery.Field.EPISODE, "1");

            List<IMetadataSearchResult> results = mgr.search("tmdb", query);
            assertTrue("Search for returned nothing!", results.size() > 0);

            // ensure we get twilight zone from 1985
            IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
            assertEquals(resultId, result.getId());
            assertEquals("tmdb", result.getProviderId());
            assertEquals(MediaType.TV, result.getMediaType());
            assertEquals(resultTitle, result.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage() + " -- " + search);
        }
    }

    @Test
    public void testDefaults() {
        List<IMetadataProvider> provs = Phoenix.getInstance().getMetadataManager().getProviders(MediaType.TV);
        assertEquals("tmdb", provs.get(0).getInfo().getId());
        assertEquals("tvdb", provs.get(1).getInfo().getId());
        provs = Phoenix.getInstance().getMetadataManager().getProviders(MediaType.MOVIE);
        assertEquals("tmdb", provs.get(0).getInfo().getId());
    }
}
