package sagex.phoenix.metadata.provider.tmdb3;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProviderInfo;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataProvider;
import sagex.phoenix.metadata.provider.ScoredSearchResultSorter;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.HasFindByIMDBID;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.DateUtils;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.AlternativeTitle;
import com.omertron.themoviedbapi.model.Artwork;
import com.omertron.themoviedbapi.model.ArtworkType;
import com.omertron.themoviedbapi.model.Genre;
import com.omertron.themoviedbapi.model.MovieDb;
import com.omertron.themoviedbapi.model.Person;
import com.omertron.themoviedbapi.model.ReleaseInfo;
import com.omertron.themoviedbapi.model.Trailer;
import com.omertron.themoviedbapi.results.TmdbResultsList;

/**
 * TheMovieDB Provider using themoviedb api v3
 */
public class TMDB3MetadataProvider extends MetadataProvider implements HasFindByIMDBID {
	private Logger log = Logger.getLogger(this.getClass());

	private TheMovieDbApi tmdb = null;
	private TMDB3Configuration config = null;
	private MetadataConfiguration mdConfig = null;

	private static class ScoredTitle {
		String title;
		float score;

		public ScoredTitle(String title, float score) {
			this.title = title;
			this.score = score;
		}
	}

	public TMDB3MetadataProvider(IMetadataProviderInfo info) {
		super(info);
		try {
			tmdb = new TheMovieDbApi(getApiKey());
		} catch (Throwable t) {
			log.error("Failed to create The Movie DB v3 Provider", t);
			throw new RuntimeException(t);
		}
		config = GroupProxy.get(TMDB3Configuration.class);
		mdConfig = GroupProxy.get(MetadataConfiguration.class);
	}

	public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
		if (MetadataSearchUtil.hasMetadata(result))
			return MetadataSearchUtil.getMetadata(result);

		String id = result.getId();
		int tid = NumberUtils.toInt(id);
		if (tid == 0) {
			throw new MetadataException("TheMovieDB: Failed to find movie info for " + id);
		}

		try {
			return createMetadata(tmdb.getMovieInfo(tid, config.getLanguage()));
		} catch (Throwable e) {
			throw new MetadataException("TheMovieDB: Failed to find movie for result", result, e);
		}
	}

	protected IMetadata createMetadata(MovieDb movie) throws MetadataException, MovieDbException {
		IMetadata md = MetadataProxy.newInstance();

		int tid = movie.getId();

		if (tid==0 || StringUtils.isEmpty(movie.getTitle())) {
			throw new MetadataException("Metadata Failed for " + movie.getId());
		}
		
		
		md.setDescription(movie.getOverview());

		List<Genre> genres = movie.getGenres();
		if (genres != null) {
			for (Genre g : genres) {
				md.getGenres().add(g.getName());
			}
		}

		md.setMediaProviderID(getInfo().getId());
		md.setMediaProviderDataID(String.valueOf(movie.getId()));
		md.setIMDBID(movie.getImdbID());
		md.setMediaType(MediaType.MOVIE.sageValue());
		md.setEpisodeName(movie.getTitle());
		md.setMediaTitle(movie.getTitle());
		md.setOriginalAirDate(DateUtils.parseDate(movie.getReleaseDate()));
		md.setRunningTime(movie.getRuntime() * 60 * 1000);
		
		TmdbResultsList<Trailer> results = getMovieTrailers(tmdb, tid, config.getLanguage());
		if (results == null || results.getTotalResults() == 0) {
			results = getMovieTrailers(tmdb, tid, null);
		}
		
		List<Trailer> trailers = null;
		if (results!=null) {
			trailers = results.getResults();
		}
				
		if (trailers != null && trailers.size() > 0) {
			for (Trailer t : trailers) {
				// look for youtube, HD, trailers
				if ("youtube".equalsIgnoreCase(t.getWebsite()) && "HD".equalsIgnoreCase(t.getSize())
						&& !StringUtils.isEmpty(t.getSource())) {
					setYoutubeTrailer(md, t);
					break;
				}
			}

			// not trailer, so look for youtube, non HD
			if (StringUtils.isEmpty(md.getTrailerUrl())) {
				for (Trailer t : trailers) {
					if ("youtube".equalsIgnoreCase(t.getWebsite()) && !StringUtils.isEmpty(t.getSource())) {
						setYoutubeTrailer(md, t);
						break;
					}
				}
			}
		}

		md.setTagLine(movie.getTagline());

		List<Person> cast = null;
		TmdbResultsList<Person> castResults = getMovieCasts(tmdb, tid, config.getLanguage());
		if (castResults==null || castResults.getTotalResults()==0) {
			castResults = getMovieCasts(tmdb, tid, null);
		}
		
		if (castResults!=null) {
			cast = castResults.getResults();
		}
		if (cast != null) {
			for (Person p : cast) {
				String job = p.getJob();
				if (job == null)
					continue;
				if ("actor".equalsIgnoreCase(job)) {
					md.getActors().add(new CastMember(p.getName(), p.getCharacter()));
				} else if ("director".equalsIgnoreCase(job)) {
					md.getDirectors().add(new CastMember(p.getName(), null));
				} else if ("producer".equalsIgnoreCase(job)) {
					md.getProducers().add(new CastMember(p.getName(), null));
				} else if ("writer".equalsIgnoreCase(job)) {
					md.getWriters().add(new CastMember(p.getName(), null));
				} else if ("screenplay".equalsIgnoreCase(job)) {
					md.getWriters().add(new CastMember(p.getName(), null));
				}
			}
		}

		// we will get ALL the artwork, and sort on language, to prefer art with
		// a language
		processArt(md, getMovieImages(tmdb, tid, null));

		updateReleaseInfo(md, movie, config.getCountry());

		md.setUserRating((int) (movie.getVoteAverage() * 10));

		md.setYear(DateUtils.parseYear(movie.getReleaseDate()));

		return md;
	}

	private TmdbResultsList<Artwork> getMovieImages(TheMovieDbApi tmdb2, int tid, Object object) {
		try {
			return tmdb.getMovieImages(tid, null);
		} catch (Throwable t) {
			return null;
		}
	}

	private TmdbResultsList<Person> getMovieCasts(TheMovieDbApi tmdb2, int tid, String language) {
		try {
			return tmdb2.getMovieCasts(tid, language);
		} catch (Throwable t) {}
		return null;
	}

	private TmdbResultsList<Trailer> getMovieTrailers(TheMovieDbApi tmdb2, int tid, String language) {
		try {
			return tmdb2.getMovieTrailers(tid, language);
		} catch (Throwable t) {
			return null;
		}
	}

	protected void processArt(IMetadata md, TmdbResultsList<Artwork> results) {
		if (results==null) {
			return;
		}
		
		List<Artwork> tmdbResultsList = results.getResults();
		if (tmdbResultsList==null) return;
		
		Collections.sort(tmdbResultsList, Collections.reverseOrder(new Comparator<Artwork>() {
			@Override
			public int compare(Artwork o1, Artwork o2) {
				// prefer english
				int c = sagex.phoenix.util.StringUtils.compare(o1.getLanguage(), o2.getLanguage(), false);
				if (c == 0) {
					// if both the same, then prefer size or rating
					if (TMDB3Configuration.PREFER_HIRES.equals(config.getFanartPriorityOrdering())) {
						c = NumberUtils.compare(o1.getWidth(), o2.getWidth());
						if (c == 0) {
							c = NumberUtils.compare(o1.getVoteAverage(), o2.getVoteAverage());
						}
					} else if (TMDB3Configuration.PREFER_USER_RATING.equals(config.getFanartPriorityOrdering())) {
						c = NumberUtils.compare(o1.getVoteAverage(), o2.getVoteAverage());
						if (c == 0) {
							c = NumberUtils.compare(o1.getWidth(), o2.getWidth());
						}
					} else {
						// do nothing, no additional sort
					}
				}
				return c;
			}
		}));

		int maxPosters = 0, maxBackgrounds = 0;
		for (Artwork a : tmdbResultsList) {
			if (a.getLanguage() != null && !a.getLanguage().equalsIgnoreCase(config.getLanguage())) {
				// skip if the language is set, but our language
				// we still process null languages, though.
				continue;
			}

			MediaArt ma = new MediaArt();
			if (a.getArtworkType() == ArtworkType.POSTER) {
				if (maxPosters >= config.getMaxPosters())
					continue;
				ma.setType(MediaArtifactType.POSTER);
				URL u;
				try {
					u = tmdb.createImageUrl(a.getFilePath(), "original");
					if (u != null) {
						ma.setDownloadUrl(u.toExternalForm());
						maxPosters++;
					}
				} catch (MovieDbException e) {
					ma = null;
				}
			} else if (a.getArtworkType() == ArtworkType.BACKDROP) {
				if (maxBackgrounds >= config.getMaxBackgrounds())
					continue;

				ma.setType(MediaArtifactType.BACKGROUND);
				URL u;
				try {
					u = tmdb.createImageUrl(a.getFilePath(), "original");
					if (u != null) {
						ma.setDownloadUrl(u.toExternalForm());
						maxBackgrounds++;
					}
				} catch (MovieDbException e) {
					ma = null;
				}
			} else {
				ma = null;
			}
			if (ma != null) {
				md.getFanart().add(ma);
			}
		}
	}

	private void setYoutubeTrailer(IMetadata md, Trailer t) {
		if (t.getSource() != null && t.getSource().startsWith("http")) {
			md.setTrailerUrl(t.getSource());
		}
		md.setTrailerUrl("http://www.youtube.com/watch?v=" + t.getSource());
	}

	private void updateReleaseInfo(IMetadata md, MovieDb movie, String country) throws MovieDbException {
		TmdbResultsList<ReleaseInfo> results = tmdb.getMovieReleaseInfo(movie.getId(), config.getLanguage());
		if (results==null) return;
		
		List<ReleaseInfo> relInfo = results.getResults();
		if (relInfo != null) {
			for (ReleaseInfo ri : relInfo) {
				if (country.equals(ri.getCountry())) {
					setRatingFromReleaseInfo(md, ri);
				}
			}
			if (StringUtils.isEmpty(md.getRated()) && relInfo.size() > 0) {
				ReleaseInfo ri = relInfo.get(0);
				setRatingFromReleaseInfo(md, ri);
				log.info("No Release Info for " + country + "; using first in list: " + ri.getCountry() + "; "
						+ ri.getCertification());
			}
		}
	}

	private void setRatingFromReleaseInfo(IMetadata md, ReleaseInfo ri) {
		String rating = Phoenix.getInstance().getRatingsManager().getRating(MediaType.MOVIE, ri.getCertification());
		md.setRated(rating);
		if (!StringUtils.isEmpty(ri.getCertification())) {
			if (!ri.getCertification().equals(rating)) {
				md.setExtendedRatings(ri.getCertification());
			}
		}
	}

	public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
		// search by ID, if the ID is present
		if (!StringUtils.isEmpty(query.get(SearchQuery.Field.ID))) {
			return MetadataSearchUtil.searchById(this, query, query.get(SearchQuery.Field.ID));
		}

		// check if we are searching for movies
		if (query.getMediaType() != MediaType.MOVIE) {
			throw new MetadataException("Unsupported Search Type: " + query.getMediaType(), query);
		}

		// carry on normal search
		try {
			return getResults(query);
		} catch (Throwable e) {
			throw new MetadataException(e);
		}
	}

	protected List<IMetadataSearchResult> getResults(SearchQuery q) throws MovieDbException {
		// parse
		List<IMetadataSearchResult> results = new ArrayList<IMetadataSearchResult>();

		TmdbResultsList<MovieDb> movies = tmdb.searchMovie(q.get(Field.QUERY), NumberUtils.toInt(q.get(Field.YEAR)), config.getLanguage(),
				config.getIncludeAdult(), 0);
		if (movies != null) {
			for (MovieDb m : movies.getResults()) {
				addMovie(q, m, results);
			}
			Collections.sort(results, ScoredSearchResultSorter.INSTANCE);
		}

		// after the first search, using language, test if we have a hit,
		// if not, then try again, without a language, and see if that helps
		if (!MetadataSearchUtil.isGoodSearch(results)) {
			results.clear();
			movies = tmdb.searchMovie(q.get(Field.QUERY), NumberUtils.toInt(q.get(Field.YEAR)), null, config.getIncludeAdult(), 0);
			if (movies != null) {
				for (MovieDb m : movies.getResults()) {
					addMovie(q, m, results);
				}
				Collections.sort(results, ScoredSearchResultSorter.INSTANCE);
			}
		}

		return results;
	}

	protected void addMovie(SearchQuery query, MovieDb movie, List<IMetadataSearchResult> results) {
		if (StringUtils.isEmpty(movie.getTitle())) {
			return;
		}

		MediaSearchResult sr = new MediaSearchResult();
		sr.setMediaType(MediaType.MOVIE);
		MetadataSearchUtil.copySearchQueryToSearchResult(query, sr);
		sr.setProviderId(getInfo().getId());

		sr.setTitle(movie.getTitle());
		sr.setScore(getScore(query.get(Field.QUERY), movie.getTitle()));

		// only score titles if we don't already have an exact match
		if (sr.getScore() < 1.0) {
			// add alternate title scoring...
			if (mdConfig.isScoreAlternateTitles()) {
				log.debug("Looking for alternate Titles");
				List<ScoredTitle> scoredTitles = new LinkedList<ScoredTitle>();

				TmdbResultsList<AlternativeTitle> titles;
				try {
					titles = tmdb.getMovieAlternativeTitles(movie.getId(), null);
					if (titles != null) {
						for (AlternativeTitle t : titles.getResults()) {
							String altTitle = t.getTitle();
							if (!StringUtils.isEmpty(altTitle)) {
								altTitle = altTitle.trim();
								ScoredTitle st = new ScoredTitle(altTitle, getScore(query.get(Field.QUERY), altTitle));
								scoredTitles.add(st);
								log.debug("Adding Alternate Title: " + st.title + "; score: " + st.score);
							}
						}
					}
				} catch (MovieDbException e) {
					log.warn("Alternate Title Search failed for " + query, e);
				}

				// check if the alternate title score is better than what we
				// have
				if (scoredTitles.size() > 0) {
					ScoredTitle curTitle = new ScoredTitle(sr.getTitle(), sr.getScore());
					for (ScoredTitle st : scoredTitles) {
						if (st.score > curTitle.score) {
							curTitle = st;
						}
					}

					if (curTitle.score > sr.getScore()) {
						log.debug("Using Alternate Title Score: " + curTitle.score + "; Title: " + curTitle.title);
						sr.setScore(curTitle.score);
						sr.setTitle(sr.getTitle() + " (aka " + curTitle.title + ") ");
					}
				}
			}
		}

		sr.setYear(DateUtils.parseYear(movie.getReleaseDate()));
		sr.setId(String.valueOf(movie.getId()));
		sr.setIMDBId(movie.getImdbID());

		results.add(sr);
	}

	private float getScore(String searchTitle, String title) {
		if (title == null)
			return 0.0f;
		try {
			float score = MetadataSearchUtil.calculateScore(searchTitle, title);
			return score;
		} catch (Exception e) {
			return 0.0f;
		}
	}

	/**
	 * This is the api key provided for the Batch Metadata Tools. Other projects
	 * MUST NOT use this key. If you are including these tools in your project,
	 * be sure to set the following System property, to set your own key. <code>
	 * themoviedb.api_key=YOUR_KEY
	 * </code>
	 */
	public static String getApiKey() {
		String key = System.getProperty("themoviedb.api_key");
		if (key == null)
			key = "d4ad46ee51d364386b6cf3b580fb5d8c";
		return key;
	}

	@Override
	public IMetadata getMetadataForIMDBId(String imdbid) {
		try {
			return createMetadata(tmdb.getMovieInfoImdb(imdbid, config.getLanguage()));
		} catch (Throwable e) {
			log.warn("TheMovieDB: Failed to find movie for imdb id: " + imdbid, e);
		}
		return null;
	}
}
