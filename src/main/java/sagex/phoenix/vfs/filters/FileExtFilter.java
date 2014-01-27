package sagex.phoenix.vfs.filters;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.PathUtils;

/**
 * filters a file extension using a regular expression
 * 
 * @author seans
 *
 */
public class FileExtFilter extends BaseRegexFilter {
    public FileExtFilter() {
    	super("Extension Regex");
    }

    public boolean canAccept(IMediaResource res) {
        if (res instanceof IMediaFile) {
            File file = PathUtils.getFirstFile((IMediaFile) res);
            if (file==null) return false;
            return match(FilenameUtils.getExtension(file.getName()));
        }
        return false;
    }
}
