package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

/**
 * Sorts based on the Start Date/Time, but when comparing a folder to an item we
 * will figure out what the most recent activity in the folder was do the
 * comparison
 *
 * @author bialio
 */
public class RecentActivitySorter implements Comparator<IMediaResource>, Serializable {
    private static final long serialVersionUID = 1L;

    public RecentActivitySorter() {
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        long t1 = getMostRecentTimeStamp(o1);
        long t2 = getMostRecentTimeStamp(o2);

        if (t1 > t2)
            return 1;
        if (t1 < t2)
            return -1;

        return 0;
    }

    private long getMostRecentTimeStamp(IMediaResource o) {
        if (o instanceof IMediaFile) {
            return ((IMediaFile) o).getStartTime();
        }

        if (o instanceof IMediaFolder) {
            return searchFolderForRecentTimeStamp((IMediaFolder) o);
        }

        // If it's not a File or Folder just return a 0
        return 0;
    }

    private long searchFolderForRecentTimeStamp(final IMediaFolder folder) {

        List<IMediaResource> children = folder.getChildren();
        long returnValue = 0;

        for (IMediaResource r : children) {

            long candidate = getMostRecentTimeStamp(r);
            if (candidate > returnValue) {
                // this item is more recent than the previously saved item
                returnValue = candidate;
            }
        }
        return returnValue;
    }
}