package sagex.phoenix.vfs.sorters;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Sorts based on the Start Date/Time
 *
 * @author seans
 */
public class StartTimeSorter implements Comparator<IMediaResource>, Serializable {
    private static final long serialVersionUID = 1L;

    public StartTimeSorter() {
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        if (o1 instanceof IMediaFile && o2 instanceof IMediaFile) {
            long t1 = ((IMediaFile) o1).getStartTime();
            long t2 = ((IMediaFile) o2).getStartTime();
            ;
            if (t1 > t2)
                return 1;
            if (t1 < t2)
                return -1;
        }

        // we are probably comparing folders or items to folders, etc..
        if (o1.getTitle() != null) {
            return o1.getTitle().compareTo(o2.getTitle());
        } else {
            return 1;
        }
    }
}
