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
public class RegexMovieFilenameScraperTest {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testMovieRegex() {
        File file = new File(InitPhoenix.PROJECT_ROOT, "src/plugins/phoenix-core/STVs/Phoenix/scrapers/movies/filenames.regex");
        RegexFilenameScraper scraper = new RegexMovieFilenameScraper(file);
        testMovie(scraper, "Finding Nemo (2012).mkv", "Finding Nemo", "2012");
        testMovie(scraper, "This and That 2014.ts", "This and That", "2014");
        testMovie(scraper, "This and That - 2014.ts", "This and That", "2014");
        testMovie(scraper, "My Movie.ts", "My Movie", null);
        testMovie(scraper, "Warcraft 1080p HDRip KORSUB x264 AAC2 0-STUTTERSHIT.mkv", "Warcraft", null);
        testMovie(scraper, "Savages.2012.720p.bluray.x264-sparks.mkv", "Savages", "2012");
    }

    private void testMovie(RegexFilenameScraper scraper, String name, String title, String year) {
        System.out.println("Testing: " + name);
        IMediaFile f = (IMediaFile) FileResourceFactory.createResource(new File(InitPhoenix.PHOENIX_HOME, name));
        Hints hints = new Hints();
        SearchQuery q = scraper.createSearchQuery(f, hints);
        assertNotNull("Search Query failed for '" + name + "'", q);
        assertEquals(title, q.getRawTitle());
        assertEquals(year, q.getYear());
    }
}