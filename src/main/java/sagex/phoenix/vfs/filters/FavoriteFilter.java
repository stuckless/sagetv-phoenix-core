package sagex.phoenix.vfs.filters;

import sagex.api.AiringAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class FavoriteFilter extends Filter {
	public FavoriteFilter() {
		super();
	}

	@Override
	public boolean canAccept(IMediaResource res) {

		if (res instanceof IMediaFile) {
			Object sageobject = res.getMediaObject();
			if (AiringAPI.IsFavorite(sageobject)) {
				return true;
			}
		}
		// if there is a folder in the view it will return false - this only
		// works
		// for flat views.
		return false;
	}
}
