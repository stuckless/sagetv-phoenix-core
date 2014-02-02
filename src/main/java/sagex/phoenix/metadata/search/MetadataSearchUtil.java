package sagex.phoenix.metadata.search;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProvider;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.Similarity;

public class MetadataSearchUtil {
	private static final Logger log = Logger.getLogger(MetadataSearchUtil.class);

	public static final String MOVIE_MEDIA_TYPE = "Movie";
	public static final String TV_MEDIA_TYPE = "TV";

	private static String compressedRegex = "[^a-zA-Z0-9]+";

	private static Pattern mpaaRatingParser = Pattern.compile("Rated\\s+([^ ]+).*");
	private static Pattern mpaaRatingParser2 = Pattern.compile("([^ ]+).*");
	public static final String IMDB_RUNNING_TIME_REGEX = "([0-9]+)(\\s+min)?";

	public static String parseMPAARating(String str) {
		if (str == null)
			return null;
		Matcher m = mpaaRatingParser.matcher(str);
		if (m.find()) {
			return m.group(1);
		} else {
			m = mpaaRatingParser2.matcher(str);
			if (m.find()) {
				return m.group(1);
			}
		}
		return null;
	}

	/**
	 * Given a metadata id, id:###, return 2 parts, the id, and the ####
	 * 
	 * if the id is not a valid id, then only a 1 element array will be
	 * returned.
	 * 
	 * @param idsagex
	 *            .phoenix.util.
	 * @return
	 */
	public static String[] getMetadataIdParts(String id) {
		if (id == null)
			return null;
		String parts[] = id.split(":");
		if (parts == null || parts.length != 2) {
			return new String[] { id };
		}
		return parts;
	}

	/**
	 * Return the best score for a title when compared to the search string. It
	 * uses 3 passes to find the best match. the first pass uses the matchTitle
	 * as is, and the second pass uses the matchTitle will non search characters
	 * removed, and the final uses a similarity based on a compressed titles
	 * 
	 * @param searchTitle
	 * @param matchTitle
	 * @return the best out of the 2 scored attempts
	 */
	public static float calculateScore(String searchTitle, String matchTitle) {
		float score1 = Similarity.getInstance().compareStrings(searchTitle, matchTitle);
		if (searchTitle == null || matchTitle == null)
			return score1;

		float score2 = Similarity.getInstance().compareStrings(searchTitle, SearchUtil.removeNonSearchCharacters(matchTitle));
		float score3 = Similarity.getInstance().compareStrings(searchTitle.replaceAll(compressedRegex, ""),
				matchTitle.replaceAll(compressedRegex, ""));

		return Math.max(score1, Math.max(score2, score3));
	}

	/**
	 * Return the best score for a title when compared to the search string. It
	 * uses 3 passes to find the best match. the first pass uses the matchTitle
	 * as is, and the second pass uses the matchTitle will non search characters
	 * removed, and the 3rd pass uses a compressed title search.
	 * 
	 * Compressed Scoring is useful when you are comparing a Sage recording
	 * (csimiami to "CSI: Miami")
	 * 
	 * @param searchTitle
	 * @param matchTitle
	 * @return the best out of the 3 scored attempts
	 */
	public static float calculateCompressedScore(String searchTitle, String matchTitle) {
		float score1 = calculateScore(searchTitle, matchTitle);
		if (searchTitle == null || matchTitle == null)
			return score1;

		float score2 = Similarity.getInstance().compareStrings(searchTitle.replaceAll(compressedRegex, ""),
				matchTitle.replaceAll(compressedRegex, ""));
		return Math.max(score1, score2);
	}

	public static long convertTimeToMillissecondsForSage(String time) {
		return NumberUtils.toLong(time) * 60 * 1000;
	}

	public static long parseRunningTime(String in, String regex) {
		if (in == null || regex == null)
			return 0;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(in);
		if (m.find()) {
			return convertTimeToMillissecondsForSage(m.group(1));
		} else {
			log.warn("Could not find Running Time in " + in + "; using Regex: " + regex);
			return 0;
		}
	}

	private static Pattern p = Pattern.compile("([0-9\\.]+)(\\s*/\\s*10)?");

	/**
	 * Given a variety of Rating strings, then try to parse a rating value. A
	 * rating value is an integer from 0 to 100.
	 * 
	 * @param in
	 * @return
	 */
	public static int parseUserRating(String in) {
		if (StringUtils.isEmpty(in))
			return 0;

		int rating = 0;
		Matcher m = p.matcher(in);
		if (m.find()) {
			double d = NumberUtils.toDouble(m.group(1));
			if (d != 0d) {
				if (d > 1.0 && d <= 10.0) {
					rating = (int) ((d / 10) * 100);
				} else if (d <= 1.0) {
					rating = (int) (d * 100);
				} else {
					// assumer greater than 10
					rating = (int) d;
				}
			}
		}

		if (rating > 100 || rating < 0) {
			log.warn("Failed to parse a rating from Rating String: " + in + "; Parsed rating value: " + rating
					+ "; Resetting rating value to 0");
			rating = 0;
		}

		return rating;
	}

	public static String getBareTitle(String name) {
		if (name != null)
			return name.replaceAll("[^A-Za-z0-9']", " ");
		return name;
	}

	public static void copySearchQueryToSearchResult(SearchQuery query, IMetadataSearchResult sr) {
		for (SearchQuery.Field f : SearchQuery.Field.values()) {
			if (f == SearchQuery.Field.QUERY)
				continue;
			String s = query.get(f);
			if (!StringUtils.isEmpty(s)) {
				sr.getExtra().put(f.name(), s);
			}
		}
	}

	public static List<IMetadataSearchResult> searchById(IMetadataProvider prov, SearchQuery query, String id) {
		log.debug("searchById() for: " + query);
		MediaSearchResult res = new MediaSearchResult(query.getMediaType(), prov.getInfo().getId(), id, query.get(Field.RAW_TITLE),
				NumberUtils.toInt(query.get(Field.YEAR)), 1.0f);
		MetadataSearchUtil.copySearchQueryToSearchResult(query, res);

		// do the search by id...
		try {
			IMetadata md = prov.getMetaData(res);
			if (md == null)
				throw new Exception("metadata result was null.");
			res.setProviderId(query.get(Field.PROVIDER));
			res.setId(query.get(Field.ID));
			res.setMetadata(md);
			res.setMediaType(query.getMediaType());
			res.setScore(1.0f);
			res.setTitle(md.getMediaTitle());
			if (!StringUtils.isEmpty(query.get(Field.YEAR))) {
				res.setYear(DateUtils.parseYear(query.get(Field.YEAR)));
			} else {
				res.setYear(md.getYear());
			}
			res.setIMDBId(md.getIMDBID());
			res.setUrl(query.get(Field.ID));
			log.info("searchById() was sucessful for: " + id);
		} catch (Exception e) {
			log.warn("searchById() failed for: " + query, e);
			return null;
		}

		return Arrays.asList(new IMetadataSearchResult[] { res });
	}

	public static boolean hasMetadata(IMetadataSearchResult result) {
		return result != null && (result instanceof MediaSearchResult) && ((MediaSearchResult) result).getMetadata() != null;
	}

	public static IMetadata getMetadata(IMetadataSearchResult result) {
		if (result != null && (result instanceof MediaSearchResult)) {
			return ((MediaSearchResult) result).getMetadata();
		}
		return null;
	}

	public static IMetadataSearchResult getBestResultForQuery(List<IMetadataSearchResult> results, SearchQuery query) {
		IMetadataSearchResult res = null;

		String year = query.get(Field.YEAR);

		if (isGoodSearch(results)) {
			if (StringUtils.isEmpty(year)) {
				log.warn("The year was not passed in the query: " + query
						+ " we are returning the first good result.  Consider adding the year to the query for better matches.");
				res = results.get(0);
			} else {
				MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
				float goodScore = config.getGoodScoreThreshold();
				int intYear = NumberUtils.toInt(year, -1);
				// attempt to find a good match that also includes the right
				// year
				for (IMetadataSearchResult r : results) {
					if (r.getScore() >= goodScore && intYear == r.getYear()) {
						res = r;
						break;
					}
				}

				// couldn't find a year match, but there was a good hit without
				// a year
				if (res == null) {
					res = results.get(0);
				}
			}
		}

		return res;
	}

	/**
	 * Since results are ordered by score, this tests if the first result has
	 * good enough score to allow for automatic updating.
	 * 
	 * @param results
	 * @return true if at least 1 result has a good scrore
	 */
	public static boolean isGoodSearch(List<IMetadataSearchResult> results) {
		MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
		float goodScore = config.getGoodScoreThreshold();
		return results != null && (results.size() > 0 && results.get(0).getScore() >= goodScore);
	}
}
