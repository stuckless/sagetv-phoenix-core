package sagex.phoenix.vfs.filters;

import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.vfs.IMediaResource;

public class MissingFanartFilter implements IResourceFilter {
    public MissingFanartFilter() {
    }

    public boolean accept(IMediaResource resource) {
        return phoenix.fanart.IsMissingFanart(resource);
    }
}
