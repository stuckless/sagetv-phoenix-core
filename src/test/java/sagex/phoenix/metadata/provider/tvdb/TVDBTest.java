package sagex.phoenix.metadata.provider.tvdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.junit.lib.Utils.makeDir;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProvider;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.ITVMetadataProvider;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataManager;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.impl.AlbumInfo;
import sagex.phoenix.vfs.sage.SageMediaFile;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

public class TVDBTest {
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

        prov = mgr.getProvider("tvdb");
        assertNotNull("Failed to load tvdb provider!", prov);
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
        query.set(Field.PROVIDER, "tvdb");
        //query.set(Field.ID, "75897");
        query.set(Field.QUERY, "South Park");
        query.set(Field.SEASON, "19");
        query.set(Field.EPISODE, "4");

        List<IMetadataSearchResult> results = mgr.search("tvdb", query);
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
        query.set(Field.SEASON, "2");
        query.set(Field.EPISODE, "7");

        testTVDBMetadata(query);
    }

    @Test
    public void testTVDBByID() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "GGGGG", "2004");
        query.set(Field.PROVIDER, "tvdb");
        query.set(Field.ID, "73255");

        query.set(Field.SEASON, "2");
        query.set(Field.EPISODE, "7");

        testTVDBMetadata(query);
    }

    @Test
    public void testTVDBByTitle() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
        query.set(Field.EPISODE_TITLE, "Hunting");

        testTVDBMetadata(query);
    }

    @Test
    public void testTVDBSeriesFanartOnly() throws Exception {
        SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
        query.set(Field.EPISODE_TITLE, "123ASDASD999DSSDSD");

        List<IMetadataSearchResult> results = mgr.search("tvdb", query);
        assertTrue("Search for returned nothing!", results.size() > 0);
        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        assertEquals(2004, result.getYear());
        assertEquals("73255", result.getId());
        assertEquals("tvdb", result.getProviderId());
        assertEquals(MediaType.TV, result.getMediaType());
        assertEquals("House", result.getTitle());
        // tvdb just passes the id as url, it's never used, so it's just for
        // reference
        assertTrue(result.getUrl()!=null && result.getUrl().contains("73255"));

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
        query.set(Field.EPISODE_DATE, "2005-11-22");

        testTVDBMetadata(query);
    }

    private void testTVDBMetadata(SearchQuery query) throws Exception {
        List<IMetadataSearchResult> results = mgr.search("tvdb", query);
        assertTrue("Search for returned nothing!", results.size() > 0);

        // ensure we get twilight zone from 1985
        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        assertEquals(2004, result.getYear());
        assertEquals("73255", result.getId());
        assertEquals("tvdb", result.getProviderId());
        assertEquals(MediaType.TV, result.getMediaType());
        assertEquals("House", result.getTitle());
        // tvdb just passes the id as url, it's never used, so it's just for
        // reference
        assertTrue(result.getUrl()!=null && result.getUrl().contains("73255"));

        // get the metadata, validate it
        IMetadata md = mgr.getMetdata(result);
        assertEquals("House", md.getMediaTitle());
        assertTrue(md.getActors().size() > 2);
        assertEquals("Hugh Laurie", md.getActors().get(0).getName());
        assertEquals("Dr. Gregory House", md.getActors().get(0).getRole());

        assertNotNull(md.getDescription());
        assertTrue(md.getDirectors().size() > 0);
        assertEquals("House", md.getRelativePathWithTitle());

        // episode specicif stuff
        assertEquals(2, md.getSeasonNumber());
        assertEquals(7, md.getEpisodeNumber());
        assertEquals("Hunting", md.getEpisodeName());
        assertTrue(md.getGuests().size() > 0);
        assertEquals("Hamilton Mitchell", md.getGuests().get(0).getName());

        assertTrue(md.getFanart().size() > 1); // should have more than just a
        for (IMediaArt ma: md.getFanart()) {
            System.out.println("Fanart: " + ma);
            assertTrue(ma.getDownloadUrl(), ma.getDownloadUrl().startsWith("http"));
        }

        // poster

        assertTrue(md.getGenres().size() > 0);
        assertTrue("Genres should have Drama but has: " + md.getGenres(), md.getGenres().contains("Drama"));

        assertEquals("tt0606027", md.getIMDBID());
        assertEquals("73255", md.getMediaProviderDataID());
        assertEquals("tvdb", md.getMediaProviderID());
        assertEquals("House", md.getMediaTitle());
        assertEquals(MediaType.TV.sageValue(), md.getMediaType());
        assertEquals(DateUtils.parseDate("2005-11-22").getTime(), md.getOriginalAirDate().getTime());
        assertEquals("TV14", md.getParentalRating());
        assertNull(md.getRated());
        assertEquals(0, md.getYear());
        // no extended ratings in tmdb
        // assertTrue(md.getExtendedRatings().length()>4);
        assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("45"), md.getRunningTime());
        assertEquals("House", md.getRelativePathWithTitle());
        assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);
        assertTrue(md.getWriters().size() > 0);
    }

    @Test
    public void testTvSeriesInfo() throws MetadataException {
        IMetadataProvider prov = mgr.getProvider("tvdb");
        assertTrue(prov instanceof ITVMetadataProvider);
        ITVMetadataProvider tv = (ITVMetadataProvider) prov;
        IMetadataSearchResult res = mgr.createResultForId("tvdb", "73255");
        ISeriesInfo info = tv.getSeriesInfo(res.getId());

        assertNull("Series ID Must never have a value, unless it is a sagetv series info id", info.getSeriesInfoID());

        // show is cancelled, so no AirDOW
        //assertEquals("Monday", info.getAirDOW());
        // assertEquals("9:00 PM", info.getAirHrMin());
        assertEquals("TV14", info.getContentRating());
        assertTrue(info.getDescription() != null && info.getDescription().length() > 0);
        // assertEquals(null, info.getFinaleDate());
        // assertEquals(, info.getHistory());
        assertTrue(((String) info.getImage()).length() > 0);
        assertEquals("FOX (US)", info.getNetwork());
        assertTrue(info.getPremiereDate().length() > 0);
        assertEquals("House", info.getTitle());
        assertTrue(info.getUserRating() > 30);
        assertEquals("EP00688359", info.getZap2ItID());
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
        testTVDBTitle("American Dad!", "73141", "American Dad!");
        testTVDBTitle("American Dad", "73141", "American Dad!");
    }

    private void testTVDBTitle(String search, String resultId, String resultTitle) {
        try {
            SearchQuery query = new SearchQuery(MediaType.TV, search, null);
            query.set(Field.SEASON, "1");
            query.set(Field.EPISODE, "1");

            List<IMetadataSearchResult> results = mgr.search("tvdb", query);
            assertTrue("Search for returned nothing!", results.size() > 0);

            // ensure we get twilight zone from 1985
            IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
            assertEquals(resultId, result.getId());
            assertEquals("tvdb", result.getProviderId());
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
        assertEquals("tvdb", provs.get(0).getInfo().getId());
        provs = Phoenix.getInstance().getMetadataManager().getProviders(MediaType.MOVIE);
        assertEquals("tmdb", provs.get(0).getInfo().getId());
    }
}
