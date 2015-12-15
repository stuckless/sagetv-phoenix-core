package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaResource;

public class ArtistGrouper implements IGrouper {
    @Override
    public String getGroupName(IMediaResource res) {
        return phoenix.music.GetArtist(res);
    }
}
