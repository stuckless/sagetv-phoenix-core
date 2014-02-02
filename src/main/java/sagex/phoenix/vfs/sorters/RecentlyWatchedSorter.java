package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import sagex.api.AiringAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

/**
 * Sorts based on the value of "GetWatchedStartTime()", but when comparing a
 * folder to an item we will figure out what the most recently watched date in
 * the folder was do the comparison
 * 
 * @author bialio
 * 
 */
public class RecentlyWatchedSorter implements Comparator<IMediaResource>, Serializable {
	private static final long serialVersionUID = 1L;

	public RecentlyWatchedSorter() {
	}

	public int compare(IMediaResource o1, IMediaResource o2) {
		if (o1 == null)
			return 1;
		if (o2 == null)
			return -1;

		long t1 = getLastWatchedTimeStamp(o1);
		long t2 = getLastWatchedTimeStamp(o2);

		if (t1 > t2)
			return 1;
		if (t1 < t2)
			return -1;

		return 0;
	}

	private long getLastWatchedTimeStamp(IMediaResource o) {
		if (o instanceof IMediaFile) {
			Object theairing = o.getMediaObject();
			return AiringAPI.GetRealWatchedStartTime(theairing);
		}

		if (o instanceof IMediaFolder) {
			return searchFolderForLastWatchedTimeStamp((IMediaFolder) o);
		}

		// If it's not a File or Folder just return a 0
		return 0;
	}

	private long searchFolderForLastWatchedTimeStamp(final IMediaFolder folder) {

		List<IMediaResource> children = folder.getChildren();
		long returnValue = 0;

		for (IMediaResource r : children) {

			long candidate = getLastWatchedTimeStamp(r);
			if (candidate > returnValue) {
				// this item is more recent than the previously saved item
				returnValue = candidate;
			}
		}
		return returnValue;
	}
}