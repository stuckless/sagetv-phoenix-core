package sagex.phoenix.tools.support;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class Tools {

	public Tools() {
	}

	public static int removeMetadataProperties(File[] dirs) {
		if (dirs == null || dirs.length == 0)
			return 0;

		int removed = 0;
		for (File dir : dirs) {
			if (dir.exists() && dir.isDirectory()) {
				Iterator<File> iter = FileUtils.iterateFiles(dir, new String[] { "properties" }, true);
				for (; iter.hasNext();) {
					File f = iter.next();
					sagex.phoenix.util.FileUtils.deleteQuietly(f);
					removed++;
				}
			}
		}

		return removed;
	}

	/**
	 * removes jar files based on a regex pattern.
	 * 
	 * @param libDir
	 * @param pattern
	 * @return list of jar files that CANNOT be removed
	 */
	public static List<File> cleanJars(File libDir, String pattern) {
		List<File> notRemoved = new ArrayList<File>();
		Pattern p = Pattern.compile(pattern);
		if (libDir.exists()) {
			for (File f : libDir.listFiles()) {
				Matcher m = p.matcher(f.getName());
				if (m.find()) {
					System.out.println("Removing Jar: " + f.getName());
					if (!f.delete()) {
						notRemoved.add(f);
					}
				}
			}
		}
		return notRemoved;
	}

	/**
	 * Renames duplicate jars and returns a list of jars that cannot be renamed.
	 * 
	 * @param jarDir
	 * @return
	 */
	public static List<File> renameConflictedJars(File jarDir) {
		List<File> notRemoved = new ArrayList<File>();
		List<JarInfo> jars = JarUtil.findDuplicateJars(jarDir);
		for (JarInfo ji : jars) {
			File f = ji.getFile();
			// System.out.printf("Renaming %s: %s (%s)\n", f, ji.getTitle(),
			// ji.getVersion());
			if (!ji.getFile().renameTo(new File(f.getParentFile(), f.getName() + String.valueOf(System.currentTimeMillis())))) {
				// System.out.println("ERROR: Failed to rename jar file: " + f);
				notRemoved.add(f);
			}
		}

		return notRemoved;
	}
}
