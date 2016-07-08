package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.search.FileMatcher;
import sagex.phoenix.metadata.search.FileMatcherManager;
import sagex.phoenix.metadata.search.ID;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import sagex.phoenix.vfs.sage.SageMediaFile;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;

public class TestSearchQuery {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testSearchQuery() {
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File("/tmp/tv/House S02E03.avi"));
        SearchQuery q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        System.out.println("Query: " + q);
        verify(q, MediaType.TV, "House", "02", "03", null, null);

        FileMatcher fm = new FileMatcher();
        fm.setFileRegex("[\\/]SATC");
        fm.setMediaType(MediaType.TV);
        fm.setMetadata(new ID("tvdb", "88231"));
        fm.setTitle("Sex and the City");
        Phoenix.getInstance().getMediaTitlesManager().addMatcher(fm);
        mf = (IMediaFile) FileResourceFactory.createResource(new File("/tmp/tv/SATC S02E03.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        System.out.println("Query: " + q);
        verify(q, MediaType.TV, "Sex and the City", "02", "03", "tvdb", "88231");
    }

    private void verify(SearchQuery q, MediaType mt, String title, String season, String episode, String prov, String id) {
        assertEquals(mt, q.getMediaType());
        assertEquals(title, q.get(Field.RAW_TITLE));
        assertEquals(season, q.get(Field.SEASON));
        assertEquals(episode, q.get(Field.EPISODE));
        assertEquals(prov, q.get(Field.PROVIDER));
        assertEquals(id, q.get(Field.ID));
    }

    @SuppressWarnings("unused")
    private void verify(SearchQuery q, MediaType mt, Object... fields) {
        assertEquals(mt, q.getMediaType());

        for (int i = 0; i < fields.length; i += 2) {
            assertEquals("Field: " + fields[i], fields[i + 1], q.get((Field) fields[i]));
        }
    }

    @Test
    public void testFileMatcherBuilder() {
        FileMatcherManager mgr = new FileMatcherManager(new File(InitPhoenix.PROJECT_ROOT,"src/test/java/test/junit"), new File(
                InitPhoenix.PHOENIX_HOME, "userdata/Phoenix/scrapers"));
        mgr.loadConfigurations();
        assertEquals(4, mgr.getFileMatchers().size());

        FileMatcher fm = mgr.getFileMatchers().get(0);
        assertEquals("Babylon 5", fm.getTitle());
        assertEquals("tvdb", fm.getMetadata().getName());
        assertEquals("7072", fm.getMetadata().getValue());
        assertEquals("1993", fm.getYear());
        assertEquals(MediaType.TV, fm.getMediaType());
        assertEquals("[\\\\/]Babylon\\s*5[\\\\/]", fm.getFileRegex().pattern());
        System.out.println("File Matchers Passed");
    }

    @Test
    public void testMovieMatchers() {
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File(
                "../../target/junit/testing/Time.Traveller.1967.dvdrip.avi"));
        SearchQuery q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "Time Traveller", Field.CLEAN_TITLE, "Time Traveller", Field.YEAR, "1967");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Big Loser (2011) DVDRip.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "Big Loser", Field.CLEAN_TITLE, "Big Loser", Field.YEAR, "2011");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Big Loser (2011) DVDRip.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "Big Loser", Field.CLEAN_TITLE, "Big Loser", Field.YEAR, "2011");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Resident Evil[2010]DVDRip[eng].avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "Resident Evil", Field.CLEAN_TITLE, "Resident Evil", Field.YEAR, "2010");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Finding Nemo 2001 DvdRip 720p.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "Finding Nemo", Field.CLEAN_TITLE, "Finding Nemo", Field.YEAR, "2001");

        mf = (IMediaFile) FileResourceFactory
                .createResource(new File("../../target/junit/testing/Total Recall 2010 1998 DvdRip 720p.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "Total Recall 2010", Field.CLEAN_TITLE, "Total Recall", Field.YEAR, "1998");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/I'm OK & That's The Truth 2010.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "I'm OK & That's The Truth", Field.CLEAN_TITLE, "I'm OK & That's The Truth",
                Field.YEAR, "2010");

        mf = (IMediaFile) FileResourceFactory.createResource(new File(
                "../../target/junit/testing/Savages.2012.720p.bluray.x264-sparks.mkv"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "Savages", Field.CLEAN_TITLE, "Savages", Field.YEAR, "2012");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/R.I.P.D (2013).iso"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "R.I.P.D", Field.CLEAN_TITLE, "R.I.P.D", Field.YEAR, "2013");
    }

    @Test
    public void testTVMatchers() {
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Lost Again S01E02.avi"));
        SearchQuery q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Lost Again", Field.CLEAN_TITLE, "Lost Again", Field.SEASON, "01", Field.EPISODE,
                "02");

        mf = (IMediaFile) FileResourceFactory
                .createResource(new File("../../target/junit/testing/BigBangTheory-NewEpisode-000000-0.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Big Bang Theory", Field.CLEAN_TITLE, "Big Bang Theory", Field.EPISODE_TITLE,
                "New Episode");

        mf = (IMediaFile) FileResourceFactory
                .createResource(new File("../../target/junit/testing/BigBangTheory-NewEpisode-000000-0.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Big Bang Theory", Field.CLEAN_TITLE, "Big Bang Theory", Field.EPISODE_TITLE,
                "New Episode");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/SeriesTitle-123456-0.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Series Title", Field.CLEAN_TITLE, "Series Title");

//        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Series Title -- Episode Title.avi"));
//        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
//        verify(q, MediaType.TV, Field.RAW_TITLE, "Series Title", Field.CLEAN_TITLE, "Series Title", Field.EPISODE_TITLE,
//                "Episode Title");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Lost 01x02.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Lost", Field.CLEAN_TITLE, "Lost", Field.SEASON, "01", Field.EPISODE, "02");

//        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Lost s01d02.avi"));
//        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
//        verify(q, MediaType.TV, Field.RAW_TITLE, "Lost", Field.CLEAN_TITLE, "Lost", Field.SEASON, "01", Field.DISC, "02");
//
//        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Lost Season 03 Disc 04.avi"));
//        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
//        verify(q, MediaType.TV, Field.RAW_TITLE, "Lost", Field.CLEAN_TITLE, "Lost", Field.SEASON, "03", Field.DISC, "4");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Lost 2009-11-22.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Lost", Field.CLEAN_TITLE, "Lost", Field.EPISODE_DATE, "2009-11-22");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Lost 2009.11.22.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Lost", Field.CLEAN_TITLE, "Lost", Field.EPISODE_DATE, "2009-11-22");

        mf = (IMediaFile) FileResourceFactory.createResource(new File("../../target/junit/testing/Lost.2009-11-22.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Lost", Field.CLEAN_TITLE, "Lost", Field.EPISODE_DATE, "2009-11-22");

        mf = (IMediaFile) FileResourceFactory
                .createResource(new File("../../target/junit/testing/The X-Files-S06E20-three of a kind.avi"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "The X-Files", Field.CLEAN_TITLE, "The X-Files", Field.EPISODE, "20",
                Field.SEASON, "06");

        mf = (IMediaFile) FileResourceFactory.createResource(new File(
                "../../target/junit/testing/TheMiddle-S04E03-TheSecondAct-6260432-0.mp4"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "The Middle", Field.CLEAN_TITLE, "The Middle", Field.EPISODE, "03", Field.SEASON,
                "04");

        mf = (IMediaFile) FileResourceFactory.createResource(new File(
                "../../target/junit/testing/Marvel's Agents of S.H.I.E.L.D. - S01E01 - Pilot [WEBDL-1080p].mkv"));
        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Marvel's Agents of S.H.I.E.L.D", Field.CLEAN_TITLE,
                "Marvel's Agents of S.H.I.E.L.D", Field.EPISODE, "01", Field.SEASON, "01");
    }

    @Test
    public void testTVWithYears() throws Exception {
        IMediaFile mf = (IMediaFile) FileResourceFactory.createResource(new File(
                "../../target/junit/testing/Archer (2009)/Season 01/Archer (2009) - S01E01 - Mole Hunt.mkv"));
        SearchQuery q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Archer", Field.CLEAN_TITLE, "Archer", Field.SEASON, "01", Field.EPISODE,
                "01");

        verify(q, MediaType.TV, Field.RAW_TITLE, "Archer", Field.CLEAN_TITLE, "Archer", Field.SEASON, "01", Field.EPISODE,
                "01", Field.YEAR, "2009");

        FileMatcher fm = new FileMatcher();
        fm.setFileRegex(Pattern.compile("[\\\\/]Archer \\(2009\\)[\\\\/]"));
        fm.setMediaType(MediaType.TV);
        fm.setMetadata(new ID("tvdb", "110381"));
        fm.setTitle("Archer");
        fm.setYear("2009");
        Phoenix.getInstance().getMediaTitlesManager().addRegexMatcher(fm);

        q = Phoenix.getInstance().getSearchQueryFactory().createQueryFromFilename(mf, new Hints());
        verify(q, MediaType.TV, Field.RAW_TITLE, "Archer", Field.CLEAN_TITLE, "Archer", Field.SEASON, "01", Field.EPISODE, "01",
                Field.PROVIDER, "tvdb", Field.ID, "110381", Field.YEAR, "2009");
    }

    @Test
    public void TestAiringQuery() {
        SimpleStubAPI api = new SimpleStubAPI();
        api.overrideAPI("GetMediaFileID", 0); // should always return 0, since
        // we are dealing with Airings
        SageAPI.setProvider(api);

        Airing a = api.newAiring(2);
        a.put("IsTVFile", false);
        a.put("GetShowCategory", "Movie");
        a.put("GetShowExternalID", "MV0000001"); // populate API
        a.put("GetShowTitle", "The Terminator"); // populate API
        a.put("GetShowEpisode", "The Terminator"); // populate API
        a.put("GetMediaTitle", "The Terminator"); // populate API
        a.put("GetAiringTitle", "The Terminator"); // populate API
        a.put("GetOriginalAiringDate", Calendar.getInstance().getTime().getTime()); // populate
        // API
        a.put("GetShowYear", "1994"); // populate API
        a.put("GetSegmentFiles", null); // populate API
        a.METADATA.put("MediaType", null);

        SageMediaFile smf = new SageMediaFile(null, a);

        // test that the type is a movie if the genre is a movie
        SearchQuery q = Phoenix.getInstance().getSearchQueryFactory()
                .createSageFriendlyQuery(smf, Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "The Terminator", Field.CLEAN_TITLE, "The Terminator", Field.YEAR, "1994");

        // test that the type is a movie if the genre is a not a move but the
        // show id starts with mv
        q = Phoenix.getInstance().getSearchQueryFactory()
                .createSageFriendlyQuery(smf, Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
        verify(q, MediaType.MOVIE, Field.RAW_TITLE, "The Terminator", Field.CLEAN_TITLE, "The Terminator", Field.YEAR, "1994");

        // test that the type is a tv if we can't determine type from categor or
        // show id
        a.put("GetShowCategory", null);
        a.put("GetShowExternalID", "EP0000001"); // populate API
        q = Phoenix.getInstance().getSearchQueryFactory()
                .createSageFriendlyQuery(smf, Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions());
        verify(q, MediaType.TV, Field.RAW_TITLE, "The Terminator", Field.CLEAN_TITLE, "The Terminator", Field.YEAR, "1994");

        // test is recorded movie
        // MetadataUtil.isRecordedMovie((IMediaFile) r)
    }

    @Test
    public void TestIsRecordedMovie() {
        SimpleStubAPI api = new SimpleStubAPI();
        api.overrideAPI("GetMediaFileID", 0); // should always return 0, since
        // we are dealing with Airings
        SageAPI.setProvider(api);

        Airing a = api.newAiring(100);
        a.put("IsTVFile", false);
        a.put("GetShowCategory", "Movie");
        a.put("GetShowExternalID", "MV0000001"); // populate API
        a.put("GetShowTitle", "The Terminator"); // populate API
        a.put("GetShowEpisode", "The Terminator"); // populate API
        a.put("GetMediaTitle", "The Terminator"); // populate API
        a.put("GetAiringTitle", "The Terminator"); // populate API
        a.put("GetOriginalAiringDate", Calendar.getInstance().getTime().getTime()); // populate
        // API
        a.put("GetShowYear", "1994"); // populate API
        a.put("GetSegmentFiles", null); // populate API
        a.METADATA.put("MediaType", null);

        SageMediaFile smf = new SageMediaFile(null, a);
        assertTrue("Should be a recorded movie type", MetadataUtil.isRecordedMovie(smf));
    }

}
