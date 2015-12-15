package sagex.phoenix.fanart;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.Utils;

/**
 * Local Fanart Support implements the FanartAPI but only looks for Fanart, in
 * the Local Directory. ie, backdrops, banners, etc reside in the same directory
 * as the local file.
 *
 * @author seans
 */
public class LocalFanartSupport implements IFanartSupport {
    private Logger log = Logger.getLogger(LocalFanartSupport.class);

    public LocalFanartSupport() {
    }

    private File GetFanartArtifactForTitle(Object mediaObject, MediaType mediaType, String mediaTitle,
                                           MediaArtifactType artifactType, String artifactTitle) {
        return FanartUtil.getLocalFanartForFile(SageFanartUtil.GetFile(mediaObject), mediaType, artifactType, true);
    }

    public String GetFanartCentralFolder() {
        return null;
    }

    public boolean IsFanartEnabled() {
        return true;
    }

    public void SetFanartCentralFolder(String folder) {
        // not required
    }

    public void SetIsFanartEnabled(boolean value) {
        // not required
    }

    public File GetFanartArtifact(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                  String artifactTitle, @SuppressWarnings("unused") Map<String, String> metadata) {
        if (artifactType == MediaArtifactType.ACTOR || mediaType == MediaType.GENRE || mediaType == MediaType.ACTOR)
            return null;

        SimpleMediaFile mf = SageFanartUtil.GetSimpleMediaFile(mediaObject);
        mediaType = Utils.returnNonNull(mediaType, mf.getMediaType());
        mediaTitle = Utils.returnNonNull(mediaTitle, mf.getTitle());

        File file = GetFanartArtifactForTitle(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle);

        if ((file == null || !file.exists()) && mf.getMediaType() == MediaType.MUSIC) {
            file = GetFanartArtifactForTitle(mediaObject, MediaType.MUSIC, SageFanartUtil.GetAlbumArtist(mediaObject),
                    artifactType, artifactTitle);

            if (!file.exists()) {
                file = GetFanartArtifactForTitle(mediaObject, MediaType.MUSIC, SageFanartUtil.GetAlbumPersonArtist(mediaObject),
                        artifactType, artifactTitle);
            }
        }

        return file;
    }

    public File[] GetFanartArtifacts(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                     String artifactTitle, Map<String, String> metadata) {
        File fa = GetFanartArtifact(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
        if (fa != null && fa.exists()) {
            return new File[]{fa};
        }
        return null;
    }

    public File GetFanartArtifactDir(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                     String artifactTitle, Map<String, String> metadata, boolean create) {
        return null;
    }

    public void SetFanartArtifact(Object mediaObject, File fanart, MediaType mediaType, String mediaTitle,
                                  MediaArtifactType artifactType, String artifactTitle, Map<String, String> metadata) {

    }
}
