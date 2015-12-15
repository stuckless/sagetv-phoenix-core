package sagex.phoenix.vfs;

import sagex.phoenix.tools.annotation.API;

import java.util.List;

@API(group = "music", proxy = true, prefix = "Album", resolver = "phoenix.media.GetAlbum")
public interface IAlbumInfo {
    public Object getArt();

    public String getArtist();

    public String getGenre();

    public String getName();

    public List<IMediaFile> getTracks();

    public String getYear();

    public boolean hasArt();
}
