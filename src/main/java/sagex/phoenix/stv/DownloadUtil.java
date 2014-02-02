package sagex.phoenix.stv;

public class DownloadUtil {
	/**
	 * returns true if the download status is complete
	 * 
	 * @param status
	 * @return
	 */
	public static boolean isDownloadComplete(Object status) {
		return ((status instanceof Boolean) && (Boolean) status);
	}

	/**
	 * Return true if the download status was an error
	 * 
	 * @param status
	 * @return
	 */
	public static boolean isDownloadError(Object status) {
		return status instanceof String && ((String) status).startsWith("Error");
	}

	/**
	 * Return true if the download status is not complete and it's not an error
	 * (ie, it's downloading)
	 * 
	 * @param status
	 * @return
	 */
	public static boolean isDownloading(Object status) {
		return !isDownloadComplete(status) && !isDownloadError(status);
	}

}
