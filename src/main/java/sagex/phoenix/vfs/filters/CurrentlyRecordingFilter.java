package sagex.phoenix.vfs.filters;

import sagex.api.MediaFileAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class CurrentlyRecordingFilter extends Filter {
	public CurrentlyRecordingFilter() {
		super();
	}

	@Override
	public boolean canAccept(IMediaResource res) {

		if (res instanceof IMediaFile) {
			return MediaFileAPI.IsFileCurrentlyRecording(((IMediaFile) res).getMediaObject());
		}
		return false;
	}
}
