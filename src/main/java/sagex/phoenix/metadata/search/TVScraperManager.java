package sagex.phoenix.metadata.search;

import java.io.File;

public class TVScraperManager extends ScraperManager {

    public TVScraperManager(File systemDir, File userDir) {
        super("TV", systemDir, userDir);
    }

    @Override
    protected IFilenameScraper loadXmbcScraper(ConfigurationType type, File file) throws Exception {
        return new XbmcTVFilenameScraper(file);
    }
}
