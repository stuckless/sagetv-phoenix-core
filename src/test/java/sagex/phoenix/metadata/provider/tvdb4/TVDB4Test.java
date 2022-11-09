package sagex.phoenix.metadata.provider.tvdb4;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.sources.TVMediaFilesSourceFactory;
import sagex.remote.json.JSONException;
import test.InitPhoenix;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class TVDB4Test {
    private Logger log = Logger.getLogger(this.getClass());
    static MetadataManager mgr;
    static TVDB4JsonHandler handler;


    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);

        mgr = Phoenix.getInstance().getMetadataManager();
        handler = new TVDB4JsonHandler();

    }

    @Test
    public void testFactoryLoading() {
        for (IMetadataProvider p : mgr.getProviders()) {
            System.out.println("Provider: " + p);
        }

        IMetadataProvider prov;

        prov = mgr.getProvider("tvdb");
        assertNotNull("Failed to load tvdb4 provider!", prov);
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
        if(validConfig()){
            SearchQuery query = new SearchQuery(MediaType.TV, "South Park", null);
            query.set(SearchQuery.Field.PROVIDER, "tvdb");
            //query.set(Field.ID, "75897");
            query.set(SearchQuery.Field.QUERY, "South Park");
            query.set(SearchQuery.Field.SEASON, "19");
            query.set(SearchQuery.Field.EPISODE, "4");

            List<IMetadataSearchResult> results = mgr.search("tvdb", query);
            for (IMetadataSearchResult r : results) {
                System.out.println("Result: " + r);
            }

            IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);

            IMetadata md = mgr.getMetdata(result);
            System.out.println(md.getMediaTitle());
            System.out.println(md.getEpisodeName());
            System.out.println(md.getDescription());
        }
    }

    @Test
    public void testTVDBBySeasonEpisode() throws Exception {
        if(validConfig()){
            SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
            query.set(SearchQuery.Field.SEASON, "2");
            query.set(SearchQuery.Field.EPISODE, "7");

            testTVDBMetadata(query);
        }
    }

    private Boolean validConfig(){
        //PIN no longer required so always return true
        return true;
        /*
        if(handler.hasPin()){
            System.out.println("TVDB4 tests can continue as TVDB4 PIN is available.");
            return true;
        }else{
            System.out.println("TVDB4 tests require a PIN to be set in the BeforeClass init. Skipping this test.");
            return false;
        }

         */
    }

    @Test
    public void testTVDBByID() throws Exception {
        if(validConfig()){
            SearchQuery query = new SearchQuery(MediaType.TV, "GGGGG", "2004");
            //SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
            query.set(SearchQuery.Field.PROVIDER, "tvdb");
            query.set(SearchQuery.Field.ID, "73255");

            query.set(SearchQuery.Field.SEASON, "2");
            query.set(SearchQuery.Field.EPISODE, "7");

            testTVDBMetadata(query);
        }
    }

    @Test
    public void testTVDBProblemMatchers2() {
        if(validConfig()){
            testTVDBTitle("America's Test Kitchen From Cook's Illustrated", "80297", "America's Test Kitchen From Cook's Illustrated");
        }
    }

    @Test
    public void testTVDBByTitle() throws Exception {
        if(validConfig()){
            SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
            query.set(SearchQuery.Field.EPISODE_TITLE, "Hunting");

            testTVDBMetadata(query);
        }
    }

    @Test
    public void testTVDBByTitlePartial() throws Exception {
        if(validConfig()){
            SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
            query.set(SearchQuery.Field.EPISODE_TITLE, "Hunt");

            testTVDBMetadata(query);
        }
    }

    @Test
    public void testTVDBSeriesFanartOnly() throws Exception {
        if(validConfig()){
            SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
            query.set(SearchQuery.Field.EPISODE_TITLE, "123ASDASD999DSSDSD");

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
            //assertTrue(result.getUrl()!=null && result.getUrl().contains("73255"));

            IMetadata md = mgr.getMetdata(result);
            assertTrue("Failed to get Series Only Fanart", md.getFanart().size() > 0);
            for (IMediaArt ma : md.getFanart()) {
                System.out.println("Fanart: " + ma.getDownloadUrl());
                assertTrue("Should not have season in fanart", ma.getSeason() == 0);
                assertTrue(ma.getDownloadUrl(), ma.getDownloadUrl().startsWith("http"));
            }
        }
    }

    @Test
    public void testTVDBByDate() throws Exception {
        if(validConfig()){
            SearchQuery query = new SearchQuery(MediaType.TV, "House", "2004");
            query.set(SearchQuery.Field.EPISODE_DATE, "2005-11-22");

            testTVDBMetadata(query);
        }
    }

    private void testTVDBMetadata(SearchQuery query) throws Exception {
        List<IMetadataSearchResult> results = mgr.search("tvdb", query);
        assertTrue("Search for returned nothing!", results.size() > 0);

        log.info("testTVDBMetadata: results:" + results);
        // ensure we get twilight zone from 1985
        IMetadataSearchResult result = MetadataSearchUtil.getBestResultForQuery(results, query);
        assertEquals(2004, result.getYear());
        assertEquals("73255", result.getId());
        assertEquals("tvdb", result.getProviderId());
        assertEquals(MediaType.TV, result.getMediaType());
        assertEquals("House", result.getTitle());
        // tvdb just passes the id as url, it's never used, so it's just for
        // reference
        //assertTrue(result.getUrl()!=null && result.getUrl().contains("73255"));

        // get the metadata, validate it
        IMetadata md = mgr.getMetdata(result);
        assertEquals("House", md.getMediaTitle());
        assertTrue(md.getActors().size() > 2);
        assertEquals("Hugh Laurie", md.getActors().get(2).getName());
        assertEquals("Gregory House", md.getActors().get(2).getRole());

        assertNotNull(md.getDescription());
        assertTrue(md.getDirectors().size() > 0);
        assertEquals("House", md.getRelativePathWithTitle());

        // episode specicif stuff
        assertEquals(2, md.getSeasonNumber());
        assertEquals(7, md.getEpisodeNumber());
        assertEquals("Hunting", md.getEpisodeName());
        assertTrue(md.getGuests().size() > 0);
        assertEquals("Currie Graham", md.getGuests().get(1).getName());

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
        assertEquals(2005, md.getYear());
        // no extended ratings in tmdb
        // assertTrue(md.getExtendedRatings().length()>4);
        assertEquals(MetadataSearchUtil.convertTimeToMillissecondsForSage("45"), md.getRunningTime());
        assertEquals("House", md.getRelativePathWithTitle());
        //assertTrue("Invalid User Rating: " + md.getUserRating(), md.getUserRating() > 0);
        assertTrue(md.getWriters().size() > 0);
    }

    @Test
    public void testTvSeriesInfo() throws MetadataException {
        if(validConfig()){
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
            assertEquals("FOX", info.getNetwork());
            assertTrue(info.getPremiereDate().length() > 0);
            assertEquals("House", info.getTitle());
            //no valid user rating so skip this
            //assertTrue(info.getUserRating() > 30);
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
    }

    @Test
    public void testTVDBProblemMatchers() {
        if(validConfig()){
            testTVDBTitle("American Dad!", "73141", "American Dad!");
            testTVDBTitle("American Dad", "73141", "American Dad!");
        }
    }

    private void testTVDBTitle(String search, String resultId, String resultTitle) {
        if(validConfig()){
            try {
                SearchQuery query = new SearchQuery(MediaType.TV, search, null);
                query.set(SearchQuery.Field.SEASON, "1");
                query.set(SearchQuery.Field.EPISODE, "1");

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
    }

    @Test
    public void testDefaults() {
        //default provider for TV is now back to tvdb as it is now free
        List<IMetadataProvider> provs = Phoenix.getInstance().getMetadataManager().getProviders(MediaType.TV);
        assertEquals("tvdb", provs.get(0).getInfo().getId());
        for(IMetadataProvider provider: provs){
            System.out.println("Available TV provider:" + provider.getInfo());
        }
        //default provider for MOVIES is tmdb as the only MOVIE provider
        provs = Phoenix.getInstance().getMetadataManager().getProviders(MediaType.MOVIE);
        assertEquals("tmdb", provs.get(0).getInfo().getId());
        for(IMetadataProvider provider: provs){
            System.out.println("Available Movie provider:" + provider.getInfo());
        }
    }

    @Test
    public void testEpisodes() {

        try {
            List<IMetadata> episodes = handler.GetEpisodes("77072");
            log.info("****TVEpisodes:" + episodes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
