package sagex.phoenix.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Simple File ext filter. Only returns true if the file ext matches an ext that
 * this instance requires.
 * 
 * @author seans
 * 
 */
public class FileExtFileFilter implements FileFilter {
	private String[] exts;
	private FileFilter dirFilter = null;

	public FileExtFileFilter(String exts[], FileFilter dirFilter) {
		this.exts = exts;
		this.dirFilter = dirFilter;
	}

	public FileExtFileFilter(String exts) {
		this(exts.split(","), null);
	}

	public FileExtFileFilter(String exts, FileFilter dirFilter) {
		this(exts.split(","), dirFilter);
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			if (dirFilter != null)
				return dirFilter.accept(f);
			return true;
		}
		if (exts == null)
			return true;
		String name = f.getName();
		int p = name.lastIndexOf('.');
		if (p == -1)
			return false;
		String ext = name.substring(p);
		for (String e : exts) {
			if (e.equalsIgnoreCase(ext)) {
				return true;
			}
		}
		return false;
	}

}
