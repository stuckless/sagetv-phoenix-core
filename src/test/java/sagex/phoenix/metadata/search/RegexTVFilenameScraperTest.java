package sagex.phoenix.metadata.search;

import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.impl.FileResourceFactory;
import test.InitPhoenix;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by seans on 08/07/16.
 */
public class RegexTVFilenameScraperTest {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void createSearchQuery() throws Exception {
        File file = new File(InitPhoenix.PROJECT_ROOT, "src/plugins/phoenix-core/STVs/Phoenix/scrapers/tv/filenames.regex");
        RegexFilenameScraper scraper = new RegexTVFilenameScraper(file);

        testAiring(scraper, "BrooklynNineNine-S03E21-MaximumSecurity-14895977-0.ts", "Brooklyn Nine Nine", "03", "21", "Maximum Security", "14895977");
        testAiring(scraper, "Castle-14123125-0.ts", "Castle", null, null, null, "14123125");
        testAiring(scraper, "CollegeBasketball-ArizonaatWashington-14584843-0.ts", "College Basketball", null, null, "Arizonaat Washington", "14584843");
        testAiring(scraper, "CSICyber-S02E14-FitandRun-14670697-0.ts", "CSI Cyber", "02", "14", "Fitand Run", "14670697");
        testAiring(scraper, "CSICyber-S02E14-FitandRun-14670697-0.ts", "CSI Cyber", "02", "14", "Fitand Run", "14670697");
        testAiring(scraper, "MarvelsAgentsofSHIELD-PurposeintheMachine-13995239-0.ts", "Marvels Agentsof SHIELD", null, null, "Purposeinthe Machine","13995239");

        testTV(scraper, "Bones S01E02.ts", "Bones", "01", "02", null, null, null);
        testTV(scraper, "Bones S01E02-03.ts", "Bones", "01", "02", "03", null, null);
        testTV(scraper, "Bones (2010) S01E02.ts", "Bones", "01", "02", null, "2010", null);
        testTV(scraper, "Bones (2010) S01E02-03.ts", "Bones", "01", "02", "03", "2010", null);
        testTV(scraper, "Bones 2010 S01E02.ts", "Bones", "01", "02", null, "2010", null);
        testTV(scraper, "Bones 2010 S01E02-03.ts", "Bones", "01", "02", "03", "2010", null);

        testTV(scraper, "Bones (2010) 01x02-03.ts", "Bones", "01", "02", "03", "2010", null);
        testTV(scraper, "Bones 1x2.ts", "Bones", "1", "2", null, null, null);
        testTV(scraper, "Bones - 1.02.ts", "Bones", "1", "02", null, null, null);

        testTV(scraper, "Bones 2010.04.05.ts", "Bones", null, null, null, null, "2010-04-05");
        testTV(scraper, "Bones 2010-04-05.ts", "Bones", null, null, null, null, "2010-04-05");

        testNoMatch(scraper, "Savages.2012.720p.bluray.x264-sparks.mkv");
    }

    private void testNoMatch(RegexFilenameScraper scraper, String name) {
        IMediaFile f = (IMediaFile) FileResourceFactory.createResource(new File(InitPhoenix.PHOENIX_HOME, name));
        Hints hints = new Hints();
        SearchQuery q = scraper.createSearchQuery(f, hints);
        assertNull("FAILED: for '" + name + "' -- SHOULD NOT MATCH", q);
    }

    private void testTV(RegexFilenameScraper scraper, String name, String show, String season, String episode, String episodeEnd, String year, String date) {
        IMediaFile f = (IMediaFile) FileResourceFactory.createResource(new File(InitPhoenix.PHOENIX_HOME, name));
        Hints hints = new Hints();
        SearchQuery q = scraper.createSearchQuery(f, hints);
        assertNotNull("FAILED: for '" + name + "'", q);
        assertEquals(show, q.getRawTitle());
        assertEquals(season, q.getSeason());
        assertEquals(episode, q.getEpisode());
        assertEquals(episodeEnd, q.getEpisodeRangeEnd());
        assertEquals(year, q.getYear());
        assertEquals(date, q.getEpisodeDate());
        System.out.println("PASSED: " + name);
    }

    private void testAiring(RegexFilenameScraper scraper, String name, String show, String season, String episode, String title, String airing) {
        IMediaFile f = (IMediaFile) FileResourceFactory.createResource(new File(InitPhoenix.PHOENIX_HOME, name));
        Hints hints = new Hints();
        SearchQuery q = scraper.createSearchQuery(f, hints);
        assertNotNull("FAILED: for '" + name + "'", q);
        assertEquals(airing, q.getAiringId());
        assertEquals(show, q.getRawTitle());
        assertEquals(title, q.getEpisodeTitle());
        assertEquals(season, q.getSeason());
        assertEquals(episode, q.getEpisode());
        System.out.println("PASSED: " + name);
    }
}