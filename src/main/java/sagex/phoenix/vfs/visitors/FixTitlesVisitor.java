package sagex.phoenix.vfs.visitors;

import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * Fixes Titles on Video Files, either Adding or Removing the relative path.
 *
 * @author sean
 */
public class FixTitlesVisitor extends FileVisitor {
    private boolean useRelativePathInTitle = false;

    public FixTitlesVisitor(Boolean useRelativePathInTitle) {
        this.useRelativePathInTitle = useRelativePathInTitle;
    }

    @Override
    public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
        // skip recordings
        if (res.isType(MediaResourceType.RECORDING.value()))
            return true;

        if (useRelativePathInTitle) {
            res.getMetadata().setRelativePathWithTitle(MetadataUtil.getRelativePathWithTitle(res, res.getMetadata()));
        } else {
            res.getMetadata().setRelativePathWithTitle(res.getMetadata().getMediaTitle());
        }

        incrementAffected();
        return true;
    }
}
