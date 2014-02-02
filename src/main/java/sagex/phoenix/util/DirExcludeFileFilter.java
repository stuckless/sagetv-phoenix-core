package sagex.phoenix.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Simple Name based exclude filter. Pass a comma separated list of dirs or
 * String array. For each dir that is passed, it will check for a name match. If
 * it matches, then the dir is excluded.
 * 
 * @author seans
 * 
 */
public class DirExcludeFileFilter implements FileFilter {
	public String[] excludes = null;

	public DirExcludeFileFilter(String dirs) {
		this(dirs.split(","));
	}

	public DirExcludeFileFilter(String[] dirs) {
		this.excludes = dirs;
	}

	public boolean accept(File f) {
		if (f.isFile())
			return false;
		if (excludes == null)
			return true;
		for (String d : excludes) {
			if (d.equals(f.getName())) {
				return false;
			}
		}
		return true;
	}

}
