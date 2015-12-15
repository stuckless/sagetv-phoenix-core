package sagex.phoenix.vfs.sorters;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Sorts based on the Original Start Date/Time
 *
 * @author bialio
 */
public class OriginalAirDateSorter implements Comparator<IMediaResource>, Serializable {
    private static final long serialVersionUID = 1L;

    public OriginalAirDateSorter() {
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        if (o1 instanceof IMediaFile && o2 instanceof IMediaFile) {
            IMediaFile m1 = ((IMediaFile) o1);
            IMediaFile m2 = ((IMediaFile) o2);
            if (m1.getMetadata() == null || m1.getMetadata().getOriginalAirDate() == null)
                return 1;
            if (m2.getMetadata() == null || m2.getMetadata().getOriginalAirDate() == null)
                return -1;
            long t1 = m1.getMetadata().getOriginalAirDate().getTime();
            long t2 = m2.getMetadata().getOriginalAirDate().getTime();
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
