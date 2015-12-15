package sagex.phoenix.vfs.filters;

import java.io.File;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.PathUtils;

/**
 * filters a file path using a regular expression
 *
 * @author seans
 */
public class FilePathFilter extends BaseRegexFilter {
    public FilePathFilter() {
        super("Path Regex");
    }

    public boolean canAccept(IMediaResource res) {
        if (res instanceof IMediaFile) {
            File file = PathUtils.getFirstFile((IMediaFile) res);
            if (file == null)
                return false;
            return match(file.getAbsolutePath());
        }
        return false;
    }

    @Override
    public boolean match(String in) {
        if (in == null)
            return false;
        if (useRegex)
            return super.match(in);
        return in.startsWith(getValue());
    }

}
