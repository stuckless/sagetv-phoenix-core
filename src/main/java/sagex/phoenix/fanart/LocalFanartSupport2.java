package sagex.phoenix.fanart;

import org.apache.log4j.Logger;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Local Fanart Support implements the FanartAPI but only looks for Fanart, in
 * the Local Directory. ie, backdrops, banners, etc reside in the same directory
 * as the local file.
 *
 * @author seans
 */
public class LocalFanartSupport2 implements IFanartSupport2 {
    private Logger log = Logger.getLogger(LocalFanartSupport2.class);

    public LocalFanartSupport2() {
    }

    @Override
    public File GetFanartArtifactsDir(IMediaFile mf, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                      String artifactTitle, Map<String, String> metadata) {
        if (artifactType == MediaArtifactType.ACTOR || mediaType == MediaType.GENRE || mediaType == MediaType.ACTOR)
            return null;
        File file = FanartUtil.getLocalFanartForFile(PathUtils.getFirstFile(mf), mediaType, artifactType, true);
        if (file != null) {
            file = file.getParentFile();
        }
        return file;
    }

    public List<File> GetFanartArtifacts(IMediaFile mf, MediaType mediaType, String mediaTitle, MediaArtifactType artifactType,
                                         String artifactTitle, Map<String, String> metadata) {
        List<File> files = new ArrayList<File>();

        if (artifactType == MediaArtifactType.ACTOR || mediaType == MediaType.GENRE || mediaType == MediaType.ACTOR)
            return files;

        File localFile = PathUtils.getFirstFile(mf);
        File file = FanartUtil.getLocalFanartForFile(localFile, mediaType, artifactType, true);
        if (file != null && file.exists()) {
            files.add(file);
        }

        return files;
    }

    @Override
    public boolean IsFanartEnabled() {
        return true;
    }

    @Override
    public void SetIsFanartEnabled(boolean value) {
        // do nothing
    }

    @Override
    public String getDisplayInformation() {
        return "Local Fanart";
    }
}
