package sagex.phoenix.remote.streaming;

public class MediaProcessFactory {
	public MediaProcessFactory() {
	}

	public MediaProcess newProcess(MediaStreamerManager manager, MediaRequest req) {
		if (req.isRequestingGenericStreamer()) {
			return new GenericCommandMediaProcess(manager, req);
		} else {
			return new VLCHLSMediaProcess(manager, req);
		}
	}
}
