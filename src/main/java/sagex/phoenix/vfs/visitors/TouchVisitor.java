package sagex.phoenix.vfs.visitors;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

public class TouchVisitor implements IMediaResourceVisitor {
    private long time = 0;

    public TouchVisitor(long time) {
        this.time = time;
    }

    @Override
    public boolean visit(IMediaResource res, IProgressMonitor mon) {
        res.touch(time);
        return true;
    }
}
