package sagex.phoenix.metadata.provider.tvdb;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProvider;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.ITVMetadataProvider;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DOMUtils;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.url.IUrl;
import sagex.phoenix.util.url.UrlFactory;

public class TVDBItemParser {
	private static final Logger log = Logger.getLogger(TVDBItemParser.class);

	public static final String BANNERS_URL = "http://www.thetvdb.com/api/{0}/series/{1}/banners.xml";
	public static final String SERIES_URL = "http://www.thetvdb.com/api/{0}/series/{1}/{2}.xml";
	public static final String SEASON_EPISODE_URL = "http://www.thetvdb.com/api/{0}/series/{1}/default/{2}/{3}/{4}.xml";

	// Date format is YYYY-MM-DD
	public static final String EPISODE_BY_DATE_URL = "http://thetvdb.com/api/GetEpisodeByAirDate.php?apikey={0}&seriesid={1}&airdate={2}";
	public static final String EPISODE_BY_TITLE = "http://www.thetvdb.com/api/{0}/series/{1}/all/{2}.xml";

	private IMetadata md = null;
	private Document banners = null;
	private TVDBConfiguration config = null;

	private IMetadataSearchResult result = null;
	private IMetadataProvider provider;

	public TVDBItemParser(IMetadataProvider prov, IMetadataSearchResult result) {
		this.provider = prov;
		this.result = result;
		config = GroupProxy.get(TVDBConfiguration.class);
	}

	public IMetadata getMetadata() throws MetadataException {
		if (md == null) {
			try {
				log.debug("Getting Metadata for Result: " + result);

				// parse and fill
				md = MetadataProxy.newInstance();

				// set our provider info
				md.setMediaProviderID(provider.getInfo().getId());
				md.setMediaProviderDataID(result.getId());

				// update with the query args, and then overwrite if needed
				md.setSeasonNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.SEASON.name())));
				md.setEpisodeNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.EPISODE.name())));
				md.setDiscNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.DISC.name())));
				md.setOriginalAirDate(DateUtils.parseDate(result.getExtra().get(SearchQuery.Field.EPISODE_DATE.name())));
				md.setMediaType(result.getMediaType().sageValue());

				addSeriesInfo(md);

				String season = result.getExtra().get(SearchQuery.Field.SEASON.name());
				String episode = result.getExtra().get(SearchQuery.Field.EPISODE.name());
				String date = result.getExtra().get(SearchQuery.Field.EPISODE_DATE.name());
				String title = result.getExtra().get(SearchQuery.Field.EPISODE_TITLE.name());

				if (!StringUtils.isEmpty(season) && !StringUtils.isEmpty(episode)) {
					addSeasonEpisodeInfo(md, season, episode);
				}

				if (md.getEpisodeNumber() == 0 && !StringUtils.isEmpty(date)) {
					addSeasonEpisodeInfoByDate(md, date);
				}

				if (md.getEpisodeNumber() == 0 && !StringUtils.isEmpty(title)) {
					addSeasonEpisodeInfoByTitle(md, title);
				}

				// Commented out the Episode and disc # checks since we want to
				// allow series level fanart
				// if (md.getEpisodeNumber()==0) {
				// throw new
				// Exception("Cannot process TV without a valid season/episod; Result: "
				// + result);
				// }
				//
				// if (md.getDiscNumber()==0) {
				// // if it's not disc based, then check that we have a valid
				// series info
				// if (StringUtils.isEmpty(md.getEpisodeName()) ||
				// md.getEpisodeNumber()==0) {
				// throw new
				// Exception("Did not find a valid season and episode; Episode: "
				// + episode + "; Search Date: " + date + "; Search Title: " +
				// title);
				// }
				// } else {
				// // TODO: gather all the episode titles and add them to the
				// description
				// // TODO: set the episode title to be "23 episodes"
				// }

				// now add in banners, no point in doing it early
				addBanners(md, season);
			} catch (MetadataException me) {
				throw me;
			} catch (Throwable e) {
				throw new MetadataException("Failed while parsing series: " + result, e);
			}
		}

		return md;
	}

	private void addSeriesInfo(IMetadata md) throws Exception {
		ISeriesInfo info = ((ITVMetadataProvider) provider).getSeriesInfo(md.getMediaProviderDataID());
		// copy some of the series info to the IMetadata object
		md.getActors().addAll(info.getCast());
		md.setParentalRating(MetadataUtil.fixContentRating(MediaType.TV, info.getContentRating()));
		md.getGenres().addAll(info.getGenres());
		md.setUserRating(info.getUserRating());

		md.setRelativePathWithTitle(info.getTitle());
		md.setMediaTitle(info.getTitle());

		md.setRunningTime(info.getRuntime());
	}

	private void updateMetadataFromUrl(IMetadata md, String episodeUrl) throws Exception {
		log.info("TVDB Episode: " + episodeUrl);
		IUrl url = UrlFactory.newUrl(episodeUrl);
		Document doc = DOMUtils.parseDocument(url);
		Element el = DOMUtils.getElementByTagName(doc.getDocumentElement(), "Episode");
		updateMetadataFromElement(md, el);
	}

	private void updateMetadataFromElement(IMetadata md, Element el) {
		md.setSeasonNumber(NumberUtils.toInt(DOMUtils.getElementValue(el, "SeasonNumber")));
		md.setEpisodeNumber(NumberUtils.toInt(DOMUtils.getElementValue(el, "EpisodeNumber")));
		md.setEpisodeName(sagex.phoenix.util.StringUtils.unquote(DOMUtils.getElementValue(el, "EpisodeName")));

		// actually this is redundant because the tvdb is already YYYY-MM-DD,
		// but this will
		// ensure that we are safe if out internal mask changes
		md.setOriginalAirDate(DateUtils.parseDate(DOMUtils.getElementValue(el, "FirstAired")));
		// YEAR is not set for TV Metadata, we get that from the Series Info
		// md.setYear(DateUtils.parseYear(DOMUtils.getElementValue(el,
		// "FirstAired")));
		md.setDescription(DOMUtils.getElementValue(el, "Overview"));
		md.setUserRating(MetadataSearchUtil.parseUserRating(DOMUtils.getElementValue(el, "Rating")));
		md.setIMDBID(DOMUtils.getElementValue(el, "IMDB_ID"));

		String epImage = DOMUtils.getElementValue(el, "filename");
		if (!StringUtils.isEmpty(epImage)) {
			// Added for EvilPenguin
			md.getFanart().add(
					new MediaArt(MediaArtifactType.EPISODE, TVDBMetadataProvider.getFanartURL(epImage), md.getSeasonNumber()));
		}

		addCastMember(md, DOMUtils.getElementValue(el, "GuestStars"), "Guest", md.getGuests());
		addCastMember(md, DOMUtils.getElementValue(el, "Writer"), "Writer", md.getWriters());
		addCastMember(md, DOMUtils.getElementValue(el, "Director"), "Director", md.getDirectors());
	}

	private void addCastMember(IMetadata md, String strSplit, String part, List<ICastMember> cast) {
		if (!StringUtils.isEmpty(strSplit)) {
			String directorsArr[] = strSplit.split("[,\\|]");
			for (String d : directorsArr) {
				if (!StringUtils.isEmpty(d)) {
					CastMember cm = new CastMember();
					cm.setName(d.trim());
					cast.add(cm);
					log.debug("Adding Cast Member: " + cm.getName());
				}
			}
		}
	}

	private void addSeasonEpisodeInfoByDate(IMetadata md, String date) {
		try {
			// tvdb requires dashes not dots
			if (date != null)
				date = date.replace('.', '-');
			String allurl = MessageFormat.format(EPISODE_BY_DATE_URL, TVDBMetadataProvider.getApiKey(), result.getId(), date);
			log.info("TVDB date: " + allurl);

			IUrl url = UrlFactory.newUrl(allurl);
			Document doc = DOMUtils.parseDocument(url);

			NodeList nl = doc.getElementsByTagName("Episode");
			if (nl.getLength() > 0) {
				Element el = (Element) nl.item(0);
				String season = DOMUtils.getElementValue(el, "SeasonNumber");
				String episode = DOMUtils.getElementValue(el, "EpisodeNumber");
				// we get the by date xml and then request season/episode
				// specific one, because the by date xml
				// is not the same as the by season/episode
				addSeasonEpisodeInfo(md, season, episode);
			}
		} catch (Exception e) {
			log.warn("Failed to get season/episode specific information for " + result.getId() + "; Date: " + date, e);
		}

	}

	private void addSeasonEpisodeInfoByTitle(IMetadata md, String title) {
		try {
			String allurl = MessageFormat.format(EPISODE_BY_TITLE, TVDBMetadataProvider.getApiKey(), result.getId(),
					config.getLanguage());
			log.info("TVDB Title: " + allurl);
			IUrl url = UrlFactory.newUrl(allurl);
			Document doc = DOMUtils.parseDocument(url);

			NodeList nl = doc.getElementsByTagName("Episode");
			boolean updated = updateIfScored(nl, title, 1.0f);
			if (!updated) {
				float matchScore = 0.8f;
				log.debug("Couldn't find an exact title match, so using a fuzzy match score of " + matchScore);

				// do another search, this time use a less sensitive matching
				// criteria
				updated = updateIfScored(nl, title, matchScore);
			}

			if (!updated) {
				log.info("Unable to match a direct title for: " + title);
			}
		} catch (Throwable e) {
			log.warn("Failed to find a match based on title: " + title);
		}
	}

	private boolean updateIfScored(NodeList nl, String title, float scoreToMatch) {
		boolean updated = false;
		int s = nl.getLength();
		for (int i = 0; i < s; i++) {
			Element el = (Element) nl.item(i);
			String epTitle = DOMUtils.getElementValue(el, "EpisodeName");
			float score = MetadataSearchUtil.calculateCompressedScore(title, epTitle);

			if (score >= scoreToMatch) {
				log.debug("Found a title match: " + epTitle + "; Updating Metadata.");
				updateMetadataFromElement(md, el);
				updated = true;
				break;
			}
		}
		return updated;
	}

	private void addSeasonEpisodeInfo(IMetadata md, String season, String episode) {
		int inSeason = NumberUtils.toInt(season, -1);
		int inEpisode = NumberUtils.toInt(episode, -1);

		if (inSeason > 0 && inEpisode > 0) {
			try {
				updateMetadataFromUrl(
						md,
						MessageFormat.format(SEASON_EPISODE_URL, TVDBMetadataProvider.getApiKey(), result.getId(),
								String.valueOf(inSeason), String.valueOf(inEpisode), config.getLanguage()));
			} catch (Exception e) {
				log.warn("Failed to get season/episode specific information for " + result.getId() + "; Season: " + season
						+ "; episode: " + episode, e);
			}
		} else {
			// TODO: Someday, allow for Season 0 and specials
			log.warn("Can't do lookup by season/epsidoe for season: " + season + "; episode: " + episode);
		}
	}

	private void addBanners(IMetadata md, String season) {
		int inSeason = NumberUtils.toInt(season, -9);
		try {
			if (banners == null) {
				String seriesUrl = MessageFormat.format(BANNERS_URL, TVDBMetadataProvider.getApiKey(), result.getId());
				log.info("Parsing TVDB Banners url: " + seriesUrl);
				IUrl url = UrlFactory.newUrl(seriesUrl);
				banners = DOMUtils.parseDocument(url);
			}

			NodeList nl = banners.getElementsByTagName("Banner");
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);

				String lang = DOMUtils.getElementValue(el, "Language");
				if (StringUtils.isEmpty(lang) || lang.equals("en") || lang.equals(config.getLanguage())) {

					String type = DOMUtils.getElementValue(el, "BannerType");

					MediaArt ma = null;
					if ("fanart".equals(type)) {
						ma = new MediaArt();
						ma.setType(MediaArtifactType.BACKGROUND);
					} else if ("poster".equals(type)) {
						ma = new MediaArt();
						ma.setType(MediaArtifactType.POSTER);
					} else if ("series".equals(type)) {
						ma = new MediaArt();
						ma.setType(MediaArtifactType.BANNER);
					} else if ("season".equals(type)) {
						int seasonNum = DOMUtils.getElementIntValue(el, "Season", -1);
						if (seasonNum == inSeason) {
							String type2 = DOMUtils.getElementValue(el, "BannerType2");
							if ("season".equals(type2)) {
								ma = new MediaArt();
								ma.setType(MediaArtifactType.POSTER);
							} else if ("seasonwide".equals(type2)) {
								ma = new MediaArt();
								ma.setType(MediaArtifactType.BANNER);
							} else {
								log.debug("Unhandled Season Banner Type2: " + type2);
							}
							if (ma != null) {
								ma.setSeason(seasonNum);
							}
						}
					} else {
						log.debug("Unhandled Banner Type: " + type);
					}

					if (ma != null) {
						addFanartUrl(md, ma, DOMUtils.getElementValue(el, "BannerPath"));
					}
				}
			}
		} catch (Throwable e) {
			log.warn("Unable to process banners for series: " + result, e);
		}
	}

	private void addFanartUrl(IMetadata md, MediaArt ma, String path) {
		if (StringUtils.isEmpty(path))
			return;
		ma.setDownloadUrl(TVDBMetadataProvider.getFanartURL(path));
		md.getFanart().add(ma);
	}
}
