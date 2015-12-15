package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import sagex.api.AiringAPI;
import sagex.api.Database;
import sagex.api.FavoriteAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

/**
 * Sorts based on favorite priority non-favorites are unsorted at the bottom of
 * this sort
 *
 * @author bialio
 */
public class FavoritePrioritySorter implements Comparator<IMediaResource>, Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Object> sortedFavorites;
    StartTimeSorter secondarySort = new StartTimeSorter();

    public FavoritePrioritySorter() {
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        int p1 = getFavoritePriority(o1);
        int p2 = getFavoritePriority(o2);

        if (p1 == p2) {
            // they are the same favorite, sort by start time
            // return the opposite of the actual result to get the
            // list in the right order
            return (secondarySort.compare(o1, o2)) * -1;
        }
        return p1 - p2;
    }

    private int getFavoritePriority(IMediaResource o) {

        if (sortedFavorites == null) {
            Object favs[] = (Object[]) Database.Sort(FavoriteAPI.GetFavorites(), true, "FavoritePriority");
            sortedFavorites = new ArrayList<Object>(Arrays.asList(favs));
        }

        if (sortedFavorites.isEmpty()) {
            return -1;
        }

        if (o instanceof IMediaFile) {
            Object theairing = o.getMediaObject();
            if (AiringAPI.IsFavorite(theairing)) {
                Object thefav = FavoriteAPI.GetFavoriteForAiring(theairing);
                return sortedFavorites.indexOf(thefav);
            }
            // If it's not a favorite, give it the lowest priority
            return -1;
        }

        if (o instanceof IMediaFolder) {
            // look at the first child
            // this assumes that all members of the folder are the same
            // 'Favorite'
            IMediaResource res = ((IMediaFolder) o).getChildren().get(0);
            return this.getFavoritePriority(res);
        }

        // If it's not a File or Folder just return a -1
        return -1;
    }
}