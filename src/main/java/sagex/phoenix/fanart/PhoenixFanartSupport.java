package sagex.phoenix.fanart;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.api.MediaFileAPI;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.ISageCustomMetadataRW;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.util.Utils;

/**
 * Central Folder V2 Support as defined the <a
 * href="http://forums.sagetv.com/forums/showthread.php?p=343902&postcount=35"
 * >SageTV Thread</a><br>
 * 
 * <pre>
 * Base Folders
 * CentralFolder\TV
 * CentralFolder\Movies
 * CentralFolder\Music
 * GentralFolder\Actors
 * CentralFolder\Genres
 * 
 * TV Folders
 * TV\SeriesTitle\seriesInfo.data (Text file containing base series information Description, Genre, etc)
 * TV\SeriesTitle\Backgrounds\(All Base Backgrounds Images)
 * TV\SeriesTitle\Posters\(All Base Posters Images)
 * TV\SeriesTitle\Banners\(All Base Banners Images)
 * TV\SeriesTitle\Actors\(All Base Actor Images)
 * TV\SeriesTitle\Season #\Backgrounds\(All Season Specific Backgrounds Images)
 * TV\SeriesTitle\Season #\Posters\(All Season Specific Posters Images)
 * TV\SeriesTitle\Season #\Banners\(All Season Specific Banners Images)
 * TV\SeriesTitle\Season #\Actors\(All Season Specific Actor Images)
 * TV\Genres\GenreName\(All GenreName Images)
 * 
 * Movies
 * Movies\MovieTitle\Backgrounds\(All Background Images)
 * Movies\MovieTitle\Posters\(All Poster Images)
 * Movies\MovieTitle\Banners\(All Banner Images)
 * Movies\MovieTitle\Actors\(All Actor Images)
 * Movies\Genres\Family\(All Family Genre Images)
 * 
 * Actors
 * GentralFolder\Actors\Actor Name\ActorName.jpg
 * 
 * Genres
 * GentralFolder\Genres\Genre Name\Posters\(All Genres images)
 * GentralFolder\Genres\Genre Name\Banners\(All Genres images)
 * GentralFolder\Genres\Genre Name\Backgrounds\(All Genres images)
 * 
 * </pre>
 * 
 * <br/>
 * To enable Central Fanart Support, set the following properties
 * 
 * <pre>
 * phoenix/mediametadata/fanartEnabled=true
 * phoenix/mediametadata/fanartCentralFolder=YOUR_CENTRAL_FOLDER
 * </pre>
 * 
 * <br/>
 * 
 * @author seans
 * 
 */
@Deprecated
public class PhoenixFanartSupport implements IFanartSupport {
	private static final Logger log = Logger
			.getLogger(PhoenixFanartSupport.class);

	private MetadataConfiguration fanartConfig = null;
	//private LocalFanartSupport localFanart = new LocalFanartSupport();
	
	public PhoenixFanartSupport() {
		fanartConfig = GroupProxy.get(MetadataConfiguration.class);
		initializeFanartFolder(fanartConfig.getFanartCentralFolder());
	}

	private void initializeFanartFolder(String dir) {
		log.info("Phoenix Fanart initializing");
		if (StringUtils.isEmpty(dir)) {
			dir = System.getProperty("user.dir") + File.separator + "STVs"
					+ File.separator + "Phoenix" + File.separator + "Fanart";
			fanartConfig.setCentralFanartFolder(dir);
		}
		File fanartFolder = new File(dir);
		if (!fanartFolder.exists()) {
			log.warn("Fanart folder does not exist, creating: " + fanartFolder);
			if (!fanartFolder.mkdirs()) {
				log.warn("Failed to create the fanart folder, this may be a permissions problem:  Folder; "
						+ fanartFolder);
			}
			if (!fanartFolder.canWrite()) {
				log.warn("You don't have permissions to write to your fanart folder: "
						+ fanartFolder);
			}
		}
		log.info("Phoenix Fanart initialized");
	}

	/**
	 * this is meant to return a subset of metadata properties that is useful
	 * for determining fanart locations
	 * 
	 * @param mediaType
	 * @param mediaObject
	 * @return
	 */
	private Map<String, String> getMetadata(MediaType mediaType,
			Object mediaObject) {
		if (mediaType == MediaType.TV) {
			Map<String, String> props = new HashMap<String, String>();
			for (String key : new String[] { FanartUtil.SEASON_NUMBER,
					FanartUtil.EPISODE_NUMBER }) {
				String v = SageFanartUtil
						.GetMediaFileMetadata(mediaObject, key);
				if (!SageFanartUtil.isEmpty(v)) {
					props.put(key, v);
				}
			}
			return props;
		}
		return null;
	}

	public String GetFanartCentralFolder() {
		return fanartConfig.getFanartCentralFolder();
	}

	public boolean IsFanartEnabled() {
		return fanartConfig.isFanartEnabled();
	}

	public void SetFanartCentralFolder(String folder) {
		log.debug("Setting Central Fanart Folder: " + folder);
		fanartConfig.setCentralFanartFolder(folder);
		initializeFanartFolder(folder);
	}

	public void SetIsFanartEnabled(boolean value) {
		fanartConfig.setFanartEnabled(value);
	}

	private boolean exists(File f) {
		return f != null && f.exists();
	}

	private boolean exists(File f[]) {
		return f != null && f.length > 0;
	}

	private File getDefaultArtifact(Object mediaObject,
			MediaArtifactType artifactType) {
		String def = null;
		if (artifactType == MediaArtifactType.POSTER) {
			def = MediaFileAPI.GetMediaFileMetadata(mediaObject,
					ISageCustomMetadataRW.FieldName.DEFAULT_POSTER);
		} else if (artifactType == MediaArtifactType.BACKGROUND) {
			def = MediaFileAPI.GetMediaFileMetadata(mediaObject,
					ISageCustomMetadataRW.FieldName.DEFAULT_BACKGROUND);
		} else if (artifactType == MediaArtifactType.BANNER) {
			def = MediaFileAPI.GetMediaFileMetadata(mediaObject,
					ISageCustomMetadataRW.FieldName.DEFAULT_BANNER);
		}

		if (!StringUtils.isEmpty(def)) {
			File f = new File(GetFanartCentralFolder(), def);
			if (f.exists() && f.isFile())
				return f;
		}

		return null;
	}

	public File GetFanartArtifact(Object mediaObject, MediaType mediaType,
			String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, Map<String, String> metadata) {
		File file = null;
		SimpleMediaFile mf = SageFanartUtil.GetSimpleMediaFile(mediaObject);
		mediaType = Utils.returnNonNull(mediaType, mf.getMediaType());
		mediaTitle = Utils.returnNonNull(mediaTitle, mf.getTitle());
		metadata = Utils.returnNonNull(metadata,
				getMetadata(mediaType, mediaObject));
		String fanartFolder = GetFanartCentralFolder();

		if (!fanartConfig.getUseSeason()) {
			// if we are setup to not use season specific fanart, null out metadata
			metadata = null;
		}
		
		// check for a default file
		file = getDefaultArtifact(mediaObject, artifactType);

		if (file == null) {
			file = GetFanartArtifactForTitle(mediaObject, mediaType,
					mediaTitle, artifactType, artifactTitle, metadata,
					fanartFolder);
		}

		if (!exists(file) && mf.getMediaType() == MediaType.MUSIC) {
			file = GetFanartArtifactForTitle(mediaObject, MediaType.MUSIC,
					SageFanartUtil.GetAlbumArtist(mediaObject), artifactType,
					artifactTitle, metadata, GetFanartCentralFolder());

			if (!exists(file)) {
				file = GetFanartArtifactForTitle(mediaObject, MediaType.MUSIC,
						SageFanartUtil.GetAlbumPersonArtist(mediaObject),
						artifactType, artifactTitle, metadata,
						GetFanartCentralFolder());
			}
		}

		// fallback to local fanart
//		if (!file.exists()) {
//			file = localFanart.GetFanartArtifact(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
//		}

		return file;
	}

	public File GetFanartArtifactDir(Object mediaObject, MediaType mediaType,
			String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, Map<String, String> metadata, boolean create) {
		SimpleMediaFile mf = SageFanartUtil.GetSimpleMediaFile(mediaObject);
		mediaType = Utils.returnNonNull(mediaType, mf.getMediaType());
		mediaTitle = Utils.returnNonNull(mediaTitle, mf.getTitle());
		metadata = Utils.returnNonNull(metadata,
				getMetadata(mediaType, mediaObject));
		String fanartFolder = GetFanartCentralFolder();
		File f = FanartUtil.getCentralFanartDir(mediaType, mediaTitle,
				artifactType, artifactTitle, fanartFolder, metadata);

		if (create && f!=null && !f.exists()) {
			sagex.phoenix.util.FileUtils.mkdirsQuietly(f);
			if (!f.exists()) {
				log.warn("Unable to create directory: " + f.getAbsolutePath()
						+ "; Pemission issue?");
			}
		}

		return f;
	}

	public File[] GetFanartArtifacts(Object mediaObject, MediaType mediaType,
			String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, Map<String, String> metadata) {
		File files[] = null;
		SimpleMediaFile mf = SageFanartUtil.GetSimpleMediaFile(mediaObject);
		mediaType = Utils.returnNonNull(mediaType, mf.getMediaType());
		mediaTitle = Utils.returnNonNull(mediaTitle, mf.getTitle());
		metadata = Utils.returnNonNull(metadata,
				getMetadata(mediaType, mediaObject));
		String fanartFolder = GetFanartCentralFolder();

		if (!fanartConfig.getUseSeason()) {
			// if we are setup to not use season specific fanart, null out metadata
			metadata = null;
		}
		
		files = GetFanartArtifactsForTitle(mediaObject, mediaType, mediaTitle,
				artifactType, artifactTitle, metadata, fanartFolder);

		if (!exists(files) && mf.getMediaType() == MediaType.MUSIC) {
			files = GetFanartArtifactsForTitle(mediaObject, mediaType,
					SageFanartUtil.GetAlbumArtist(mediaObject), artifactType,
					artifactTitle, metadata, fanartFolder);
			if (!exists(files)) {
				files = GetFanartArtifactsForTitle(mediaObject, mediaType,
						SageFanartUtil.GetAlbumPersonArtist(mediaObject),
						artifactType, artifactTitle, metadata, fanartFolder);
			}
		}
		
		// if no central fanart, then check for local fanart
//		if (!exists(files)) {
//			File f = localFanart.GetFanartArtifact(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
//			if (f.exists()) {
//				files = new File[] {f};
//			}
//		}
		
		return files;
	}

	public void SetFanartArtifact(Object mediaObject, File fanart,
			MediaType mediaType, String mediaTitle,
			MediaArtifactType artifactType, String artifactTitle,
			Map<String, String> metadata) {
		try {
			String central = (new File(GetFanartCentralFolder()))
					.getCanonicalPath();
			String file = fanart.getCanonicalPath();

			if (!file.startsWith(central)) {
				throw new Exception(
						"You can only set a fanart artifact relative to the fanart folder. Folder: "
								+ central + "; fanart: " + file);
			}

			String art = file.substring(central.length());
			if (art.startsWith(File.separator)) {
				art = StringUtils.strip(art, File.separator);
			}

			String key = null;
			if (artifactType == MediaArtifactType.POSTER) {
				key = ISageCustomMetadataRW.FieldName.DEFAULT_POSTER;
			} else if (artifactType == MediaArtifactType.BACKGROUND) {
				key = ISageCustomMetadataRW.FieldName.DEFAULT_BACKGROUND;
			} else if (artifactType == MediaArtifactType.BANNER) {
				key = ISageCustomMetadataRW.FieldName.DEFAULT_BANNER;
			}
			if (key == null)
				throw new Exception("Invalid Artifact Type: " + artifactType
						+ "; Can't set default artifact.");
			MediaFileAPI.SetMediaFileMetadata(mediaObject, key, art);
		} catch (Exception e) {
			log.warn("Failed to set the default fanart artifact!", e);
		}
	}

	private static final String getSeason(Map<String, String> metadata) {
		if (metadata == null) {
			return null;
		}
		return metadata.get(FanartUtil.SEASON_NUMBER);
	}

	public File GetFanartArtifactForTitle(Object mediaObject,
			MediaType mediaType, String mediaTitle,
			MediaArtifactType artifactType, String artifactTitle,
			Map<String, String> metadata, String centralFolder) {
		File art = null;

		art = FanartUtil.getCentralFanartArtifact(mediaType, mediaTitle,
				artifactType, artifactTitle, centralFolder, metadata);
		if (art == null || !art.exists()) {
			if (mediaType == MediaType.TV && metadata != null
					&& metadata.get(FanartUtil.SEASON_NUMBER) != null) {
				// do a search without the season metadata
				art = FanartUtil.getCentralFanartArtifact(mediaType,
						mediaTitle, artifactType, artifactTitle, centralFolder,
						null);
			}
		}

		// if no matches, then find the first one
		if (art == null || !art.exists()) {
			File all[] = GetFanartArtifactsForTitle(mediaObject, mediaType,
					mediaTitle, artifactType, artifactTitle, metadata,
					centralFolder);
			if (!SageFanartUtil.isEmpty(all)) {
				art = all[0];
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("GetFanartArtifactForTitle: MediaType: " + mediaType
					+ "; MediaTitle: " + mediaTitle + "; ArtifactType: "
					+ artifactType + "; ArtifactTitle: " + artifactTitle
					+ "; Artifact: " + art + "; Season: " + getSeason(metadata)
					+ "; MediaFile: " + mediaObject);
		}
		return art;
	}

	public File[] GetFanartArtifactsForTitle(Object mediaObject,
			MediaType mediaType, String mediaTitle,
			MediaArtifactType artifactType, String artifactTitle,
			Map<String, String> metadata, String centralFolder) {
		File files[] = null;
		files = FanartUtil.getCentalFanartArtifacts(mediaType, mediaTitle,
				artifactType, artifactTitle, centralFolder, metadata);
		if (files == null || files.length == 0) {
			if (mediaType == MediaType.TV && metadata != null
					&& metadata.get(FanartUtil.SEASON_NUMBER) != null) {
				// do a search without the season metadata
				files = FanartUtil.getCentalFanartArtifacts(mediaType,
						mediaTitle, artifactType, artifactTitle, centralFolder,
						null);
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("GetFanartArtifactsForTitle: MediaType: " + mediaType
					+ "; MediaTitle: " + mediaTitle + "; ArtifactType: "
					+ artifactType + "; ArtifactTitle: " + artifactTitle
					+ "; Artifact Count: " + (files == null ? 0 : files.length)
					+ "; MediaFile: " + mediaObject);
		}
		return files;
	}
}
