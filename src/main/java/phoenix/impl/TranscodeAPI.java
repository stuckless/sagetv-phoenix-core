package phoenix.impl;

import sagex.phoenix.tools.annotation.API;

/**
 * TODO: Accept Transcoding requests, and then add an entry that links the transcoded file
 * with the original file, such that, later when we know the real file, we can ask for a list
 * of Transcoded Files based on the original.  ie, we might have the original, a 3g version, etc.
 * @author sls
 *
 */
@API(group = "transcode")
public class TranscodeAPI {

	public TranscodeAPI() {
	}

}
