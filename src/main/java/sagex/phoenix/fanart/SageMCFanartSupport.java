package sagex.phoenix.fanart;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sagex.api.Configuration;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.Utils;

@Deprecated
public class SageMCFanartSupport implements IFanartSupport {
	private static final Logger log = Logger.getLogger(SageMCFanartSupport.class);

	private Map<MediaType, String> folders = new HashMap<MediaType, String>();
	private LocalFanartSupport localSupport = new LocalFanartSupport();

	/**
	 * SageMC only has support for Background
	 */
	public File GetFanartArtifactForTitle(Object mediaObject, MediaType mediaType, String mediaTitle,
			MediaArtifactType artifactType, String artifactTitle, String centralFolder) {
		// first check for local support
		File f = localSupport.GetFanartArtifact(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, null);

		// if there is no local file, then use the central folder
		if (f == null || !f.exists()) {
			// now use central fanart support for backgrounds only....
			if (artifactType == MediaArtifactType.BACKGROUND) {
				File dir = new File(centralFolder, folders.get(mediaType));
				f = new File(dir, FanartUtil.createSafeTitle(mediaTitle) + ".jpg");
			}
		}
		return f;
	}

	/**
	 * Returns true if sagemc/media_background/enabled property is enabled
	 */
	public boolean IsFanartEnabled() {
		return SageFanartUtil.GetBooleanProperty("sagemc/media_background/enabled", "false");
	}

	/**
	 * Sets the sagemc/media_background/enabled to true or false
	 */
	public void SetIsFanartEnabled(boolean v) {
		Configuration.SetProperty("sagemc/media_background/enabled", String.valueOf(v));
	}

	/**
	 * Sets the SageMC Property: sagemc/media_background/central_folder
	 */
	public void SetFanartCentralFolder(String folder) {
		Configuration.SetProperty("sagemc/media_background/central_folder", folder);
	}

	/**
	 * Returns the value set in sagemc/media_background/central_folder or the
	 * SageMC default folder ROOTPATH/MediaBackgrounds/ if it's not set.
	 */
	public String GetFanartCentralFolder() {
		Object o = Configuration.GetProperty("sagemc/media_background/central_folder", null);
		if (o == null) {
			String sageMCHome = System.getProperty("user.dir") + File.separator + "STVs" + File.separator + "SageTV3"
					+ File.separator + "SageMCE";
			String path = sageMCHome + File.separator + "MediaBackgrounds";
			return path;
		} else {
			return String.valueOf(o);
		}
	}

	public File[] GetFanartArtifacts(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, Map<String, String> metadata) {
		SimpleMediaFile mf = SageFanartUtil.GetSimpleMediaFile(mediaObject);
		File fa = GetFanartArtifactForTitle(mediaObject, (mediaType == null) ? mf.getMediaType() : mediaType,
				(mediaTitle == null) ? mf.getTitle() : mediaTitle, artifactType, artifactTitle, GetFanartCentralFolder());
		if (fa != null) {
			return new File[] { fa };
		}
		return null;
	}

	public File GetFanartArtifact(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, @SuppressWarnings("unused") Map<String, String> metadata) {
		if (mediaType == MediaType.ACTOR || mediaType == MediaType.GENRE || artifactType == MediaArtifactType.ACTOR)
			return null;

		SimpleMediaFile mf = SageFanartUtil.GetSimpleMediaFile(mediaObject);
		mediaType = Utils.returnNonNull(mediaType, mf.getMediaType());
		mediaTitle = Utils.returnNonNull(mediaTitle, mf.getTitle());
		String fanartFolder = GetFanartCentralFolder();

		File file = GetFanartArtifactForTitle(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, fanartFolder);

		if ((file == null || !file.exists()) && mf.getMediaType() == MediaType.MUSIC) {
			file = GetFanartArtifactForTitle(mediaObject, MediaType.MUSIC, SageFanartUtil.GetAlbumArtist(mediaObject),
					artifactType, artifactTitle, fanartFolder);

			if (!file.exists()) {
				file = GetFanartArtifactForTitle(mediaObject, MediaType.MUSIC, SageFanartUtil.GetAlbumPersonArtist(mediaObject),
						artifactType, artifactTitle, fanartFolder);
			}
		}
		return file;
	}

	public File GetFanartArtifactDir(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
			String artifactTitle, Map<String, String> metadata, boolean create) {
		// not implemented
		return null;
	}

	public void SetFanartArtifact(Object mediaObject, File fanart, MediaType mediaType, String mediaTitle,
			MediaArtifactType artifactType, String artifactTitle, Map<String, String> metadata) {
		// not implemented
	}
}
