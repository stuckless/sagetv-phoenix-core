package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.Comparator;

import sagex.phoenix.vfs.IMediaResource;

public class TrackSorter implements Comparator<IMediaResource>, Serializable {
	private static final long serialVersionUID = 1L;

	public int compare(IMediaResource res1, IMediaResource res2) {
		int track1 = phoenix.metadata.GetTrack(res1);
		int track2 = phoenix.metadata.GetTrack(res2);
		return track1 - track2;
	}
}
