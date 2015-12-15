package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaResource;

public class AlbumGrouper implements IGrouper {
    @Override
    public String getGroupName(IMediaResource res) {
        IAlbumInfo info = phoenix.media.GetAlbum(res);
        if (info != null) {
            return info.getName();
        }
        return null;
    }
}
