package sagex.phoenix.vfs.visitors;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;

public class ArchiveVisitor extends FileVisitor {
    private boolean archivedState = true;

    public ArchiveVisitor(Boolean archivedState) {
        this.archivedState = archivedState;
    }

    @Override
    public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
        res.setLibraryFile(archivedState);
        incrementAffected();
        return true;
    }
}
