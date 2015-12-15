package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.Comparator;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

/**
 * Sorts based on the Season # Episode #
 *
 * @author bialio
 */
public class SeasonEpisodeSorter implements Comparator<IMediaResource>, Serializable {
    private static final long serialVersionUID = 1L;

    public SeasonEpisodeSorter() {
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;

        // Get the relevant info
        if (o1 instanceof IMediaFile && o2 instanceof IMediaFile) {
            int s1 = ((IMediaFile) o1).getMetadata().getSeasonNumber();
            int e1 = ((IMediaFile) o1).getMetadata().getEpisodeNumber();
            int s2 = ((IMediaFile) o2).getMetadata().getSeasonNumber();
            int e2 = ((IMediaFile) o2).getMetadata().getEpisodeNumber();

            return (s1 == s2 ? e1 - e2 : s1 - s2);
        }

        // If they are not both IMediaFile return 0
        return 0;
    }
}