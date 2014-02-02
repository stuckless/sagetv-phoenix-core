package sagex.phoenix.vfs.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sagex.phoenix.util.url.UrlUtil;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

/**
 * PathUtils work with Resources as inputs, but it will use the underlying File
 * object for all operations.
 * 
 * @author seans
 * 
 */
public class PathUtils {
	public static String getExtension(IMediaResource res) {
		String name = getName(res);
		if (name == null)
			return null;
		int p = name.lastIndexOf('.');
		if (p != -1) {
			return name.substring(p + 1);
		} else {
			return null;
		}
	}

	/**
	 * Return the Name of the resource. If the resouce has a filesystem File
	 * object, then it will return the name of that File, otherwise it will
	 * return the Title of the given resource.
	 * 
	 * @param res
	 * @return
	 */
	public static String getName(IMediaResource res) {
		if (res instanceof IMediaFile) {
			File file = getFirstFile((IMediaFile) res);
			if (file != null) {
				String name = file.getName();
				if ("VIDEO_TS".equals(name) || "BDMV".equals(name)) {
					file = file.getParentFile();
					if (file != null) {
						name = file.getName();
					}
				}
				return name;
			} else {
				return res.getTitle();
			}
		} else {
			return res.getTitle();
		}
	}

	/**
	 * Returns the Path for the given resource. If the resouce has a fileystem
	 * File object, then the path of that File is returned, otherwise, it will
	 * build up a virtual path as a unix style path, of the given resource and
	 * its parent resources.
	 * 
	 * @param res
	 * @return
	 */
	public static String getLocation(IMediaResource res) {
		String loc = null;
		if (res instanceof IMediaFile) {
			File f = getFirstFile((IMediaFile) res);
			if (f != null) {
				loc = f.getAbsolutePath();
			}
		}

		if (loc == null) {
			List<String> paths = new ArrayList<String>();
			paths.add(res.getTitle());
			IMediaResource r = res;
			while ((r = r.getParent()) != null) {
				paths.add(0, r.getTitle());
			}

			StringBuffer sb = new StringBuffer();
			for (String s : paths) {
				sb.append("/");
				sb.append(s);
			}
			loc = sb.toString();
		}

		return loc;
	}

	public static File getFirstFile(IMediaFile res) {
		if (res == null)
			return null;
		List<File> files = res.getFiles();
		if (files.size() > 0)
			return files.get(0);
		return null;
	}

	public static String getBasename(IMediaResource res) {
		if (res instanceof IMediaFile) {
			String name = getName(res);
			if (name == null)
				return null;
			int p = name.lastIndexOf('.');
			if (p != -1) {
				return name.substring(0, p);
			} else {
				return name;
			}
		} else {
			return getName(res);
		}
	}

	public static String getPathAsUrl(IMediaResource res) {
		StringBuilder sb = new StringBuilder();
		do {
			sb.insert(0, "/" + UrlUtil.encode(res.getTitle()));
		} while ((res = res.getParent()) != null);
		return sb.toString();
	}
}
