package phoenix.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.api.AiringAPI;
import sagex.api.MediaFileAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.vfs.IMediaFile;

/**
 * API For managing Video Thumbnails
 * 
 * @author sean
 */
@API(group = "videothumbs")
public class VideoThumbAPI {
	private static final Pattern detPattern = Pattern.compile("S([0-9]+)_([0-9]+)_([0-9]+)_([0-9]+).jpg");
	private static Logger log = Logger.getLogger(VideoThumbAPI.class);

	/**
	 * Returns the video thumbnail dir for a given media file
	 * 
	 * @param file
	 * @return
	 */
	public File GetThumbnailDir(Object file) {
		File dir = new File(Phoenix.getInstance().getUserCacheDir(), "videothumb/" + phoenix.media.GetMediaFile(file).getId());
		if (!dir.exists()) {
			sagex.phoenix.util.FileUtils.mkdirsQuietly(dir);
		}
		return dir;
	}

	/**
	 * Generates a mediafile video thumbnail.
	 * 
	 * @param inFile
	 *            Sage MediaFile or {@link IMediaFile}
	 * @param outFile
	 *            destination filepath and name
	 * @param seconds
	 *            seconds offset
	 * @param w
	 *            width
	 * @param h
	 *            height
	 * @throws IOException
	 *             if the image was not created
	 */
	public void GenerateThumbnailInDir(Object inFile, File outFile, float seconds, int w, int h) {
		MediaFileAPI.GenerateThumbnail(phoenix.media.GetSageMediaFile(inFile), seconds, w, h, outFile);
		if (!outFile.exists()) {
			log.warn("Failed to create thumnail for file " + inFile);
			return;
		}
	}

	/**
	 * Generates a mediafile thumbnail to the default video thumbnail dir for
	 * the file. The file name is like "Sseconds_WIDTH_HEIGHT.jpg, ie,
	 * S60000.0_320_200.jpg
	 * 
	 * @param inFile
	 * @param seconds
	 * @param w
	 * @param h
	 * @throws IOException
	 */
	public void GenerateThumbnail(Object inFile, float seconds, int w, int h) {
		try {
			// String strSeconds = String.valueOf((long)seconds);
			// issue: 148
			String strSeconds = String.valueOf(seconds).replace(".", "_");
			File f = new File(GetThumbnailDir(inFile), "S" + strSeconds + "_" + w + "_" + h + ".jpg").getCanonicalFile();
			GenerateThumbnailInDir(inFile, f, seconds, w, h);
		} catch (Exception e) {
			log.warn("Failed to generate thumbnails for " + inFile, e);
		}
	}

	/**
	 * Generates n number of thumbs throughout a given mediafile, where each
	 * thumb is evenly spaced throughout the file.
	 * 
	 * All files will be removed from the outdir before the batch is created. If
	 * outdir is null, then outdir will default to the default thumbnail dir
	 * 
	 * @param file
	 * @param thumbs
	 *            total number of thumbs that will be created
	 * @param w
	 * @param h
	 */
	public File[] GenerateThumbnailsEvenly(Object file, int thumbs, int w, int h) {
		Object sageFile = phoenix.media.GetSageMediaFile(file);

		File dir = GetThumbnailDir(file);
		try {
			FileUtils.cleanDirectory(dir);
		} catch (IOException e) {
			log.warn("Failed to clean directory " + dir);
		}

		long dur = AiringAPI.GetAiringDuration(sageFile);
		if (dur == 0) {
			log.warn("Cannot generated thumbnails; File has no duration: " + file);
			return null;
		}
		long dursec = dur / 1000;
		int secs = (int) (dursec / thumbs);
		try {
			for (int i = 0; i < thumbs; i++) {
				GenerateThumbnail(file, i * secs, w, h);
			}
		} catch (Exception e) {
			log.warn("Failed to create images for " + file, e);
		}

		return GetThumbnails(dir);
	}

	/**
	 * Returns the Video Thumbnails for the given mediafile
	 * 
	 * @param file
	 * @return
	 */
	public File[] GetThumbnails(Object file) {
		return GetThumbnails(GetThumbnailDir(file));
	}

	/**
	 * Returns the Video Thumbnails for the given dir
	 * 
	 * @param dir
	 * @return
	 */
	public File[] GetThumbnails(File dir) {
		if (dir == null || !dir.exists())
			return null;

		final Set<File> files = new TreeSet<File>(new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				long t1 = GetThumbnailSeconds(o1);
				long t2 = GetThumbnailSeconds(o2);
				if (t1 == t2)
					return 0;
				if (t1 < t2)
					return -1;
				if (t1 > t2)
					return +1;
				return 0;
			}
		});

		dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".jpg")) {
					String details[] = GetThumbnailDetails(pathname);
					if (details != null) {
						files.add(pathname);
					} else {
						// delete the file since it's invalid
						sagex.phoenix.util.FileUtils.deleteQuietly(pathname);
					}
				}
				return false;
			}
		});

		return (File[]) files.toArray(new File[] {});
	}

	/**
	 * Retuns an array of image details. The first element is the seconds,
	 * followed by the width, and then the height.
	 * 
	 * A null array is returned if the info cannot be determined.
	 * 
	 * @param f
	 * @return
	 */
	public String[] GetThumbnailDetails(File f) {
		Matcher m = detPattern.matcher(f.getName());
		if (m.matches()) {
			return new String[] { m.group(1), m.group(2), m.group(3) };
		}
		return null;
	}

	/**
	 * Generates thumbnails every n seconds.
	 * 
	 * @param file
	 * @param seconds
	 * @param w
	 * @param h
	 * @return
	 */
	public File[] GenerateThumbnailsEvery(Object file, int seconds, int w, int h) {
		Object sageFile = phoenix.media.GetSageMediaFile(file);

		File dir = GetThumbnailDir(file);
		try {
			FileUtils.cleanDirectory(dir);
		} catch (IOException e) {
			log.warn("Failed to clean directory " + dir);
		}

		long dur = AiringAPI.GetAiringDuration(sageFile);
		if (dur == 0) {
			log.warn("Cannot generated thumbnails; File has no duration: " + file);
			return null;
		}

		long seclong = seconds * 1000;
		try {
			long cursec = 0;
			while (cursec < dur) {
				GenerateThumbnail(file, (cursec / 1000f), w, h);
				cursec += seclong;
			}
		} catch (Exception e) {
			log.warn("Failed to create images for " + file, e);
		}

		return GetThumbnails(dir);
	}

	/**
	 * Removes the generated video thumbnails for the given mediafile.
	 * 
	 * @param file
	 */
	public void ClearThumbnails(Object file) {
		File dir = GetThumbnailDir(file);
		try {
			FileUtils.cleanDirectory(dir);
		} catch (IOException e) {
			log.warn("Failed to clean directory " + dir);
		}
	}

	/**
	 * Get the Seconds offset for this thumbnail
	 * 
	 * @param file
	 * @return
	 */
	public long GetThumbnailSeconds(File file) {
		String det[] = GetThumbnailDetails(file);
		if (det != null) {
			return NumberUtils.toLong(det[0]);
		}
		return 0;
	}

}
