package sagex.phoenix.vfs.sources;

import java.util.Set;

import sagex.api.PlaylistAPI;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.sage.MediaFilesMediaFolder;

/**
 * Factory that creates a Playlist Folder for the current playlists
 *
 * @author seans
 */
public class PlaylistSourceFactory extends Factory<IMediaFolder> {
    public PlaylistSourceFactory() {
        super();
    }

    public IMediaFolder create(Set<ConfigurableOption> altOptions) {
        VirtualMediaFolder vmf = new VirtualMediaFolder("Playlists");
        Object plist[] = PlaylistAPI.GetPlaylists();
        if (plist != null) {
            for (Object p : plist) {
                MediaFilesMediaFolder f = new MediaFilesMediaFolder(vmf, PlaylistAPI.GetPlaylistItems(p), PlaylistAPI.GetName(p));
                vmf.addMediaResource(f);
            }
        }
        return vmf;
    }
}
