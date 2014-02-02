package sagex.phoenix.vfs.filters;

import java.io.File;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.PathUtils;

/**
 * Filters a filename by regular expression
 * 
 * @author seans
 */
public class FileNameFilter extends BaseRegexFilter {
	public FileNameFilter() {
		super("Extension Regex");
	}

	@Override
	protected boolean canAccept(IMediaResource res) {
		if (res instanceof IMediaFile) {
			File file = PathUtils.getFirstFile((IMediaFile) res);
			if (file == null)
				return false;
			return match(file.getName());
		}
		return false;
	}
}
