package sagex.phoenix.vfs.impl;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

public class FileResourceFactory {
	private static final Logger log = Logger.getLogger(FileResourceFactory.class);

	// TODO: Maybe we can use this to enable BMT to use it's scraper files for
	// testing file types
	public interface FileTypeTester {
		public boolean isDVD(File f);

		public boolean isBluRay(File f);

		public boolean isVideoFile(File f);

		public boolean isRecording(File f);

		public boolean isImageFile(File f);

		public boolean isMusicFile(File f);
	}

	private static Pattern dvdPattern = Pattern.compile("video_ts|\\.vob$|\\.ifo$|\\.bup$", Pattern.CASE_INSENSITIVE);
	private static Pattern bluRayPattern = Pattern.compile("bdmv|\\.m2ts$", Pattern.CASE_INSENSITIVE);
	private static Pattern imgPattern = Pattern.compile("\\.bmp$|\\.jpg$|\\.gif$|\\.png$|\\.tif$", Pattern.CASE_INSENSITIVE);
	private static Pattern musicPattern = Pattern.compile("\\.mp3$|\\.ogg$|\\.wma$|\\.aac$|\\.flac$", Pattern.CASE_INSENSITIVE);
	private static Pattern videoPattern = Pattern.compile(
			"\\.avi$|\\.mpg$|\\.divx$|\\.mkv$|\\.wmv$|\\.mov$|\\.xvid$|\\.ts$|\\.m2ts$|\\.m4v$|\\.mp4$|\\.iso$",
			Pattern.CASE_INSENSITIVE);
	private static Pattern tvPattern = Pattern.compile("(season\\s*[0-9]{1,2})|(s[0-9]{1,2}[\\ ex\\.])", Pattern.CASE_INSENSITIVE);
	private static Pattern recordingPattern = Pattern.compile(
			"(([^-]+)-([^-]+)-([0-9]+)-([0-9]{1,2}))|(([^-]+)-([0-9]+)-([0-9]{1,2}))\\.", Pattern.CASE_INSENSITIVE);

	/**
	 * createResource() is used as a factory for Filesystem {@link File} objects
	 * to ensure that a Filesystem {@link File}, such as a DVD or bluray disc
	 * gets created as the correct type of {@link IMediaResource}.
	 * 
	 * {@link FileMediaFile} or {@link FileMediaFolder} should now be created
	 * directly but rather they should only be created when using this factory.
	 * 
	 * @param
	 * @return
	 */
	public static IMediaResource createResource(IMediaFolder parent, File f) {
		if (f == null)
			return null;

		if (isDVD(f)) {
			return new FileMediaFile(parent, resolveDVD(f));
		} else if (isBluRay(f)) {
			return new FileMediaFile(parent, resolveBluRay(f));
		} else if (f.isDirectory()) {
			return new FileMediaFolder(parent, f);
		} else {
			return new FileMediaFile(parent, f);
		}
	}

	/**
	 * Creates resource without a parent
	 */
	public static IMediaResource createResource(File f) {
		return createResource(null, f);
	}

	/**
	 * Convenience method to create a Folder. If the file is not a directory,
	 * then a directory of the parent is created.
	 */
	public static IMediaFolder createFolder(IMediaFolder parent, File f) {
		if (f == null)
			return null;

		IMediaResource r = createResource(parent, f);
		if (r instanceof IMediaFolder) {
			return (IMediaFolder) r;
		} else {
			return r.getParent();
		}
	}

	/**
	 * Creates a folder without a known parent
	 */
	public static IMediaFolder createFolder(File f) {
		return createFolder(null, f);
	}

	/**
	 * resolveBluRay() attempt to resolve a file as a BluRay folder. It will
	 * return the path of the BDMV folder or a parent folder that contains a
	 * .m2ts file.
	 * 
	 * @param f
	 * @return
	 */
	public static File resolveBluRay(File f) {
		if (f.isDirectory()) {
			if ("BDMV".equalsIgnoreCase(f.getName())) {
				return f;
			}

			File ts = new File(f, "BDMV");
			if (ts.exists()) {
				return ts;
			}

			return f;
		} else {
			if (f.getParentFile() != null) {
				return f.getParentFile();
			} else {
				return f;
			}
		}
	}

	/**
	 * resolveDVD() attempts to resolve a DVD file by inspecting the file to see
	 * if it a dvd file. If it is a dvd file, then it will return it as a
	 * directory of either the VIDEO_TS dir or a dir that contains a .vob file.
	 * 
	 * @param f
	 * @return
	 */
	public static File resolveDVD(File f) {
		if (f.isDirectory()) {
			if ("VIDEO_TS".equalsIgnoreCase(f.getName())) {
				return f;
			}

			File ts = new File(f, "VIDEO_TS");
			if (ts.exists()) {
				return ts;
			}

			return f;
		} else {
			if (f.getParentFile() != null) {
				return f.getParentFile();
			} else {
				return f;
			}
		}
	}

	public static boolean isDVD(File f) {
		Matcher m = dvdPattern.matcher(f.getAbsolutePath());
		if (m.find())
			return true;

		if (f.isDirectory()) {
			// check if we have anychildren that match vob pattern or video_ts
			File children[] = f.listFiles();
			for (File ch : children) {
				Matcher match = dvdPattern.matcher(ch.getName());
				if (match.find())
					return true;
			}
		}

		return false;
	}

	public static boolean isBluRay(File f) {
		Matcher m = bluRayPattern.matcher(f.getAbsolutePath());
		if (m.find())
			return true;

		if (f.isDirectory()) {
			// check if we have anychildren that match vob pattern or video_ts
			File children[] = f.listFiles();
			for (File ch : children) {
				Matcher match = bluRayPattern.matcher(ch.getName());
				if (match.find())
					return true;
			}
		}

		return false;
	}

	public static boolean isImageFile(File f) {
		return filenameMatches(f, imgPattern);
	}

	public static boolean isMusicFile(File f) {
		return filenameMatches(f, musicPattern);
	}

	public static boolean isVideoFile(File f) {
		return filenameMatches(f, videoPattern);
	}

	public static boolean isRecordingFile(File f) {
		return filenameMatches(f, recordingPattern);
	}

	public static boolean isTvFile(File f) {
		Matcher m = tvPattern.matcher(f.getAbsolutePath());
		return m.find();
	}

	public static boolean filenameMatches(File f, Pattern p) {
		Matcher m = p.matcher(f.getName());
		return m.find();
	}

	/**
	 * Given a file that may be a reg file, or dvd, or bluray, return the
	 * meaninful filename. ie, if VIDEO_TS is passed in then the parent filename
	 * is returned, since VIDEO_TS is not meaninful.
	 * 
	 * @param f
	 */
	public static String getRealTitle(File f) {
		if (f == null)
			return null;

		// adjust the title for VIDEO_TS folders
		if ("VIDEO_TS".equalsIgnoreCase(f.getName()) || "BDMV".equalsIgnoreCase(f.getName())) {
			return returnParentIfNotNull(f).getName();
		} else {
			return f.getName();
		}
	}

	private static File returnParentIfNotNull(File f) {
		if (f == null)
			return null;
		if (f.getParentFile() != null) {
			return f.getParentFile();
		}
		return f;
	}
}
