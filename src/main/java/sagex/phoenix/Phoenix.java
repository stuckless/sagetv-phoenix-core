package sagex.phoenix;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.api.Configuration;
import sagex.api.Global;
import sagex.api.WidgetAPI;
import sagex.phoenix.configuration.ConfigurationManager;
import sagex.phoenix.configuration.ConfigurationMetadataManager;
import sagex.phoenix.configuration.impl.SageConfigurationProvider;
import sagex.phoenix.download.DownloadManager;
import sagex.phoenix.event.EventBus;
import sagex.phoenix.event.PhoenixEventID;
import sagex.phoenix.event.SageSystemMessageListener;
import sagex.phoenix.event.SystemMessageID;
import sagex.phoenix.image.TransformFactory;
import sagex.phoenix.menu.MenuManager;
import sagex.phoenix.metadata.MetadataManager;
import sagex.phoenix.metadata.RatingsManager;
import sagex.phoenix.metadata.XbmcScraperMetadataProviderConfiguration;
import sagex.phoenix.metadata.search.FileMatcherManager;
import sagex.phoenix.metadata.search.MovieScraperManager;
import sagex.phoenix.metadata.search.SearchQueryFactory;
import sagex.phoenix.metadata.search.TVScraperManager;
import sagex.phoenix.profiles.Profile;
import sagex.phoenix.profiles.ProfileManager;
import sagex.phoenix.remote.services.ScriptingServiceFactory;
import sagex.phoenix.remote.streaming.MediaProcessFactory;
import sagex.phoenix.remote.streaming.MediaStreamerConfig;
import sagex.phoenix.remote.streaming.MediaStreamerManager;
import sagex.phoenix.skins.SkinManager;
import sagex.phoenix.stv.OnlineVideoPlaybackManager;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.TaskManager;
import sagex.phoenix.util.url.CachedUrlCleanupTask;
import sagex.phoenix.vfs.VFSManager;
import sagex.phoenix.vfs.ov.OnlineVideosUrlResolverManager;
import sagex.util.Log4jConfigurator;

/**
 * Phoenix is the main entry point into getting various core sub managers, such
 * as Menu Manager, Configuration Metadata, etc.
 * <p/>
 * The Phoenix Home Dir can be set to an alternat location (for testing) using
 * the System property, "<b>phoenix/homeDir</b>".
 *
 * @author seans
 */
public class Phoenix {
    // some tests set this to true to prevent some things from starting in TEST
    // mode
    private static boolean TESTING = false;
    private static boolean STANDALONE = false;

    /**
     * Default UserData area, if not overridden * {@value}
     */
    public static final String DEFAULT_USERDATA = "userdata/Phoenix";

    private static enum State {
        Online, Offline
    }

    ;

    private static State state = State.Offline;

    private static final List<Runnable> onLoadList = new ArrayList<Runnable>();

    private static final Phoenix INSTANCE = new Phoenix();

    static {
        // initialize phoenix
        try {
            INSTANCE.init();
            INSTANCE.initServices();
        } catch (Exception e) {
            Loggers.LOG.error("Failed to load phoenix.", e);
        }
    }

    public static Phoenix getInstance() {
        return INSTANCE;
    }

    // create a simple Thread worker for doing misc tasks.
    private ExecutorService timer = Executors.newFixedThreadPool(5);

    private Logger log = Logger.getLogger(this.getClass());

    private MenuManager menuManager = null;
    private ConfigurationMetadataManager configurationMetadataManager = null;
    private ConfigurationManager configurationManager = null;
    private TransformFactory transformFactory = null;
    private SkinManager skinManager = null;
    private VFSManager vfsManager = null;

    private ProfileManager profileManager = new ProfileManager();
    private EventBus eventBus = new EventBus();
    private TaskManager taskManager = new TaskManager();
    private FileMatcherManager fileMatcher = null;

    private MetadataManager metadataManager = null;
    private DownloadManager downloadManager = null;
    private RatingsManager ratingsManager = null;

    private MovieScraperManager movieFilenameScrapers = null;
    private TVScraperManager tvFilenameScrapers = null;

    private SearchQueryFactory searchQueryFactory = new SearchQueryFactory();

    private OnlineVideosUrlResolverManager urlResolverManager;

    private OnlineVideoPlaybackManager onlinePlaybackManager;

    private MediaStreamerManager mediaStreamingManager;

    private ScriptingServiceFactory scriptingSerivesFactory;

    protected Phoenix() {
        STANDALONE = BooleanUtils.toBoolean(System.getProperty("phoenix/standalone"));
        TESTING = BooleanUtils.toBoolean(System.getProperty("phoenix/testing"));
    }

    public static boolean isStandalone() {
        return STANDALONE;
    }

    protected void init() {
        try {
            Log4jConfigurator.configureQuietly("phoenix", this.getClass().getClassLoader());
            Log4jConfigurator.configureQuietly("phoenix-metadata", this.getClass().getClassLoader());
            log.info("Initializing Phoenix - Version: " + phoenix.system.GetVersion());
            log.info("Java classpath: " + System.getProperty("java.class.path"));
            log.info("Java Impl: " + System.getProperty("java.vendor") + " - " + System.getProperty("java.version"));
            log.info("OS: " + System.getProperty("os.name") + " - " + System.getProperty("os.arch") + " - "
                    + System.getProperty("os.version"));
            log.info("User: " + System.getProperty("user.name"));

            // register the SystemEvent handler
            log.info("Registering System Message Handler to the Event Bus");
            eventBus.addListener(PhoenixEventID.SystemMessageEvent, new SageSystemMessageListener());

            try {
                // Configuration metadata should be loaded first, since just
                // about everything uses it.
                // IT MUST NEVER call Phoenix.getInstance(), or else it will
                // lock the system.
                configurationMetadataManager = new ConfigurationMetadataManager(getPhoenixConfigurationMetadataDir(), new File(
                        getPhoenixUserDir(), "Configuration"));
                configurationMetadataManager.loadConfigurations();
                configurationManager = new ConfigurationManager(configurationMetadataManager, new SageConfigurationProvider());
                log.info("Configuration Metadata Initialized");
            } catch (Throwable t) {
                log.warn("Failed to load the Configuration Metadata.  Configuration Operations will fail.", t);
                t.printStackTrace();
            }

            // create the managers, but don't initialize them, until
            // initServices() is called.
            vfsManager = new VFSManager(getVFSDir(), new File(getPhoenixUserDir(), "vfs"));

            if (!STANDALONE) {
                menuManager = new MenuManager(getMenusDir(), new File(getPhoenixUserDir(), "Menus"));
            }

            transformFactory = new TransformFactory(getJavascriptImageTransformDir());

            if (!STANDALONE) {
                skinManager = new SkinManager(getPhoenixSkinsDir(), new File(getPhoenixUserDir(), "Skins"));
            }

            metadataManager = new MetadataManager(getPhoenixMetadataDir(), new File(getPhoenixUserDir(), "metadata"));

            metadataManager.addConfiguration(new XbmcScraperMetadataProviderConfiguration(metadataManager, new File(
                    getScrapersDir(), "xbmc/video"), new File(getPhoenixUserDir(), "scrapers/xbmc/video")));

            movieFilenameScrapers = new MovieScraperManager(new File(getScrapersDir(), "xbmc/moviefilenames"), new File(
                    getPhoenixUserDir(), "scrapers/xbmc/moviefilenames"));
            tvFilenameScrapers = new TVScraperManager(new File(getScrapersDir(), "xbmc/tvfilenames"), new File(getPhoenixUserDir(),
                    "scrapers/xbmc/tvfilenames"));

            downloadManager = new DownloadManager();

            ratingsManager = new RatingsManager(getPhoenixMetadataDir(), new File(getPhoenixUserDir(), "metadata"));

            fileMatcher = new FileMatcherManager(getScrapersDir(), new File(getPhoenixUserDir(), "scrapers"));

            urlResolverManager = new OnlineVideosUrlResolverManager(getDir(new File(getPhoenixRootDir(), "urlresolvers")),
                    new File(getPhoenixUserDir(), "urlresolvers"));

            onlinePlaybackManager = new OnlineVideoPlaybackManager();
            getEventBus().addListener(onlinePlaybackManager);

            mediaStreamingManager = new MediaStreamerManager(new MediaStreamerConfig(), new MediaProcessFactory());

            scriptingSerivesFactory = new ScriptingServiceFactory();
        } catch (Throwable t) {
            log.error("Phoenix Failed to initialize correctly.  Phoenix will most like not function correctly.", t);
            t.printStackTrace();
        } finally {
            fireOnLoadEvents();
            log.info("Phoenix base system initialized.");
        }

        state = State.Online;
    }

    /**
     * Init services is called when running in plugin mode. This will load the
     * various configurations.
     */
    public synchronized void initServices() {
        try {
            log.info("Initializing Phoenix Services");

            try {
                vfsManager.loadConfigurations();
                eventBus.addListener(vfsManager);
                log.info("VFS Initialized");
            } catch (Throwable t) {
                log.warn("Failed to load the VFS.  VFS Operations will fail.", t);
            }

            if (!STANDALONE) {
                try {
                    menuManager.loadConfigurations();
                    log.info("Menus Initialized");
                } catch (Throwable t) {
                    log.warn("Failed to load the Dynamic Menus.  Dynamic Menus Operations will fail.", t);
                    t.printStackTrace();
                }
            }

            if (!STANDALONE) {
                try {
                    skinManager.loadConfigurations();
                    log.info("Skins Initialized");
                } catch (Exception t) {
                    log.warn("Failed to load the Skin/Theme Manager.  Some Skin Operations will fail.", t);
                    t.printStackTrace();
                }
            }

            try {
                metadataManager.loadConfigurations();
                log.info("Metadata Scrapers Initialized");
            } catch (Exception t) {
                log.warn("Failed to load the Metadata/Fanart Manager.  Some Metadata Operations will fail.", t);
                t.printStackTrace();
            }

            try {
                movieFilenameScrapers.loadConfigurations();
                log.info("Movie Filename Scrapers Initialized");
            } catch (Exception t) {
                log.warn("Failed to load the Movie Filename Scrapers.  Some Metadata Operations will fail.", t);
                t.printStackTrace();
            }

            try {
                tvFilenameScrapers.loadConfigurations();
                log.info("TV Filename Scrapers Initialized");
            } catch (Exception t) {
                log.warn("Failed to load the TV Filename Scrapers.  Some Metadata Operations will fail.", t);
                t.printStackTrace();
            }

            try {
                ratingsManager.loadConfigurations();
                log.info("Ratings Manager Initialized");
            } catch (Exception e) {
                log.warn("Failed to load the ratings manager.  Rating mappings will fail.", e);
            }

            try {
                fileMatcher.loadConfigurations();
                log.info("MediaTitles.xml Initialized");
            } catch (Exception t) {
                log.warn("Failed to load the MediaTitles Manager.  Some Metadata Operations will fail.", t);
                t.printStackTrace();
            }

            try {
                urlResolverManager.loadConfigurations();
                log.info("URL Resolvers has been loaded");
            } catch (Exception e) {
                log.warn("Failed to load url resolvers", e);
            }

            try {
                scriptingSerivesFactory.initialize();
                log.info("Scripting Services Factory initialized");
            } catch (Exception e) {
                log.warn("Failed to initialize Scripting services", e);
            }

            // add in task monitoring...
            getTaskManager().scheduleTask(CachedUrlCleanupTask.TaskID, new CachedUrlCleanupTask(),
                    Calendar.getInstance().getTime(), 24 * 60 * 60 * 1000);
            log.info("Core Scheduled Tasks Initialized");

            if (!(STANDALONE || TESTING)) {
                // initialize weather
                if (!phoenix.weather.IsConfigured()) {
                    try {
                        try {
                            Object gweather = WidgetAPI.EvaluateExpression("sage_google_weather_GoogleWeather_getInstance()");
                            if (gweather != null) {
                                // configure using google weather
                                Global.AddGlobalContext("PHOENIX_GOOGLE_WEATHER", gweather);
                                String loc = (String) WidgetAPI
                                        .EvaluateExpression("sage_google_weather_GoogleWeather_getNWSZipCode(PHOENIX_GOOGLE_WEATHER)");
                                if (StringUtils.isEmpty(loc)) {
                                    loc = (String) WidgetAPI
                                            .EvaluateExpression("sage_google_weather_GoogleWeather_getGoogleWeatherLoc(PHOENIX_GOOGLE_WEATHER)");
                                }
                                phoenix.weather.SetLocation(loc);
                                Global.AddGlobalContext("PHOENIX_GOOGLE_WEATHER", null);
                            }
                        } catch (Throwable t) {
                            log.warn("Nothing to be be too concerned about... Tried to use google weather, at doesn't appear to be there, yet.");
                        }

                        if (!phoenix.weather.IsConfigured()) {
                            log.info("Configuring weather using EPG zip code");
                            String zip = Configuration.GetServerProperty("epg/zip_code", null);
                            if (!StringUtils.isEmpty(zip)) {
                                phoenix.weather.SetLocation(zip);
                            }
                        }
                    } catch (Throwable e) {
                        log.warn("Failed to auto set the weather location", e);
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Phoenix Failed to initialize Phoenix Services, some things may not work.", t);
            t.printStackTrace();
        } finally {
            fireOnLoadEvents();
            log.info("Phoenix Services initialized.");
        }
    }

    protected void shutdown() {
        try {
            timer.shutdownNow();
        } catch (Exception e) {
            // don't care, we are shutting down
        }

        skinManager.stopPlugins();
    }

    public TransformFactory getTransformFactory() {
        return transformFactory;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public OnlineVideosUrlResolverManager getOnlineVideosUrlResolverManager() {
        return urlResolverManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public ConfigurationMetadataManager getConfigurationMetadataManager() {
        return configurationMetadataManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public Profile getCurrentProfile() {
        return getProfileManager().getCurrentProfile();
    }

    private File getDir(File dir) {
        if (!dir.exists()) {
            FileUtils.mkdirsQuietly(dir);
        }
        return dir;
    }

    public File getPhoenixRootDir() {
        return new File(getSageTVRootDir(), System.getProperty("phoenix/homeDir", "STVs/Phoenix"));
    }

    public File getPhoenixUserDir() {
        return new File(getSageTVRootDir(), System.getProperty("phoenix/userDir",
                Configuration.GetProperty("phoenix/userDir", DEFAULT_USERDATA)));
    }

    public String getUserPath(String path) {
        return new File(getPhoenixUserDir(), path).getPath();
    }

    public File getSageTVRootDir() {
        return new File(System.getProperty("phoenix/sagetvHomeDir", "."));
    }

    public File getPhoenixConfigurationMetadataDir() {
        return getDir(new File(getPhoenixRootDir(), "Configuration"));
    }

    public File getPhoenixMetadataDir() {
        return getDir(new File(getPhoenixRootDir(), "metadata"));
    }

    public File getJavascriptImageTransformDir() {
        return getDir(new File(getPhoenixRootDir(), "ImageTransforms"));
    }

    public File getMenusDir() {
        return getDir(new File(getPhoenixRootDir(), "Menus"));
    }

    public File getVFSDir() {
        return getDir(new File(getPhoenixRootDir(), "vfs"));
    }

    public File getPhoenixSkinsDir() {
        return getDir(new File(getPhoenixRootDir(), "Skins"));
    }

    public File getScrapersDir() {
        return getDir(new File(getPhoenixRootDir(), "scrapers"));
    }

    public File getUserCacheDir() {
        return getDir(new File(getPhoenixUserDir(), "cache"));
    }

    public SkinManager getSkinManager() {
        return skinManager;
    }

    public OnlineVideoPlaybackManager getOnlineVideoPlaybackManager() {
        return onlinePlaybackManager;
    }

    public VFSManager getVFSManager() {
        return vfsManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * @return the taskManager
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * get the Metadata/Fanart manager
     *
     * @return
     */
    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    /**
     * Returns the Manager for the Media Titles xml
     *
     * @return
     */
    public FileMatcherManager getMediaTitlesManager() {
        return fileMatcher;
    }

    /**
     * Pushes an error message into the Event System, where someone may pick it
     * up
     *
     * @param msg
     * @param t
     */
    public static void fireError(String msg, Throwable t) {
        if (state == State.Online) {
            INSTANCE.getEventBus().fireEvent(
                    PhoenixEventID.SystemMessageEvent,
                    SageSystemMessageListener.createEvent(SystemMessageID.PHOENIX_GENERAL_ERROR, SageSystemMessageListener.ERROR,
                            "Phoenix Error", msg, t), false);
        } else {
            Loggers.LOG.warn("fireError(): " + msg, t);
        }
    }

    /**
     * Return the DownloadManager instance for all phoenix download operations
     *
     * @return
     */
    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    /**
     * Return the {@link RatingsManager} instance that is used to map parental
     * ratings.
     *
     * @return
     */
    public RatingsManager getRatingsManager() {
        return ratingsManager;
    }

    /**
     * Return the {@link TVFileNameUtils} scrapers for processing TV Filenames.
     *
     * @return
     */
    public TVScraperManager getTVScrapers() {
        return tvFilenameScrapers;
    }

    /**
     * return the {@link MovieFileNameUtils} scrapers for processing Movie
     * Filenames.
     *
     * @return
     */
    public MovieScraperManager getMovieScrapers() {
        return movieFilenameScrapers;
    }

    /**
     * return the {@link SearchQueryFactory} used for creating new metadata
     * lookups
     *
     * @return
     */
    public SearchQueryFactory getSearchQueryFactory() {
        return searchQueryFactory;
    }

    /**
     * Adds a task to phoenix to run once Phoenix has been loaded. If phoenix is
     * loaded, then the task is executed immediately.
     *
     * @param r
     */
    public static void addOnLoad(Runnable r) {
        if (state == State.Online) {
            Loggers.LOG.info("Firing new onload task, now since Phoenix is started.");
            r.run();
        } else {
            Loggers.LOG.info("Adding new onload task, to fire when Phoenix is started.");
            onLoadList.add(r);
        }
    }

    /**
     * Fires the onload events, and then clears the list.
     */
    protected synchronized void fireOnLoadEvents() {
        if (onLoadList.size() > 0) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Firing onLoad tasks in a new thread...");
                    for (Runnable r : onLoadList) {
                        r.run();
                    }
                    onLoadList.clear();
                    log.info("Done Firing onLoad tasks");
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * Gets a cache file from the the user's cache area for the given folder and
     * filepath and name.
     *
     * @param folder   cache sub folder, ie, 'videos', 'tv', 'web', 'images', etc
     * @param filepath file url/filepath being cached, ie,
     *                 'http://some.onlinevideo.com/videos/videofile.mp4'
     * @return File of the cached entity or null if a cached entity could be
     * created.
     */
    public File getUserCacheEntry(String folder, String filepath) {
        if (filepath == null)
            return null;
        File cache = new File(getUserCacheDir(), folder);
        String cachePrefix = DigestUtils.md5Hex(filepath);
        cache = new File(cache, cachePrefix.charAt(0) + "/" + cachePrefix.charAt(1) + "/" + cachePrefix.charAt(2));
        if (!cache.exists()) {
            if (!cache.mkdirs()) {
                log.warn("Failed to create cache dir " + cache);
                return null;
            }
        }
        String name = cachePrefix + "." + FilenameUtils.getExtension(filepath);
        cache = new File(cache, name);
        return cache;
    }

    /**
     * The following running will run in a background thread, when there is
     * time. Typically these tasks should not run for a long time, ie, a few
     * seconds at most, since the entire background thread pool will serialize
     * these requests such that there is only a couples of tasks running at a
     * time.
     *
     * @param r
     */
    public void invokeLater(Runnable r) {
        timer.submit(r);
    }

    public MediaStreamerManager getMediaStreamer() {
        return mediaStreamingManager;
    }

    public ScriptingServiceFactory getScriptingServices() {
        return scriptingSerivesFactory;
    }
}
