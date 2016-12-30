package sagex.phoenix.vfs.visitors;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * Created by seans on 27/12/16.
 */
public class MissingFanartVisitor extends FileVisitor {
    public MissingFanartVisitor() {
    }

    @Override
    public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
        if (res.isType(MediaResourceType.TV.value()) || res.isType(MediaResourceType.VIDEO.value())) {

        }
        return false;
    }
}
