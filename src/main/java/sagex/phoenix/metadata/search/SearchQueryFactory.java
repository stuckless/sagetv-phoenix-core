package sagex.phoenix.metadata.search;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataHints;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.util.PathUtils;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

public class SearchQueryFactory {
    private Logger log = Logger.getLogger(SearchQueryFactory.class);

    public SearchQuery createTVQuery(String title) {
        return new SearchQuery(MediaType.TV, title);
    }

    public SearchQuery createMovieQuery(String title) {
        return new SearchQuery(MediaType.MOVIE, title);
    }

    /**
     * Creates a Specific query type by scraping the underlying filename of the
     * media resource
     *
     * @param resource
     * @param searchType
     * @return
     */
    public SearchQuery createQueryFromFilename(IMediaFile resource, MediaType searchType, Hints hints) {
        SearchQuery q = createQueryFromFilename(resource, hints);

        if (q != null && searchType != null) {
            q.setMediaType(searchType);
        }

        return q;
    }

    /**
     * Attempts to create a query by scraping the filename of the underlying
     * MediaFile object
     *
     * @param resource
     * @return
     */
    public SearchQuery createQueryFromFilename(IMediaFile resource, Hints hints) {
        SearchQuery q = null;

        // try to create TV query
        try {
            q = Phoenix.getInstance().getTVScrapers().createSearchQuery(resource, hints);
        } catch (Exception e) {
            log.warn("TV Title scrapers failed!", e);
        }

        // if no query, then try a movie query
        if (q == null) {
            try {
                q = Phoenix.getInstance().getMovieScrapers().createSearchQuery(resource, hints);
            } catch (Exception e) {
                log.warn("Movie Title scrapers failed!", e);
            }
        }

        if (q == null) {
            log.warn("Failed to create a SearchQuery for resource: " + resource);
            return null;
        }
        // clean the episode name and the title fields
        if (q.get(Field.RAW_TITLE) != null) {
            q.set(Field.RAW_TITLE, SearchUtil.removeNonSearchCharacters(q.get(Field.RAW_TITLE)));
        }
        if (q.get(Field.EPISODE_TITLE) != null) {
            q.set(Field.EPISODE_TITLE, SearchUtil.removeNonSearchCharacters(q.get(Field.EPISODE_TITLE)));
        }

        // add in a cleaned title
        q.set(Field.CLEAN_TITLE, SearchUtil.cleanSearchCriteria(q.get(Field.RAW_TITLE), true));

        // TODO: Support Music Queries
        updateQueryUsingMediaTitlesXml(resource, q);
        updateQueryDateAndFileFromResource(resource, q);
        updateQueryFromSageCategory(resource, q);

        q.set(Field.CLEAN_TITLE, SearchUtil.cleanSearchCriteria(q.get(Field.RAW_TITLE), true));

        // clean all the fields
        for (Field f : SearchQuery.Field.values()) {
            String s = q.get(f);
            if (s != null) {
                s = s.trim();
                // don't clean raw title
                if (f == Field.RAW_TITLE) {
                    s = s.replaceAll("[^a-zA-Z0-9\\(\\)\\p{L}]+$", "");
                } else {
                    s = s.replaceAll("[^a-zA-Z0-9\\p{L}]+$", "");
                }
                q.set(f, s);
            }
        }

        log.info("Created Search Query: " + q);
        return q;
    }

    private void updateQueryDateAndFileFromResource(IMediaFile resource, SearchQuery q) {
        if (q != null) {
            File f = PathUtils.getFirstFile(resource);
            if (f != null) {
                q.set(Field.FILE, f.getAbsolutePath());
                // if there isn't a date set, then try setting the date using
                // the file's date/time
                if (StringUtils.isEmpty(q.get(Field.EPISODE_DATE))) {
                    q.set(Field.EPISODE_DATE, getFormattedAiringDate(f.lastModified()));
                }

                int cd = ScraperUtils.parseCD(f.getAbsolutePath());
                if (cd > 0) {
                    q.set(Field.DISC, String.valueOf(cd));
                }
            }
        }
    }

    /**
     * Attempts to update the SearchQuery MediaType based on whether or not
     * SageTV thinks this is a Movie. Some TV recordings are movies.
     *
     * @param resource
     * @param q
     */
    public void updateQueryFromSageCategory(IMediaFile resource, SearchQuery q) {
        if (q != null) {
            // only do this if the query doesn't have an exact id
            if (StringUtils.isEmpty(q.get(Field.ID))) {
                if (MetadataUtil.isRecordedMovie(resource)) {
                    log.info("Setting the media type to Movie for query: " + q + "; Because it's a SageTV Movie");
                    q.setMediaType(MediaType.MOVIE);
                }
            }
        }
    }

    /**
     * Updates the Search Query using information found in the MediaTitles
     * configuration
     *
     * @param resource
     * @param q
     */
    public void updateQueryUsingMediaTitlesXml(IMediaFile resource, SearchQuery q) {
        File f = PathUtils.getFirstFile(resource);
        if (f != null) {
            FileMatcher match = Phoenix.getInstance().getMediaTitlesManager().getMatcher(f.getAbsolutePath());
            if (match != null) {
                log.info("Applying MediaTitle information: " + match);
                q.setMediaType(match.getMediaType());

                if (!StringUtils.isEmpty(match.getTitle())) {
                    q.set(Field.RAW_TITLE, match.getTitle());
                }

                if (!StringUtils.isEmpty(match.getYear())) {
                    q.set(Field.YEAR, match.getYear());
                }

                if (match.getMetadata() != null) {
                    q.set(Field.PROVIDER, match.getMetadata().getName());
                    q.set(Field.ID, match.getMetadata().getValue());
                }
            }
        }
    }

    private static Map<String, SearchQuery.Field> mappedFields = new HashMap<String, Field>();

    static {
        mappedFields.put("title", Field.RAW_TITLE);
        mappedFields.put("disc", Field.DISC);
        mappedFields.put("episodedate", Field.EPISODE_DATE);
        mappedFields.put("episodenumber", Field.EPISODE);
        mappedFields.put("episodetitle", Field.EPISODE_TITLE);
        mappedFields.put("seasonnumber", Field.SEASON);
        mappedFields.put("id", Field.ID);
        mappedFields.put("year", Field.YEAR);
    }

    public static Set<String> getJSONQueryFields() {
        return mappedFields.keySet();
    }

    public void updateQueryFromJSON(SearchQuery query, String data) throws Exception {
        JSONObject jo = new JSONObject(data);
        for (Iterator i = jo.keys(); i.hasNext(); ) {
            String k = (String) i.next();
            String v = jo.getString(k);
            if ("MediaType".equalsIgnoreCase(k)) {
                query.setMediaType(MediaType.toMediaType(v));
            } else {
                SearchQuery.Field f = mappedFields.get(k.toLowerCase());
                if (f == null) {
                    throw new JSONException("Invalid Field: " + k);
                }
                log.debug("Setting Query Field via json args: " + f + " = " + v);
                query.set(f, v);
            }
        }
    }

    /**
     * creates a query using the sagetv resource. This does not do any file
     * parsing, it simply reads the current state of information in the object
     * and builds a query. This is useful for recorded tv and recored movies
     * that do no require filename scraping.
     *
     * @param res
     */
    public SearchQuery createQueryFromExistingMetadata(IMediaFile res, Hints hints) {
        log.debug("Creating a 'clean' query for " + res.getTitle() + " because it has some basic metadata.");
        SearchQuery q = new SearchQuery(hints);
        IMetadata md = res.getMetadata();
        if (res.isType(MediaResourceType.TV.value()) || res.isType(MediaResourceType.EPG_AIRING.value())) {
            q.setMediaType(MediaType.TV);
            q.set(Field.RAW_TITLE,
                    sagex.phoenix.util.StringUtils.firstNonEmpty(
                            sagex.phoenix.util.StringUtils.fixTitle(md.getRelativePathWithTitle()), md.getEpisodeName()));
            q.set(Field.CLEAN_TITLE, q.get(Field.RAW_TITLE));
            q.set(Field.EPISODE_TITLE, res.getMetadata().getEpisodeName());
            q.set(Field.EPISODE_DATE, getFormattedAiringDate(res.getMetadata().getOriginalAirDate().getTime()));
            if (res.getMetadata().getEpisodeNumber() > 0) {
                q.set(Field.EPISODE, String.valueOf(res.getMetadata().getEpisodeNumber()));
                q.set(Field.SEASON, String.valueOf(res.getMetadata().getSeasonNumber()));
            }

            // see if we can update the year from the series info
            ISeriesInfo info = phoenix.media.GetSeriesInfo(res);
            if (info != null) {
                int year = DateUtils.parseYear(info.getPremiereDate());
                if (year > 0) {
                    q.set(Field.YEAR, String.valueOf(year));
                }
            }
        } else {
            q.setMediaType(MediaType.MOVIE);
            q.set(Field.RAW_TITLE,
                    sagex.phoenix.util.StringUtils.firstNonEmpty(md.getEpisodeName(),
                            sagex.phoenix.util.StringUtils.fixTitle(md.getRelativePathWithTitle())));
            q.set(Field.CLEAN_TITLE, q.get(Field.RAW_TITLE));
        }

        if (StringUtils.isEmpty(q.get(Field.YEAR)) && md.getYear() > 0) {
            q.set(Field.YEAR, String.valueOf(md.getYear()));
        }

        updateQueryUsingMediaTitlesXml(res, q);

        if (StringUtils.isEmpty(q.get(Field.EPISODE_DATE))) {
            updateQueryDateAndFileFromResource(res, q);
        }

        updateQueryFromSageCategory(res, q);
        return q;
    }

    private String getFormattedAiringDate(long time) {
        if (time <= 0) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date d = new Date(time);
        return dateFormat.format(d);
    }

    /**
     * Most resources are Sage Resources, so this attempts to first create a
     * query using the existing Sage Resource to fill in the title, year,
     * episode title, etc.
     * <p/>
     * If the resource is a recording, airing, or already has a MediaType, then
     * it will simply use the existing metadata from the object as the query.
     * Otherwise it will do a scraped query.
     *
     * @param res
     */
    public SearchQuery createSageFriendlyQuery(IMediaFile res, Hints hints) {
        SearchQuery q = null;
        if (res.isType(MediaResourceType.MUSIC.value())) {
            q = new SearchQuery(hints);
            IAlbumInfo info = res.getAlbumInfo();
            if (info != null) {
                q.set(Field.RAW_TITLE, res.getTitle());
                q.set(Field.ARTIST, info.getArtist());
                q.set(Field.ALBUM, info.getName());
                q.setMediaType(MediaType.MUSIC);
            }
        } else if (hints.getBooleanValue(MetadataHints.KNOWN_RECORDING, false) || res.isType(MediaResourceType.RECORDING.value())
                || res.isType(MediaResourceType.EPG_AIRING.value())) {
            if (MetadataUtil.isImportedRecording(res)) {
                // these were non sage recordings, that we imported, so we'll do
                // a full scape.
                q = createQueryFromFilename(res, hints);
            } else {
                // do a clean query
                q = createQueryFromExistingMetadata(res, hints);
            }
        } else {
            // do a normal scraped query
            q = createQueryFromFilename(res, hints);
        }
        return q;
    }
}
