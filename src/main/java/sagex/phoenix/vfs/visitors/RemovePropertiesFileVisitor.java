package sagex.phoenix.vfs.visitors;

import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

import java.io.File;

/**
 * Physically removed .properties file that as associated with each given media
 * file
 *
 * @author seans
 */
public class RemovePropertiesFileVisitor extends FileVisitor {
    public RemovePropertiesFileVisitor() {
    }

    @Override
    public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
        File file = FanartUtil.resolvePropertiesFile(PathUtils.getFirstFile(res));
        if (file != null && file.exists()) {
            if (file.delete()) {
                incrementAffected();
            }
        }
        return true;
    }
}
