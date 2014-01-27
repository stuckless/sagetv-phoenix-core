package sagex.phoenix.metadata.search;

import java.io.File;

public class MovieScraperManager extends ScraperManager {
	public MovieScraperManager(File systemDir, File userDir) {
		super("Movie", systemDir, userDir);
	}

	@Override
	protected IFilenameScraper loadXmbcScraper(ConfigurationType type, File file) throws Exception {
		return new XbmcMovieFilenameScraper(file);
	}

	@Override
	public void loadConfigurations() {
		super.loadConfigurations();
		
		// now add in our default scraper
		addScraper(ConfigurationType.System, new DefaultMovieFilenameScraper());
	}
}
