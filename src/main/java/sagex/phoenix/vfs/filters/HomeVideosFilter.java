package sagex.phoenix.vfs.filters;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.PathUtils;

/**
 * Returns true if the file is a considered to be a Home Video. ie the file's
 * path is in a configured Home Video folder.
 *
 * @author sean
 */
public class HomeVideosFilter extends Filter {
    HomeVideosConfiguration cfg = null;

    public HomeVideosFilter() {
        super();
        cfg = GroupProxy.get(HomeVideosConfiguration.class);
    }

    public boolean canAccept(IMediaResource res) {
        String dirspec = cfg.getFolders();
        if (StringUtils.isEmpty(dirspec))
            return false;
        if (res instanceof IMediaFile) {
            File f = PathUtils.getFirstFile((IMediaFile) res);
            if (f == null)
                return false;
            String path = f.getAbsolutePath();
            path = path.toLowerCase();
            dirspec = dirspec.toLowerCase();
            String dirs[] = dirspec.split("\\s*;\\s*");
            for (String d : dirs) {
                if (path.startsWith(d)) {
                    return true;
                }
            }
            return false;
        } else {
            // folder
            return true;
        }
    }

    private static HomeVideosFilter filter = null;

    public static boolean isHomeVideo(IMediaResource res) {
        if (filter == null)
            filter = new HomeVideosFilter();
        return filter.accept(res);
    }
}
