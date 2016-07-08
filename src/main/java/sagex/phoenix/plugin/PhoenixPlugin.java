package sagex.phoenix.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import sage.SageTVEventListener;
import sage.SageTVPluginRegistry;
import sagex.api.Configuration;
import sagex.api.MediaFileAPI;
import sagex.api.PluginAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.common.HasFileConfigurations;
import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.configuration.ConfigurationManager;
import sagex.phoenix.configuration.Group;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.event.PhoenixEventID;
import sagex.phoenix.event.SageEventBus;
import sagex.phoenix.event.SageSystemMessageListener;
import sagex.phoenix.event.SystemMessageID;
import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.ISageCustomMetadataRW;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataHints;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.task.ITaskOperation;
import sagex.phoenix.task.ITaskProgressHandler;
import sagex.phoenix.task.RetryTaskManager;
import sagex.phoenix.task.TaskItem;
import sagex.phoenix.util.Hints;
import sagex.phoenix.util.LogUtil;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.PropertiesUtils;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.filters.HomeVideosConfiguration;
import sagex.phoenix.vfs.filters.HomeVideosFilter;
import sagex.phoenix.vfs.sage.SageMediaFile;
import sagex.phoenix.vfs.util.PathUtils;
import sagex.plugin.AbstractPlugin;
import sagex.plugin.ButtonClickHandler;
import sagex.plugin.PluginProperty;
import sagex.plugin.SageEvent;
import sagex.plugin.SageEvents;

/**
 * Sage7 Plugin for Phoenix
 * 
 * @author seans
 */
public class PhoenixPlugin extends AbstractPlugin implements ITaskOperation, ITaskProgressHandler {
	private MetadataConfiguration config = null;
	private RetryTaskManager retryTaskManager = null;
	private HomeVideosFilter homeVideoFilter = new HomeVideosFilter();
	private HomeVideosConfiguration homeVideoCfg = null;

	public PhoenixPlugin(SageTVPluginRegistry registry) {
		super(registry);
	}

	@SageEvent(value = SageEvents.ImportingCompleted, background = true)
	public void importingCompleted(@SuppressWarnings("rawtypes") Map vars) {
		checkForMissingEpisodes();
	}
	
	//if the system level configuration options is enabled we will check for missing episodes
	private void checkForMissingEpisodes(){
		checkForMissingEpisodes(null, null);
	}
	private void checkForMissingEpisodes(String seriesID, String seasonNum){
		if (config.getEnableSystemMessagesForTVEpisodeGaps()) {
			String BaseView = "phoenix.view.util.missingEpisodes";
			Map<String,Object> viewOptions = new HashMap<String,Object>();
			IMediaFolder folder = null;
			if (seriesID==null){
				folder = phoenix.umb.CreateView(BaseView);
			}else{
				viewOptions.put("seriesID", seriesID);
				viewOptions.put("seasonNum", seasonNum);
				folder = phoenix.umb.CreateView(BaseView, viewOptions);
			}
			if (folder.getChildren().isEmpty()){
				LogUtil.logTVEpisodeGapReview("Missing episode review found NO missing episodes");
				return;
			}
			for (IMediaResource show : folder.getChildren()) {
				//first level is the show
				StringBuilder sb = new StringBuilder();
				for (IMediaResource episode : phoenix.media.GetChildren(show)) {
					sb.append("S").append(phoenix.metadata.GetSeasonNumber(episode));
					sb.append("E").append(phoenix.metadata.GetEpisodeNumber(episode)).append(";");
					if (phoenix.media.GetChildren(show).size()<=7){
						sb.append(phoenix.metadata.GetEpisodeName(episode));
						sb.append("\n");
					}
				}
				//raise an event for each show that has missing episodes
				Phoenix.getInstance()
				.getEventBus()
				.fireEvent(
						PhoenixEventID.SystemMessageEvent,
						SageSystemMessageListener.createEvent(SystemMessageID.PHOENIX_MISSING_EPISODES,
								SageSystemMessageListener.INFO, "Missing Episodes: " + show.getTitle() ,
								sb.toString(), null), false);
				LogUtil.logTVEpisodeGapReview("System message created for missing episodes for show: " + show.getTitle());
			}
		}
	}
	
	@SageEvent(value = SageEvents.MediaFileImported, background = true)
	public void mediaFileImported(@SuppressWarnings("rawtypes") Map vars) {
		if (config != null && config.isAutomatedFanartEnabled()) {
			Object mediaFile = vars.get("MediaFile");
			if (mediaFile == null) {
				log.warn("MediaFileImported was called, but no mediafile was passed");
				return;
			}

			if (MediaFileAPI.IsPictureFile(mediaFile)) {
				// just skip images
				return;
			}

			SageMediaFile smf = new SageMediaFile(null, mediaFile);
			File propFile = FanartUtil.resolvePropertiesFile(PathUtils.getFirstFile(smf));
			if (propFile != null && propFile.exists()) {
				// Assume that SageTV has ALREADY updated the metadata, or
				// something else has.
				// but check to see if we should see if the watched/library
				// flags are set.
				// X- flags are not processed by sagetv's native metadata
				// parser, so we
				// update those X- flags from here
				try {
					Properties props = PropertiesUtils.load(propFile);
					if (props.containsKey(IMetadata.XWatched)) {
						smf.setWatched(BooleanUtils.toBoolean(props.getProperty(IMetadata.XWatched)));
					}

					if (props.containsKey(IMetadata.XLibraryFile)) {
						smf.setLibraryFile(BooleanUtils.toBoolean(props.getProperty(IMetadata.XLibraryFile)));
					}
				} catch (IOException e) {
				}

				// don't process other metadata
				return;
			}

			// check for home videos
			if (homeVideoFilter.accept(smf)) {
				// we have a home video

				if (!StringUtils.isEmpty(homeVideoCfg.getCategory())) {
					// assign the home video category to this
					smf.getMetadata().getGenres().add(homeVideoCfg.getCategory());
				}

				return;
			}

			// regular file, let's update.
			LogUtil.logAutoUpdate("MEDIA", smf);
			updateMetadata(smf, false);
		}
	}

	@SageEvent(value = SageEvents.RecordingCompleted, background = true)
	public void recordingCompleted(@SuppressWarnings("rawtypes") Map vars) {
		if (config != null && config.isAutomatedFanartEnabled()) {
			Object mediaFile = vars.get("MediaFile");
			if (mediaFile == null) {
				log.warn("MediaFileImported was called, but no mediafile was passed");
				return;
			}

			SageMediaFile smf = new SageMediaFile(null, mediaFile);
			LogUtil.logAutoUpdate("RECORDING", smf);
			updateMetadata(smf, true);
			//see if this is a TV item and if so check for missing episodes
			if(MediaFileAPI.IsTVFile(mediaFile)){
				String seriesID = phoenix.metadata.GetMediaProviderDataID(smf);
				Integer seasonNum = phoenix.metadata.GetSeasonNumber(smf);
				checkForMissingEpisodes(seriesID, seasonNum.toString());
			}
		}
	}

	private void updateMetadata(IMediaFile file, boolean recording) {
		try {
			Hints options = Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions();
			options.setBooleanHint(MetadataHints.KNOWN_RECORDING, recording);
			options.setBooleanHint(MetadataHints.AUTOMATIC, true);

			if (Phoenix.getInstance().getMetadataManager().canScanMediaFile(file, options)) {
				Phoenix.getInstance().getMetadataManager().automaticUpdate(file, options);
			} else {
				LogUtil.logMetadataSkipped(file);
			}
		} catch (Exception me) {
			if (canRetry(me)) {
				log.info("Automatic Metadata Failed. Will try to requeued for later for " + file);
				TaskItem ti = new TaskItem();
				ti.getUserData().put("file", file);
				ti.getUserData().put("recording", recording);
				ti.setHandler(this);
				retryTaskManager.performTask(ti, this);
			} else {
				reportFailure(file, me);
			}
		}
	}

	private void reportFailure(IMediaFile file, Throwable e) {
		LogUtil.logMetadataUpdatedError(file, e);
		if (config.getEnableSystemMessagesForFailures()) {
			Phoenix.getInstance()
					.getEventBus()
					.fireEvent(
							PhoenixEventID.SystemMessageEvent,
							SageSystemMessageListener.createEvent(SystemMessageID.AUTOMATIC_METADATA_LOOKUP_FAILED,
									SageSystemMessageListener.STATUS, "Automatic Metadata Failed: " + file.getTitle(),
									e.getMessage(), e), false);
		}

	}

	@SageEvent(value = SageEvents.AllPluginsLoaded, background = false)
	public void onPluginsLoaded() {
		log.info("Begin: Phoenix looking for plugins that contribute to the Phoenix Core...");
		List<HasFileConfigurations> managers = new ArrayList<HasFileConfigurations>();

		try {
			Object[] plugins = PluginAPI.GetAllAvailablePlugins();
			if (plugins != null && plugins.length > 0) {
				for (Object plugin : plugins) {
					File dir = null;
					String pluginArea = PluginAPI.GetPluginResourcePath(plugin);
					if (pluginArea != null) {
						dir = new File(pluginArea);
					}
					if (dir == null || !dir.exists()) {
						dir = new File(new File("plugins"), PluginAPI.GetPluginIdentifier(plugin));
					}
					if (dir == null || !dir.exists()) {
						// skip this plugin, it has nothing to offer
						continue;
					}

					dir = new File(dir, "Phoenix");
					if (dir == null || !dir.exists()) {
						// skip this plugin, it has no phoenix contribution
						continue;
					}

					managePlugin(new File(dir, "Configuration"), Phoenix.getInstance().getConfigurationMetadataManager(), managers);
					managePlugin(new File(dir, "vfs"), Phoenix.getInstance().getVFSManager(), managers);
					managePlugin(new File(dir, "Menus"), Phoenix.getInstance().getMenuManager(), managers);
					managePlugin(new File(dir, "metadata"), Phoenix.getInstance().getMetadataManager(), managers);
					managePlugin(new File(dir, "Skins"), Phoenix.getInstance().getSkinManager(), managers);
					managePlugin(new File(dir, "scrapers"), Phoenix.getInstance().getMediaTitlesManager(), managers);
					managePlugin(new File(dir, "scrapers/movies"), Phoenix.getInstance().getMovieScrapers(), managers);
					managePlugin(new File(dir, "scrapers/tv"), Phoenix.getInstance().getTVScrapers(), managers);
				}
			}

			if (managers.size() > 0) {
				log.info("Begin Reloading some configurations because of plugin contributions");
				for (HasFileConfigurations m : managers) {
					m.loadConfigurations();
				}
				log.info("End Reloading some configurations because of plugin contributions");
			}
		} catch (Throwable t) {
			log.warn("Phoenix failed to discover additional phoenix enhancements from other plugins", t);
		} finally {
			log.info("Reloading Phoenix Services in case Plugins have contributed.");
			// now that the plugins are loaded, start the services.
			Phoenix.getInstance().initServices();
		}

		log.info("End: Phoenix looking for plugins that contribute to the Phoenix Core");
	}

	private void managePlugin(File dir, SystemConfigurationFileManager manager, List<HasFileConfigurations> managers) {
		if (dir.exists()) {
			manager.addPluginConfiguration(dir);
			managers.add(manager);
		}
	}

	@Override
	public void start() {
		try {
			log.info("Phoenix Plugin starting...");
			super.start();

			try {
				log.info("Registering SageTV EventListener in Phoenix...");
				// bind sagetv event system to phoenix
				Phoenix.getInstance().getEventBus().setEventBus(new SageEventBus(pluginRegistry));

				// get the plugin configuration
				Group el = (Group) Phoenix.getInstance().getConfigurationMetadataManager().findElement("phoenix");
				PluginConfigurationHelper.addConfiguration(this, el);
				config = GroupProxy.get(MetadataConfiguration.class);
				retryTaskManager = new RetryTaskManager(config.getAutomaticRetryCount(), config.getAutomaticRetryThreadCount(),
						config.getAutomaticRetryDelay());
				homeVideoCfg = GroupProxy.get(HomeVideosConfiguration.class);

				// register ourself to listen configuration button events
				// and dispatch them to the setConfigValue so that it triggers
				// an button event
				Phoenix.getInstance().getEventBus().addListener(ConfigurationManager.BUTTON_EVENT, new SageTVEventListener() {
					@Override
					public void sageEvent(String evt, Map args) {
						if (ConfigurationManager.BUTTON_EVENT.equals(evt)) {
							setConfigValue((String) args.get(ConfigurationManager.EVENT_PROPERTY), "true");
						}
					}
				});
			} catch (Throwable t) {
				t.printStackTrace();
			}

			if (StringUtils.isEmpty((String) phoenix.config.GetServerProperty("phoenix/configured"))) {
				phoenix.config.SetServerProperty("phoenix/configured", String.valueOf(Calendar.getInstance().getTime().getTime()));
				try {
					upgradeFromBMT();
				} catch (Throwable t) {
					log.warn("Failed to upgrade BMT to Phoenix", t);
				}
			}
			updateCustomMetadataFields();
		} catch (Throwable e) {
			e.printStackTrace();
			log.warn("Phoenix Plugin failed to start!", e);
		}
	}

	public static void updateCustomMetadataFields() {
		String fieldProp = Configuration.GetServerProperty("custom_metadata_properties", "");
		String fields[] = fieldProp.split(";");
		Set<String> fieldList = new TreeSet<String>(Arrays.asList(fields));

		// remove al the know props
		// sean 2010-01-08
		// disabling this because sagetv apparently adds the fields
		// automatically
		// String all[] = MetadataUtil.getPropertyKeys(IMetadata.class);
		// for (String s : all) {
		// fieldList.remove(s);
		// }

		// add known custom props
		String custom[] = MetadataUtil.getPropertyKeys(ISageCustomMetadataRW.class);
		for (String s : custom) {
			fieldList.add(s);
		}

		fieldProp = StringUtils.join(fieldList, ";");
		Configuration.SetServerProperty("custom_metadata_properties", fieldProp);
		Loggers.LOG.info("Setting Custom Metadata Fields: " + fieldProp);
	}

	private void upgradeFromBMT() {
		boolean autoplugin = false;

		// check if bmt is configured, and if so, then remove it.
		String plugin = Configuration.GetProperty("mediafile_metadata_parser_plugins", null);
		if (plugin != null && plugin.contains("org.jdna.sage.MetadataUpdaterPlugin")) {
			Configuration.SetProperty("mediafile_metadata_parser_plugins", null);
			autoplugin = true;
		}

		plugin = Configuration.GetServerProperty("mediafile_metadata_parser_plugins", null);
		if (plugin != null && plugin.contains("org.jdna.sage.MetadataUpdaterPlugin")) {
			Configuration.SetServerProperty("mediafile_metadata_parser_plugins", null);
			autoplugin = true;
		}

		if (autoplugin) {
			MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
			config.setAutomatedFanartEnabled(true);
		}

		removeFile("STVs/Phoenix/Configuration/ext/bmt.xml");
		removeFile("STVs/Phoenix/Configuration/ext/log4j.xml");
		removeFile("STVs/Phoenix/Configuration/ext/Sage.xml");
		removeFile("STVs/Phoenix/vfs/ext/bmt.xml");

		// reload configuration metadata and vfs, since it may have changed.
		Phoenix.getInstance().getConfigurationMetadataManager().loadConfigurations();
		Phoenix.getInstance().getVFSManager().loadConfigurations();

		// move the media titles, in case it's been created, modified.
		File titles = new File("scrapers/MediaTitles.xml");
		File newTitles = new File("STVs/Phoenix/scrapers/MediaTitles.xml");
		if (titles.exists() && !newTitles.exists()) {
			try {
				org.apache.commons.io.FileUtils.moveFile(titles, newTitles);
			} catch (IOException e) {
				log.warn("Failed to copy/move the MediaTitles.xml");
			}
		}

		File scrapers = new File("scrapers");
		if (scrapers.exists()) {
			File oldScrapers = new File("scrapers.old");
			boolean renamed = scrapers.renameTo(oldScrapers);
			if (!renamed) {
				log.warn("Failed to rename scraper dir: " + scrapers);
			}
		}

		// send a system message stating that we've upgrade the bmt plugin
		Phoenix.getInstance()
				.getEventBus()
				.fireEvent(
						PhoenixEventID.SystemMessageEvent,
						SageSystemMessageListener.createEvent(SystemMessageID.PHOENIX_METADATA, SageSystemMessageListener.INFO,
								"Batch Metadata Tools updated",
								"Phoenix Metadata has been installed and the legacy bmt files/plugin have been removed.", null),
						false);
	}

	private void removeFile(String f) {
		File file = new File(f);
		if (file.exists()) {
			if (!file.delete()) {
				file.deleteOnExit();
			}
		}
	}

	@Override
	public void onStart(TaskItem item) {
		// ignore, already logged
	}

	@Override
	public void onComplete(TaskItem item) {
		// ignore, it'll be logged when the item is written
		item.setHandler(null);
		item.setOperation(null);
		item.getUserData().clear();
	}

	@Override
	public void onError(TaskItem item) {
		// finally failed, so report the error now
		reportFailure((IMediaFile) item.getUserData().get("file"), item.getError());
		item.setHandler(null);
		item.setOperation(null);
		item.getUserData().clear();
	}

	@Override
	public void performAction(TaskItem item) throws Throwable {
		IMediaFile file = (IMediaFile) item.getUserData().get("file");
		boolean recording = (Boolean) item.getUserData().get("recording");
		log.info("Retrying Scan for " + file);

		Hints options = Phoenix.getInstance().getMetadataManager().getDefaultMetadataOptions();
		options.setBooleanHint(MetadataHints.KNOWN_RECORDING, recording);
		options.setBooleanHint(MetadataHints.AUTOMATIC, true);

		Phoenix.getInstance().getMetadataManager().automaticUpdate(file, options);
	}

	@Override
	public boolean canRetry(Throwable t) {
		if (t != null && t instanceof MetadataException) {
			return ((MetadataException) t).canRetry();
		}
		return false;
	}

	public RetryTaskManager getRetryTaskManager() {
		return retryTaskManager;
	}

	@ButtonClickHandler("phoenix/fanart/rescanFanart")
	public void rescanCollection() {
		log.info("Rescanning Missing Metadata items to update fanart and metadata");
		PluginProperty prop = getPluginPropertyForSetting("phoenix/fanart/rescanFanart");
		prop.setLabel("Scanning Fanart....");
		prop.setHelp("A scan is in progress... updateing progress...");
	}
}
