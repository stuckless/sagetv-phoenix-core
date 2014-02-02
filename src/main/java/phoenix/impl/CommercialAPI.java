package phoenix.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.tools.annotation.API;

/**
 * API for reading EDL Files for commercial skip. An EDL file has the following
 * Structure...
 * 
 * <pre>
 * 83.78    299.27  0
 * 832.90  1018.22 0
 * 1646.11 1806.20 0
 * 2347.91 2583.15 0
 * 3052.78 3208.00 0
 * 3786.92 3899.46 0
 * </pre>
 * 
 * @author seans
 * 
 */
@API(group = "comskip")
public class CommercialAPI {
	private Logger log = Logger.getLogger(CommercialAPI.class);

	/**
	 * For the given medai file, return the .edl file
	 * 
	 * @param mediaFile
	 *            Sage MediaFile or VFS MediaFile
	 * @return {@link File} representing the .edl file. It may not exist.
	 */
	public File GetCommercialFile(Object mediaFile) {
		File seg = phoenix.media.GetFileSystemMediaFile(mediaFile);
		if (seg == null)
			return null;
		return new File(seg.getParentFile(), FilenameUtils.getBaseName(seg.getName()) + ".edl");
	}

	/**
	 * Return true if an edl file file exists, AND it has some content
	 * 
	 * @param mediaFile
	 *            Sage MediaFile or VFS MediaFile
	 * @return true if the file exists and has content
	 */
	public boolean HasCommercials(Object mediaFile) {
		File f = GetCommercialFile(mediaFile);
		return f != null && f.exists() && f.length() > 0;
	}

	/**
	 * Return the Commercial start and stop points as an Array of
	 * {@link CommercialStruct} items
	 * 
	 * @param mediaFile
	 *            Sage MediaFile or VFS MediaFile
	 * @return Array of {@link CommercialStruct} items or null if there are no
	 *         commercials
	 */
	public CommercialStruct[] getCommercials(Object mediaFile) {
		List<CommercialStruct> comms = new ArrayList<CommercialStruct>();
		try {
			Pattern commPat = Pattern.compile("([0-9\\.]+)\\s+([0-9\\.]+)");
			LineIterator iter = FileUtils.lineIterator(GetCommercialFile(mediaFile));
			while (iter.hasNext()) {
				String line = iter.nextLine();
				Matcher m = commPat.matcher(line);
				if (m.find()) {
					CommercialStruct cs = new CommercialStruct();
					cs.start = NumberUtils.toFloat(m.group(1), 0f);
					cs.stop = NumberUtils.toFloat(m.group(2), 0f);
					comms.add(cs);
				}
			}
		} catch (Exception e) {
			log.warn("Failed to process commercials for: " + mediaFile, e);
		}

		if (comms.isEmpty())
			return null;
		return comms.toArray(new CommercialStruct[] {});
	}

	/**
	 * return the start value for the commercial
	 * 
	 * @param comm
	 *            {@link CommercialStruct}
	 * @return start value
	 */
	public float getCommercialStart(CommercialStruct comm) {
		if (comm == null)
			return 0f;
		return comm.start;
	}

	/**
	 * return the stop value for the commercial
	 * 
	 * @param comm
	 *            {@link CommercialStruct}
	 * @return stop value
	 */
	public float getCommercialStop(CommercialStruct comm) {
		if (comm == null)
			return 0f;
		return comm.stop;
	}
}
