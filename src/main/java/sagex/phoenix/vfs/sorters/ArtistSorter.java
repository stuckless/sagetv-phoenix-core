package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.Comparator;

import sagex.phoenix.vfs.IMediaResource;

public class ArtistSorter implements Comparator<IMediaResource>, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(IMediaResource res1, IMediaResource res2) {
		String art1 = phoenix.music.GetArtist(res1);
		String art2 = phoenix.music.GetArtist(res2);
		if (art1 == null || art2 == null)
			return -1;
		return art1.compareTo(art2);
	}
}
