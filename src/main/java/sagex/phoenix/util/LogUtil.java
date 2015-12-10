package sagex.phoenix.util;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.sage.SageMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

public class LogUtil {
	public static void logMetadataUpdated(IMediaFile file) {
		try {
			IMetadata md = file.getMetadata();
			StringBuilder sb = new StringBuilder("UPDATE");
			sb.append("; ").append(PathUtils.getLocation(file));
			sb.append("; ").append(md.getMediaTitle());
			sb.append("; ").append(md.getMediaType());
			if (file.isType(MediaResourceType.TV.value())) {
				sb.append("; S").append(md.getSeasonNumber());
				if (md.getEpisodeNumber() > 0) {
					sb.append("; E").append(md.getEpisodeNumber());
				}
				if (md.getDiscNumber() > 0) {
					sb.append("; D").append(md.getDiscNumber());
				}
			}
			sb.append("; ").append(md.getExternalID());
			sb.append("; ").append(file.getId());

			Loggers.METADATA.info(sb.toString());
		} catch (Exception e) {
			Loggers.LOG.warn("Failed to write metadata log message", e);
		}
	}

	public static void logTVEpisodeGapReview(String logInfo) {
		Loggers.LOG.info("INFO; TVEpisodeGapReview; " + logInfo);
	}

	public static void logMetadataUpdatedError(IMediaFile file, Throwable e) {
		Loggers.METADATA.warn("ERROR; " + getFileString(file) + "; " + e.getMessage());

		// log the exception in the main log
		Loggers.LOG.warn("ERROR; " + getFileString(file) + "; " + e.getMessage(), e);
	}

	public static void logMetadataSkipped(IMediaFile file) {
		Loggers.METADATA.info("SKIP; " + getFileString(file));
	}

	public static void logAutoUpdate(String type, SageMediaFile file) {
		Loggers.METADATA.info("AUTO; " + type + "; " + getFileString(file));
	}

	private static String getFileString(IMediaFile file) {
		if (Loggers.METADATA.isDebugEnabled()) {
			return String.valueOf(file) + "; Class: " + file.getClass().getName() + "; >>> Sage Object["
					+ String.valueOf(file.getMediaObject()) + ", Class: " + file.getMediaObject().getClass().getName() + "]";
		} else {
			StringBuilder sb = new StringBuilder();
			String loc = PathUtils.getLocation(file);
			if ("/".equals(file.getTitle())) {
				loc = String.valueOf(file.getMediaObject());
			}
			sb.append(loc);
			sb.append("; ").append(file.getTitle());
			sb.append("; ").append(file.getId());
			return sb.toString();
		}
	}

	public static void logNewTVSeriesInfoAdded(ISeriesInfo info) {
		if (info != null) {
			Loggers.METADATA.info("ADDED: TV Series Info: " + info.getTitle());
		}
	}
}
