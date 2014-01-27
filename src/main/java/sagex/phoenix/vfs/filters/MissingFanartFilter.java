package sagex.phoenix.vfs.filters;

import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.vfs.IMediaResource;

public class MissingFanartFilter implements IResourceFilter {
    private MediaArtifactType fanart = null;
    
    public MissingFanartFilter(MediaArtifactType fanart) {
        this.fanart=fanart;
    }
    
    public boolean accept(IMediaResource resource) {
        Object file = phoenix.api.GetSageMediaFile(resource);
        if (file != null) {
            if (fanart == MediaArtifactType.POSTER) {
                return !phoenix.api.HasFanartPoster(file);
            }
            if (fanart == MediaArtifactType.BACKGROUND) {
                return !phoenix.api.HasFanartBackground(file);
            }
            if (fanart == MediaArtifactType.BANNER) {
                return !phoenix.api.HasFanartBanner(file);
            }
        }
        return false;
    }

}
