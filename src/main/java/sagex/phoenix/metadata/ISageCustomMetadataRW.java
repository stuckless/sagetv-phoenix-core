package sagex.phoenix.metadata;

import java.util.Date;
import java.util.List;

import sagex.phoenix.metadata.proxy.SageProperty;

/**
 * Provide support for some common custom fields, This list is not extensive,
 * but it's a start.
 * <p/>
 * <pre>
 * IMDBID;
 * DiscNumber;
 * EpisodeNumber;
 * EpisodeTitle; (removed, since it's the EpisodeName)
 * MediaProviderDataID;
 * MediaTitle;
 * MediaType;
 * OriginalAirDate; (removed since it's defined in the Sage Properties)
 * SeasonNumber;
 * UserRating;
 * </pre>
 * <pre>
 * Added "EpisodeCount" to allow overriding the number of episodes contained inside a single episode mediafile
 * </pre>
 * <pre>
 * Added "CollectionName, ID and Overview" which represent the movie collection a movie belongs to (optional)
 * </pre>
 *
 * @author seans
 */
public interface ISageCustomMetadataRW extends ISageMetadata {
    /**
     * MediaTitle is used ONLY for Fanart purposes
     *
     * @return
     */
    @SageProperty("MediaTitle")
    public String getMediaTitle();

    @SageProperty("MediaTitle")
    public void setMediaTitle(String title);

    @SageProperty("MediaType")
    public String getMediaType();

    @SageProperty("MediaType")
    public void setMediaType(String type);

    @SageProperty("SeasonNumber")
    public int getSeasonNumber();

    @SageProperty("SeasonNumber")
    public void setSeasonNumber(int num);

    @SageProperty("EpisodeNumber")
    public int getEpisodeNumber();

    @SageProperty("EpisodeNumber")
    public void setEpisodeNumber(int num);

    @SageProperty("IMDBID")
    public String getIMDBID();

    @SageProperty("IMDBID")
    public void setIMDBID(String id);

    @SageProperty("DiscNumber")
    public int getDiscNumber();

    @SageProperty("DiscNumber")
    public void setDiscNumber(int disc);

    @SageProperty("MediaProviderID")
    public String getMediaProviderID();

    @SageProperty("MediaProviderID")
    public void setMediaProviderID(String id);

    @SageProperty("MediaProviderDataID")
    public String getMediaProviderDataID();

    @SageProperty("MediaProviderDataID")
    public void setMediaProviderDataID(String id);

    /**
     * UserRating should be a value between 0 and 100, so a rating of "9.5"
     * would actually be "95"
     *
     * @return
     */
    @SageProperty("UserRating")
    public int getUserRating();

    @SageProperty("UserRating")
    public void setUserRating(int f);

    @SageProperty(value = "Fanart", listFactory = "sagex.phoenix.metadata.proxy.MediaArtPropertyListFactory")
    public List<IMediaArt> getFanart();

    @SageProperty("TrailerUrl")
    public String getTrailerUrl();

    @SageProperty("TrailerUrl")
    public void setTrailerUrl(String trailer);

    @SageProperty("SeriesInfoID")
    public int getSeriesInfoID();

    @SageProperty("SeriesInfoID")
    public void setSeriesInfoID(int id);

    /**
     * EpisodeCount is the count of episodes contained in the MediaFile
     * This will track situations where a pilot may contain 2 or more episodes in the single file
     * An unset value will be considered equal to 1 episode
     *
     * @return
     */
    @SageProperty("EpisodeCount")
    public int getEpisodeCount();

    @SageProperty("EpisodeCount")
    public void setEpisodeCount(int count);

    /**
     * CollectionName is the name/title of the optional Movie collection/group that a movie belongs to
     *
     * @return
     */
    @SageProperty("CollectionName")
    public String getCollectionName();

    @SageProperty("CollectionName")
    public void setCollectionName(String name);

    /**
     * CollectionID is the ID of the optional Movie collection/group that a movie belongs to
     * the ID can link back to the provider such as TMDB to get associated collection fanart
     *
     * @return
     */
    @SageProperty("CollectionID")
    public int getCollectionID();

    @SageProperty("CollectionID")
    public void setCollectionID(int id);

    /**
     * CollectionOverview is the overview/description of the optional Movie collection/group
     *
     * @return
     */
    @SageProperty("CollectionOverview")
    public String getCollectionOverview();

    @SageProperty("CollectionOverview")
    public void setCollectionOverview(String overview);

    /**
     * DefaultPoster, DefaultBackground, and DefaultBanner should ONLY set the
     * relative path of the poster, background, banner, relative to the
     * CentralFanart dir.
     *
     * @return
     */
    @SageProperty(FieldName.DEFAULT_POSTER)
    public String getDefaultPoster();

    @SageProperty(FieldName.DEFAULT_POSTER)
    public void setDefaultPoster(String poster);

    @SageProperty(FieldName.DEFAULT_BANNER)
    public String getDefaultBanner();

    @SageProperty(FieldName.DEFAULT_BANNER)
    public void setDefaultBanner(String banner);

    @SageProperty(FieldName.DEFAULT_BACKGROUND)
    public String getDefaultBackground();

    @SageProperty(FieldName.DEFAULT_BACKGROUND)
    public void setDefaultBackground(String background);

    @SageProperty(FieldName.SCRAPED_BY)
    public String getScrapedBy();

    @SageProperty(FieldName.SCRAPED_BY)
    public void setScrapedBy(String by);

    @SageProperty(FieldName.SCRAPED_DATE)
    public Date getScrapedDate();

    @SageProperty(FieldName.SCRAPED_DATE)
    public void setScrapedDate(Date date);

    @SageProperty("MediaUrl")
    public String getMediaUrl();

    @SageProperty("MediaUrl")
    public void setMediaUrl(String trailer);

    @SageProperty(FieldName.TRIVIA)
    public String getTrivia();

    @SageProperty(FieldName.TRIVIA)
    public void setTrivia(String trivia);

    @SageProperty(FieldName.QUOTES)
    public String getQuotes();

    @SageProperty(FieldName.QUOTES)
    public void setQuotes(String quotes);

    @SageProperty(FieldName.TAGLINE)
    public String getTagLine();

    @SageProperty(FieldName.TAGLINE)
    public void setTagLine(String tagline);

    // fields for custom metadata that are references in other locations
    public static interface FieldName {
        public static final String TAGLINE = "TagLine";
        public static final String QUOTES = "Quotes";
        public static final String TRIVIA = "Trivia";

        public static final String DEFAULT_POSTER = "DefaultPoster";
        public static final String DEFAULT_BANNER = "DefaultBanner";
        public static final String DEFAULT_BACKGROUND = "DefaultBackground";
        public static final String SCRAPED_BY = "ScrapedBy";
        public static final String SCRAPED_DATE = "ScrapedDate";
    }
}
