package sagex.phoenix.metadata;

import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.Config;
import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.Converter;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "Fanart/Metadata Options", path = "phoenix/fanart", description = "Fanart and Metadata options that control how metadata fanart is treated")
public class MetadataConfiguration extends GroupProxy {
    @AField(label = "Ignore Words in Title", description = "Comma separated list of words that will be removed from a title when doing a search", visible = "prop:server:phoenix/core/enableAdvancedOptions", listSeparator = ",")
    private FieldProxy<String> wordsToClean = new FieldProxy<String>(
            "1080p,720p,480p,1080i,720i,480i,dvd,dvdrip,cam,ts,tc,scr,screener,dvdscr,xvid,divx,avi,vrs,repack,mallat,proper,dmt,dmd,stv,HDTV,x264");

    @AField(label = "TV Metadata Provider(s)", description = "Default provider(s) to use for TV", listSeparator = ",", list = "tmdb:The MovieDB,tvdb:TVDb,tvdb4:TVDb4", scope = ConfigScope.SERVER)
    private FieldProxy<String> tvProviders = new FieldProxy<String>("tvdb,tmdb,tvdb4");

    @AField(label = "Movie Metadata Provider(s)", description = "Default provider(s) to use for Movies", listSeparator = ",", list = "tmdb:The MovieDB", scope = ConfigScope.SERVER)
    private FieldProxy<String> movieProviders = new FieldProxy<String>("tmdb");

    @AField(label = "Music Metadata Provider(s)", description = "Default provider(s) to use for Music", listSeparator = ",", visible = "false", scope = ConfigScope.SERVER)
    private FieldProxy<String> musicProviders = new FieldProxy<String>("");

    @AField(label = "Good Score Threshold", description = "Title matching score which must be exceeded to be considered a result a good title match", visible = "prop:server:phoenix/core/enableAdvancedOptions", scope = ConfigScope.SERVER)
    private FieldProxy<Float> goodScoreThreshold = new FieldProxy<Float>(0.9f);

    @AField(label = "Score Alternate Titles", description = "Check alternate titles for matches, and pick the best scored title.", visible = "prop:server:phoenix/core/enableAdvancedOptions", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> scoreAlternateTitles = new FieldProxy<Boolean>(true);

    @AField(label = "Max Images to Download", description = "Maximum # of images to download within each fanart category.", visible = "prop:server:phoenix/core/enableAdvancedOptions", scope = ConfigScope.SERVER)
    private FieldProxy<Integer> maxDownloadableImages = new FieldProxy<Integer>(5);

    @AField(label = "Fanart Enabled", description = "Enable Fanart downloading", fullKey = "phoenix/mediametadata/fanartEnabled")
    private FieldProxy<Boolean> fanartEnabled = new FieldProxy<Boolean>(true);

    @AField(label = "Season Fanart Enabled", description = "Use season specific fanart if it is available")
    private FieldProxy<Boolean> useSeason = new FieldProxy<Boolean>(true);

    @AField(label = "Local Fanart Enabled", description = "Use local fanart if it is available")
    private FieldProxy<Boolean> localFanartEnabled = new FieldProxy<Boolean>(true);

    @AField(label = "Central Fanart Folder", description = "Location of the central fanart folder", fullKey = "phoenix/mediametadata/fanartCentralFolder", type = ConfigType.DIRECTORY)
    private FieldProxy<String> fanartCentralFolder = new FieldProxy<String>(Phoenix.DEFAULT_USERDATA + "/Fanart");

    @AField(label = "Automatic Fanart/Metadata", description = "Automatically fetch Fanart/Metadata for all Sage Media as they are added to the media library, this includes TV shows once they are finished recording.", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> automatedFanartEnabled = new FieldProxy<Boolean>(true);

    @AField(label = "Automatic Retry Count", description = "Number of retries for Automatic Lookup", scope = ConfigScope.SERVER)
    private FieldProxy<Integer> automaticRetryCount = new FieldProxy<Integer>(5);

    @AField(label = "Automatic Retry Threads", description = "Number of threads used for Automatic Lookup retries", scope = ConfigScope.SERVER)
    private FieldProxy<Integer> automaticRetryThreadCount = new FieldProxy<Integer>(2);

    @AField(label = "Automatic Retry Delay", description = "Number of seconds to delay a retry attempt", scope = ConfigScope.SERVER)
    private FieldProxy<Integer> automaticRetryDelay = new FieldProxy<Integer>(60);

    @AField(label = "Delete Downloaded Fanart Smaller than this File Size (K)", description = "Delete images if there filesize is smaller than this size in K", scope = ConfigScope.SERVER)
    private FieldProxy<Integer> deleteImagesSmallerThan = new FieldProxy<Integer>(5);

    @AField(label = "Use System Messages for Failed Lookups", description = "When an automated lookup fails, it can register a SageTV system message about the failure.", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> enableSystemMessagesForFailures = new FieldProxy<Boolean>(false);

    @AField(label = "Use System Messages for Missing Episodes", description = "When a media scan is completed, it will check for missing TV episodes and then register a SageTV system message with the missing episode information.", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> enableSystemMessagesForTVEpisodeGaps = new FieldProxy<Boolean>(false);

    @AField(label = "Preserve Original Metadata for Recordings", description = "When updating metadata, do not update/overwrite metadata on Sage Recordings", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> preserverRecordingMetadata = new FieldProxy<Boolean>(true);

    @AField(label = "Fill in Missing Metadata for Recordings", description = "If Preserve Original Metadata is enabled, AND this option is enabled, then Recording metadata that is blank will be filled in from other Metadata Providers", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> fillInMissingRecordingMetadata = new FieldProxy<Boolean>(true);

    @AField(label = "Import TV Media Types as Recordings", description = "If enabled, it will automatically import TV media files (ie, Show S01E01) as a Sage Recording", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> importTVAsRecordings = new FieldProxy<Boolean>(false);

    // @AField(label="Add Series Fanart for Failed TV Lookups",
    // description="If enabled, it will add the TV Series Fanart for the TV item, even though an exact episode was not found")
    // private FieldProxy<Boolean> addFanartForIncompleteTV = new
    // FieldProxy<Boolean>(false);

    @AField(label = "Archive imported Recordings", description = "Archive all imported Recordings", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> archiveRecordings = new FieldProxy<Boolean>(true);

    @AField(label = "Prefix Videos with Relative path", description = "If true, then Sage Vidoes (not recordings) will contain the relative path of the video item in the Title (as per sagetv documentation)", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> prefixVideosWithRelativePath = new FieldProxy<Boolean>(false);

    @AField(label = "Media Types", description = "Metadata/Fanart lookups will only be issued for media types that match the given media types", listSeparator = ",", list = "ANY_VIDEO,TV,VIDEO,DVD,BLURAY,RECORDING,MUSIC", scope = ConfigScope.SERVER)
    private FieldProxy<String> scannableMediaTypes = new FieldProxy<String>("ANY_VIDEO,MUSIC");

    @AField(label = "Exclude Pattern", description = "A Regular expression pattern that will be used to exclude media files from being scanned based on it's completer file path", hints = Config.Hint.REGEX, scope = ConfigScope.SERVER)
    private FieldProxy<String> excludePattern = new FieldProxy<String>(null, Converter.TEXT);

    // TODO: Currently not used, but useful
    @AField(label = "CD Stacking Regex", description = "Stacking Model regex (taken from xbmc group)", hints = Config.Hint.REGEX, visible = "false", scope = ConfigScope.SERVER)
    private FieldProxy<String> stackingModelRegex = new FieldProxy<String>("[ _\\\\.-]+(cd|dvd|part|disc)[ _\\\\.-]*([0-9a-d]+)");

    @AField(label = "Image Compression", description = "Image compression to use (between 0 and 1.0) where 0 max max compression and 1.0 is no compression.", scope = ConfigScope.SERVER)
    private FieldProxy<Float> imageCompression = new FieldProxy<Float>(1.00f);

    @AField(label = "Fetch Quotes and Trivia", description = "If true, then Quotes and Trivia will fetched from metadata providers that support it.", scope = ConfigScope.SERVER, fullKey = "metadata/get_imdb_trivia")
    private FieldProxy<Boolean> fetchQuotesAndTrivia = new FieldProxy<Boolean>(true);


    @AField(label = "Scale Large Fanart", description = "If enabled, then large fanart will be scaled down to conserve memory in SageTV Server and MiniClients", scope = ConfigScope.SERVER)
    private FieldProxy<Boolean> scaleLargeFanart = new FieldProxy<Boolean>(true);

    @AField(label = "Max Screen Size", description = "Sets the size of the largest screen, in WidthxHeight (ie, 1920x1080).  This is used for scaling Fanart.", scope = ConfigScope.SERVER)
    private FieldProxy<String> maxScreenSize = new FieldProxy<String>("1920x1080");

    @AField(label = "Rescale Fanart", description = "Scales source fanart based on Max Screen Size", scope = ConfigScope.SERVER, type = ConfigType.BUTTON, fullKey = "phoenix/fanart/rescaleFanart")
    private FieldProxy<String> rescaleFanart = new FieldProxy<String>("RESCALE");

    public MetadataConfiguration() {
        super();
        init();
    }

    public String getMaxScreenSize() {
        return maxScreenSize.get();
    }

    public void setMaxScreenSize(String v) {
        this.maxScreenSize.set(v);
    }

    public boolean scaleLargeFanart() {
        return scaleLargeFanart.get();
    }

    public void setScaleLargeFanart(boolean b) {
        this.scaleLargeFanart.set(b);
    }

    public String getWordsToClean() {
        return wordsToClean.get();
    }

    public void setWordsToClean(String wordsToClean) {
        this.wordsToClean.set(wordsToClean);
    }

    public String getTVProviders() {
        return tvProviders.get();
    }

    public void setTVProviders(String defaultProviderId) {
        this.tvProviders.set(defaultProviderId);
    }

    public String getMovieProviders() {
        return movieProviders.get();
    }

    public void setMovieProviders(String defaultProviderId) {
        this.movieProviders.set(defaultProviderId);
    }

    public String getMusicProviders() {
        return musicProviders.get();
    }

    public void setMusicProviders(String defaultProviderId) {
        this.musicProviders.set(defaultProviderId);
    }

    public float getGoodScoreThreshold() {
        return goodScoreThreshold.get();
    }

    public void setGoodScoreThreshold(float goodScoreThreshold) {
        this.goodScoreThreshold.set(goodScoreThreshold);
    }

    public float getImageCompression() {
        return imageCompression.get();
    }

    public void setImageCompression(float compression) {
        this.imageCompression.set(compression);
    }

    public boolean isScoreAlternateTitles() {
        return scoreAlternateTitles.get();
    }

    public void setScoreAlternateTitles(boolean scoreAlternateTitles) {
        this.scoreAlternateTitles.set(scoreAlternateTitles);
    }

    public int getMaxDownloadableImages() {
        return maxDownloadableImages.get();
    }

    public void setMaxDownloadableImages(int maxDownloadableImages) {
        this.maxDownloadableImages.set(maxDownloadableImages);
    }

    public String getFanartCentralFolder() {
        return fanartCentralFolder.get();
    }

    public void setCentralFanartFolder(String centralFanartFolder) {
        this.fanartCentralFolder.set(centralFanartFolder);
    }

    public boolean isFanartEnabled() {
        return fanartEnabled.get();
    }

    public void setFanartEnabled(boolean fanartEnabled) {
        this.fanartEnabled.set(fanartEnabled);
    }

    public boolean getFanartEnabled() {
        return fanartEnabled.get();
    }

    public void setLocalFanartEnabled(boolean fanartEnabled) {
        this.localFanartEnabled.set(fanartEnabled);
    }

    public boolean getLocalFanartEnabled() {
        return localFanartEnabled.get();
    }

    public boolean getUseSeason() {
        return useSeason.get();
    }

    public void setAutomatedFanartEnabled(boolean automatedFanart) {
        this.automatedFanartEnabled.set(automatedFanart);
    }

    public boolean isAutomatedFanartEnabled() {
        return automatedFanartEnabled.get();
    }

    public void setPreserverRecordingMetadata(boolean preserverRecordingMetadata) {
        this.preserverRecordingMetadata.set(preserverRecordingMetadata);
    }

    public boolean getPreserverRecordingMetadata() {
        return preserverRecordingMetadata.get();
    }


    public void setEnableSystemMessagesForTVEpisodeGaps(boolean enableSystemMessagesForTVEpisodeGaps) {
        this.enableSystemMessagesForTVEpisodeGaps.set(enableSystemMessagesForTVEpisodeGaps);
    }

    public boolean getEnableSystemMessagesForTVEpisodeGaps() {
        return enableSystemMessagesForTVEpisodeGaps.get();
    }

    public void setEnableSystemMessagesForFailures(boolean enableSystemMessagesForFailures) {
        this.enableSystemMessagesForFailures.set(enableSystemMessagesForFailures);
    }

    public boolean getEnableSystemMessagesForFailures() {
        return enableSystemMessagesForFailures.get();
    }

    public void setScannableMediaTypes(String scannableMediaTypes) {
        this.scannableMediaTypes.set(scannableMediaTypes);
    }

    public String getScannableMediaTypes() {
        return scannableMediaTypes.get();
    }

    public void setExcludePattern(String excludePattern) {
        this.excludePattern.set(excludePattern);
    }

    public String getExcludePattern() {
        return excludePattern.get();
    }

    public void setStackingModelRegex(String stackingModelRegex) {
        this.stackingModelRegex.set(stackingModelRegex);
    }

    public String getStackingModelRegex() {
        return stackingModelRegex.get();
    }

    public boolean getImportTVAsRecordings() {
        return importTVAsRecordings.get();
    }

    public void setImportTVAsRecordings(boolean importTVAsRecordings) {
        this.importTVAsRecordings.set(importTVAsRecordings);
    }

    public boolean getArchiveRecordings() {
        return archiveRecordings.get();
    }

    public void setArchiveRecordings(boolean archiveRecordings) {
        this.archiveRecordings.set(archiveRecordings);
    }

    public boolean getPrefixVideosWithRelativePath() {
        return prefixVideosWithRelativePath.get();
    }

    public void setPrefixVideosWithRelativePath(boolean prefixVideosWithRelativePath) {
        this.prefixVideosWithRelativePath.set(prefixVideosWithRelativePath);
    }

    public int getAutomaticRetryCount() {
        return automaticRetryCount.get();
    }

    public void setAutomaticRetryCount(int automaticRetryCount) {
        this.automaticRetryCount.set(automaticRetryCount);
    }

    public int getAutomaticRetryThreadCount() {
        return automaticRetryThreadCount.get();
    }

    public void setAutomaticRetryThreadCount(int automaticRetryThreadCount) {
        this.automaticRetryThreadCount.set(automaticRetryThreadCount);
    }

    public int getAutomaticRetryDelay() {
        return automaticRetryDelay.get();
    }

    public void setAutomaticRetryDelay(int automaticRetryDelay) {
        this.automaticRetryDelay.set(automaticRetryDelay);
    }

    public int getDeleteImagesSmallerThan() {
        return deleteImagesSmallerThan.get();
    }

    public void setDeleteImagesSmallerThan(int deleteImagesSmallerThan) {
        this.deleteImagesSmallerThan.set(deleteImagesSmallerThan);
    }

    public boolean getFetchQuotesAndTrivia() {
        return fetchQuotesAndTrivia.get();
    }

    public void setFetchQuotesAndTrivia(boolean val) {
        fetchQuotesAndTrivia.set(val);
    }

    public void setFillInMissingRecordingMetadata(boolean val) {
        fillInMissingRecordingMetadata.set(val);
    }

    public boolean getFillInMissingRecordingMetadata() {
        return fillInMissingRecordingMetadata.get();
    }

    public FieldProxy<String> getRescaleFanartAction() {
        return rescaleFanart;
    }
}
