package sagex.phoenix.fanart;

import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.persistence.PersistenceUtil;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

import java.io.File;
import java.util.List;

/**
 * Writes a simple poster image for the sage system.
 *
 * @author seans
 */
public class LocalFanartStorage {
    public void saveFanart(IMediaFile mf, IMetadata md) {
        List<IMediaArt> posters = FanartUtil.getMediaArt(md, MediaArtifactType.POSTER, 0);
        if (posters.size() == 0) {
            return;
        }

        File f = PathUtils.getFirstFile(mf);
        if (f == null || !f.exists()) {
            return;
        }

        IMediaArt mediaArt = posters.get(0);

        File imageFile = FanartUtil.getLocalFanartForFile(f, MediaType.MOVIE, MediaArtifactType.POSTER, false);
        PersistenceUtil.writeImageFromUrl(mediaArt.getDownloadUrl(), imageFile);
    }
}
