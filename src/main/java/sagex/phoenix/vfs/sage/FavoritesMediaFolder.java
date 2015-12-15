package sagex.phoenix.vfs.sage;

import java.util.Collections;
import java.util.List;

import sagex.api.FavoriteAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.sorters.StartTimeSorter;

/**
 * Returns the currently schedules favorites as a Folder
 *
 * @author sean
 */
public class FavoritesMediaFolder extends VirtualMediaFolder {
    private boolean singleLevel = false;

    public FavoritesMediaFolder() {
        this(false);
    }

    public FavoritesMediaFolder(boolean singleLevel) {
        super("Favorites");
        this.singleLevel = singleLevel;
    }

    @Override
    protected void populateChildren(List<IMediaResource> children) {
        Object favs[] = FavoriteAPI.GetFavorites();
        if (favs != null) {
            for (Object f : favs) {
                if (singleLevel) {
                    Object singles[] = FavoriteAPI.GetFavoriteAirings(f);
                    if (singles != null) {
                        long curTime = System.currentTimeMillis();
                        for (Object s : singles) {
                            IMediaFile mf = new SageMediaFile(this, s);
                            if (mf.getStartTime() > curTime) {
                                children.add(mf);
                            }
                        }
                    }
                } else {
                    children.add(new MediaFilesMediaFolder(this, FavoriteAPI.GetFavoriteAirings(f), FavoriteAPI.GetFavoriteTitle(f)));
                }
            }
        }

        if (singleLevel) {
            Collections.sort(children, new StartTimeSorter());
        }
    }
}
