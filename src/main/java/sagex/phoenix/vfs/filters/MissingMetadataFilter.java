package sagex.phoenix.vfs.filters;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * Filter returns true if the {@link IMediaFile} is missing core metadata
 * fields. This only applies to TV and Video files, and for TV, it means
 * season/episode information
 *
 * @author seans
 */
public class MissingMetadataFilter implements IResourceFilter {
    public MissingMetadataFilter() {
    }

    public boolean accept(IMediaResource res) {
        try {
            // never attempt to work on excluded files
            if (Phoenix.getInstance().getMetadataManager().isExcluded(res)) {
                return false;
            }

            if (res.isType(MediaResourceType.TV.value())) {
                IMetadata md = ((IMediaFile) res).getMetadata();
                if (StringUtils.isEmpty(md.getMediaType()))
                    return true;
                if (StringUtils.isEmpty(md.getMediaTitle()))
                    return true;
                if (StringUtils.isEmpty(md.getEpisodeName()))
                    return true;
                if (md.getEpisodeNumber() <= 0)
                    return true;
            } else if (res.isType(MediaResourceType.ANY_VIDEO.value())) {
                IMetadata md = ((IMediaFile) res).getMetadata();
                if (StringUtils.isEmpty(md.getMediaType()))
                    return true;
                if (StringUtils.isEmpty(md.getMediaTitle()))
                    return true;
            } else if (res.isType(MediaResourceType.MUSIC.value())) {
                IMetadata md = ((IMediaFile) res).getMetadata();
                if (StringUtils.isEmpty(md.getMediaType()))
                    return true;
            }

            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
