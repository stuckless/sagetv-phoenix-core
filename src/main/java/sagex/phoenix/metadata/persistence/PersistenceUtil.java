package sagex.phoenix.metadata.persistence;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.download.DownloadItem;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.util.FileUtils;

public class PersistenceUtil {
	private static Logger log = Logger.getLogger(PersistenceUtil.class);

	public static void writeImageFromUrl(String url, File out) {
		try {
			if (url == null) {
				log.error("WriteImageFromUrl called with null url");
				return;
			}

			if (out == null) {
				log.error("writeImageFromUrl called with null output file for image: " + url);
				return;
			}

			log.info("Writing Image; Url: " + url + "; ToFile: " + out.getAbsolutePath());

			// make the directory, if it doesn't exist.
			if (out.getParentFile() != null && !out.getParentFile().exists()) {
				log.info("Creating new image dir " + out.getParentFile());
				if (!out.getParentFile().mkdirs()) {
					log.warn("Failed to create parent image folder (probably a permission issue): "
							+ out.getParentFile().getAbsolutePath());
					return;
				}
			}

			URL u = new URL(url);
			if (PersistenceUtil.isSameFile(u, out)) {
				log.warn("Skipping: " + url + "; since it's the same as the output file.");
				return;
			}

			DownloadItem di = new DownloadItem(new URL(url), out);
			Phoenix.getInstance().getDownloadManager().downloadAndWait(di);
		} catch (SocketTimeoutException timeout) {
			log.warn("Timedout while writing image: " + url + "; to file: " + out.getAbsolutePath(), timeout);
		} catch (Throwable t) {
			log.warn("Failed to write image: " + url + "; to file: " + out.getAbsolutePath(), t);
		} finally {
			if (out.exists() && out.length() == 0) {
				log.info("Removing 0 byte file: " + out.getAbsolutePath() + "; for url: " + url);
				FileUtils.deleteQuietly(out);
			}
		}
	}

	public static void writeImageFromUrl(String url, File out, int scaleWidth) {
		try {
			if (url == null) {
				log.error("WriteImageFromUrl called with null url");
				return;
			}

			if (out == null) {
				log.error("writeImageFromUrl called with null output file for image: " + url);
			}

			log.info("Writing Image; Url: " + url + "; ToFile: " + out.getAbsolutePath() + "; WithScale: " + scaleWidth);

			// make the directory, if it doesn't exist.
			if (out.getParentFile() != null && !out.getParentFile().exists()) {
				if (!out.getParentFile().mkdirs()) {
					log.warn("Failed to create parent image folder (probably a permission issue): "
							+ out.getParentFile().getAbsolutePath());
					return;
				}
			}

			URL u = new URL(url);
			if (PersistenceUtil.isSameFile(u, out)) {
				log.warn("Skipping: " + url + "; since it's the same as the output file.");
				return;
			}

			BufferedImage imageSrc = ImageUtil.readImage(u);
			if (imageSrc == null) {
				log.warn("writeImageFromUrl(): Failed to download image: " + url + "; ToFile: " + out.getAbsolutePath());
				return;
			}

			ImageUtil.writeImage(imageSrc, out, scaleWidth, -1);
		} catch (SocketTimeoutException timeout) {
			log.warn("Timedout while writing image: " + url + "; to file: " + out.getAbsolutePath(), timeout);
		} catch (Throwable t) {
			log.warn("Failed to write image: " + url + "; to file: " + out.getAbsolutePath() + "; with scale: " + scaleWidth, t);
		} finally {
			if (out.exists() && out.length() == 0) {
				log.info("Removing 0 byte file: " + out.getAbsolutePath() + "; for url: " + url);
				FileUtils.deleteQuietly(out);
			}
		}
	}

	public static boolean isSameFile(URL u, File out) {
		try {
			if (u == null || out == null)
				return false;
			return out.equals(new File(u.toURI()));
		} catch (Throwable e) {
			return false;
		}
	}

}
