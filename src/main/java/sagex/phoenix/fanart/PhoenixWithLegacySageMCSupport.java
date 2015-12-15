package sagex.phoenix.fanart;

import java.io.File;
import java.util.Map;

import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;

/**
 * FanartSupport class that falls back on the legacy sagemc when fanart cannot
 * be located in the new system.
 *
 * @author seans
 */
@Deprecated
public class PhoenixWithLegacySageMCSupport implements IFanartSupport {
    private PhoenixFanartSupport phoenix = new PhoenixFanartSupport();
    private SageMCFanartSupport sagemc = new SageMCFanartSupport();

    public String GetFanartCentralFolder() {
        return phoenix.GetFanartCentralFolder();
    }

    public boolean IsFanartEnabled() {
        return phoenix.IsFanartEnabled();
    }

    public void SetFanartCentralFolder(String folder) {
        phoenix.SetFanartCentralFolder(folder);
    }

    public void SetIsFanartEnabled(boolean value) {
        phoenix.SetIsFanartEnabled(value);
    }

    public File[] GetFanartArtifacts(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                     String artifactTitle, Map<String, String> metadata) {
        File files[] = phoenix.GetFanartArtifacts(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
        if (files == null || files.length == 0) {
            files = sagemc.GetFanartArtifacts(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
        }
        return files;
    }

    public File GetFanartArtifact(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                  String artifactTitle, Map<String, String> metadata) {
        File file = phoenix.GetFanartArtifact(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
        if (file == null || !file.exists()) {
            file = sagemc.GetFanartArtifact(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
        }
        return file;
    }

    public File GetFanartArtifactDir(Object mediaObject, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                     String artifactTitle, Map<String, String> metadata, boolean create) {
        return phoenix.GetFanartArtifactDir(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata, create);
    }

    public void SetFanartArtifact(Object mediaObject, File fanart, MediaType mediaType, String mediaTitle,
                                  MediaArtifactType artifactType, String artifactTitle, Map<String, String> metadata) {
        phoenix.SetFanartArtifact(mediaObject, fanart, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
    }
}
