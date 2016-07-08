package sagex.phoenix.metadata.search;

import java.io.File;

public class MovieScraperManager extends ScraperManager {
    public MovieScraperManager(File systemDir, File userDir) {
        super("Movie", systemDir, userDir);
    }

    @Override
    protected IFilenameScraper loadRegexScraper(ConfigurationType type, File file) throws Exception {
        return new RegexMovieFilenameScraper(file);
    }
}
