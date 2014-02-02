package sagex.phoenix.metadata.search;

import java.io.File;
import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.scrapers.xbmc.XbmcScraperProcessor;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

public class XbmcMovieFilenameScraper extends XbmcFilenameScraper {
	public XbmcMovieFilenameScraper(File scraperFile) throws Exception {
		super(scraperFile);
	}

	@Override
	public SearchQuery createSearchQuery(IMediaFile res, Hints hints) {
		// important, or else the filename parser will find %20 in the file
		// names, not good.
		String filenameUri = null;

		try {
			filenameUri = URLDecoder.decode(PathUtils.getLocation(res));
		} catch (Throwable t) {
			filenameUri = PathUtils.getLocation(res);
		}
		log.debug("Using Movie Scrapers to find a query for: " + filenameUri);
		SearchQuery q = new SearchQuery(hints);
		q.setMediaType(MediaType.MOVIE);

		String args[] = new String[] { "", filenameUri };
		XbmcScraperProcessor proc = new XbmcScraperProcessor(scraper);
		String title = proc.executeFunction("GetTitle", args);
		if (StringUtils.isEmpty(title)) {
			return null;
		}

		title = title.trim();
		log.debug("Found a Movie: " + title + " for " + filenameUri);
		q.set(Field.RAW_TITLE, title);

		// get the year
		String year = proc.executeFunction("GetYear", args);
		if (StringUtils.isEmpty(year)) {
			log.warn("Movie Scraper " + scraper + " failed to parse year for: " + filenameUri);
		} else {
			q.set(SearchQuery.Field.YEAR, year.trim());
		}

		ScraperUtils.adjustTitleWithCD(q);

		return q;
	}

}
