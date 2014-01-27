package sagex.phoenix.vfs.sage;

import java.util.ArrayList;
import java.util.List;

import sagex.api.AlbumAPI;
import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFile;

public class SageAlbumInfo implements IAlbumInfo {
    private Object album;
    public SageAlbumInfo(Object album) {
        this.album=album;
    }
    public Object getArt() {
        return AlbumAPI.GetAlbumArt(album);
    }
    public String getArtist() {
        return AlbumAPI.GetAlbumArtist(album);
    }
    public String getGenre() {
        return AlbumAPI.GetAlbumGenre(album);
    }
    public String getName() {
        return AlbumAPI.GetAlbumName(album);
    }
    
    public List<IMediaFile> getTracks() {
        List<IMediaFile> files = new ArrayList<IMediaFile>();
        Object tracks[] = AlbumAPI.GetAlbumTracks(album);
        if (tracks!=null) {
            for (Object o : tracks) {
                files.add(new SageMediaFile(null, o));
            }
        }
        return files;
    }
    
    public String getYear() {
        return AlbumAPI.GetAlbumYear(album);
    }
    public boolean hasArt() {
        return AlbumAPI.HasAlbumArt(album);
    }

}
