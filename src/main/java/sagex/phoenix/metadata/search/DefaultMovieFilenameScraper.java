package sagex.phoenix.metadata.search;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

/**
 * This scraper that is used on movies, if no other scraper in the system can
 * produce a value.
 * 
 * @author sean
 */
public class DefaultMovieFilenameScraper implements IFilenameScraper {
	private Logger log = Logger.getLogger(this.getClass());

	public DefaultMovieFilenameScraper() {
	}

	@Override
	public String getId() {
		return "defaultmovie";
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}

	@Override
	public SearchQuery createSearchQuery(IMediaFile res, Hints hints) {
		String filenameUri = null;

		try {
			filenameUri = URLDecoder.decode(PathUtils.getLocation(res));
		} catch (Throwable t) {
			filenameUri = PathUtils.getLocation(res);
		}
		
		SearchQuery q = new SearchQuery(hints);
		
		// scrapers failed, so let's do it this way.
		String title = PathUtils.getBasename(res);
		log.warn("Using DEFAULT scraper for: "
				+ filenameUri + ", will use the following movie title: "
				+ title);
		q.set(Field.RAW_TITLE, title);

		// look for a year pattern...
		Pattern YearPat = Pattern.compile("[12]{1}[0-9]{3}");
		Matcher m = YearPat.matcher(title);
		while (m.find()) {
			int year = NumberUtils.toInt(title.substring(m.start(), m.end()), 0);
			// only add the year if it's a valid year
			// sometimes it might find 1080p and think that it's a year
			if (year>0 && year != 1080) {
				q.set(Field.YEAR, title.substring(m.start(), m.end()));
				q.set(Field.RAW_TITLE, title.substring(0, m.start()));
			}
		}

		ScraperUtils.adjustTitleWithCD(q);
		return q;
	}
}
