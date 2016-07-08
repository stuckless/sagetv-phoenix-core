package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.junit.lib.TestUtil.makeDir;

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

public class TestMetadataProviderFactory {
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
        assertEquals(DateUtils.parseDate("2010-05-07").getTime(), md.getOriginalAirDate().getTime());
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
        query.set(Field.PROVIDER, "tmdb");
        query.set(Field.ID, "10138");

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
        assertEquals(DateUtils.parseDate("2010-05-07").getTime(), md.getOriginalAirDate().getTime());
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
        assertEquals("73255", result.getUrl());

        IMetadata md = mgr.getMetdata(result);
        assertTrue("Failed to get Series Only Fanart", md.getFanart().size() > 0);
        for (IMediaArt ma : md.getFanart()) {
            System.out.println("Fanart: " + ma.getDownloadUrl());
            assertTrue("Should not have season in fanart", ma.getSeason() == 0);
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
        assertEquals("73255", result.getUrl());

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
    public void testHTBackdropsMusic() throws MetadataException {
        SearchQuery q = new SearchQuery(Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
        q.setMediaType(MediaType.MUSIC);
        q.set(Field.ARTIST, "Madonna");

        List<IMetadataSearchResult> results = Phoenix.getInstance().getMetadataManager().search("htb", q);
        assertTrue(results.size() > 0);
        for (IMetadataSearchResult r : results) {
            System.out.printf("result: %s %s %s\n", r.getTitle(), r.getId(), r.getScore());
        }

        IMetadata md = Phoenix.getInstance().getMetadataManager().getMetdata(results.get(0));
        assertTrue(md.getFanart().size() > 1);
        for (IMediaArt f : md.getFanart()) {
            System.out.printf("fanart: %s %s\n", f.getType(), f.getDownloadUrl());
        }

        System.out.println("---------------------");

        // now check if we get a hit using search by id
        q = new SearchQuery(Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
        q.setMediaType(MediaType.MUSIC);
        q.set(Field.ID, "79239441-bfd5-4981-a70c-55c3f15c1287");

        results = Phoenix.getInstance().getMetadataManager().search("htb", q);
        assertEquals(results.size(), 1);
        for (IMetadataSearchResult r : results) {
            System.out.printf("result: %s %s %s\n", r.getTitle(), r.getId(), r.getScore());
            assertTrue(r.getScore() >= 1.0f);
        }

        md = Phoenix.getInstance().getMetadataManager().getMetdata(results.get(0));
        assertTrue(md.getFanart().size() > 1);
        for (IMediaArt f : md.getFanart()) {
            System.out.printf("fanart: %s %s\n", f.getType(), f.getDownloadUrl());
        }
        assertEquals("Madonna", md.getMediaTitle());
    }

    @Test
    public void testSaveMusicFanart() throws MetadataException, InterruptedException {
        SimpleStubAPI api = new SimpleStubAPI();

        Airing smf = api.newMediaFile(1);
        smf.put("IsMusicFile", true);
        smf.put("IsTVFile", false); // consider overriding api;
        // {IsAiringObject=true, IsMusicFile=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=1}:
        // test.junit.lib.SimpleStubAPI$Airing,
        smf.put("GetShowEpisode", ""); // consider overriding api;
        // {IsAiringObject=true,
        // IsMusicFile=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=1}:
        // test.junit.lib.SimpleStubAPI$Airing,
        smf.put("GetShowTitle", ""); // consider overriding api;
        // {IsAiringObject=true,
        // IsMusicFile=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=1}:
        // test.junit.lib.SimpleStubAPI$Airing,
        smf.put("GetMediaTitle", "Material Girl"); // consider overriding api;
        // {IsAiringObject=true,
        // IsMusicFile=true,
        // IsMediaFileObject=true,
        // GetMediaFileID=1}:
        // test.junit.lib.SimpleStubAPI$Airing,

        api.overrideAPI("GetAlbumForFile", new AlbumInfo());
        api.overrideAPI("GetAlbumArtist", "Madonna"); // consider overriding
        // api;
        // sagex.phoenix.vfs.impl.AlbumInfo@272b72f4:
        // sagex.phoenix.vfs.impl.AlbumInfo,
        api.overrideAPI("GetAlbumName", "Like A Virgin"); // consider overriding
        // api;
        // sagex.phoenix.vfs.impl.AlbumInfo@272b72f4:
        // sagex.phoenix.vfs.impl.AlbumInfo,

        SageAPI.setProvider(api);

        IMediaFile mf = new SageMediaFile(null, smf);
        assertTrue("not a music file", mf.isType(MediaResourceType.MUSIC.value()));
        Hints options = Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions();
        SearchQuery q = Phoenix.getInstance().getMetadataManager().createQuery(mf, options);
        assertEquals("not a music search", q.getMediaType(), MediaType.MUSIC);
        assertEquals(q.get(Field.RAW_TITLE), "Material Girl");
        assertEquals(q.get(Field.ARTIST), "Madonna");
        assertEquals(q.get(Field.ALBUM), "Like A Virgin");

        assertTrue("Can't accept music query??", Phoenix.getInstance().getMetadataManager().canScanMediaFile(mf, options));

        // setup central fanart
        File fanartDir = makeDir("test/FanartFolder");
        phoenix.fanart.SetFanartCentralFolder(fanartDir.getAbsolutePath());
        String sdir = phoenix.fanart.GetFanartCentralFolder();
        System.out.println("Central Folder: " + sdir);

        // automatic update, and then check for fanart
        Phoenix.getInstance().getMetadataManager().automaticUpdate(mf, options);

        System.out.println("Sleeping while downloading...");
        Thread.currentThread().sleep(10 * 1000);

        // check files
        File music = new File(fanartDir, "Music/Madonna/Posters");
        assertTrue("Failed to create fanart dir: " + music, music.exists());
        File files[] = music.listFiles();
        assertTrue("failed to download fanart", files != null && files.length > 0);
        for (File f : files) {
            System.out.println(f);
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
