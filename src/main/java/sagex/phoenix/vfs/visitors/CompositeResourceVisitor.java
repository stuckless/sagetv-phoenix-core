package sagex.phoenix.vfs.visitors;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

/**
 * Composes a visitor from 2 other visitors.  It will only call the second visitor, if the first was
 * accepted.
 * 
 * @author seans
 */
public class CompositeResourceVisitor implements IMediaResourceVisitor {
    private IMediaResourceVisitor r1;
    private IMediaResourceVisitor r2;

    public CompositeResourceVisitor(IMediaResourceVisitor r1, IMediaResourceVisitor r2) {
        this.r1=r1;
        this.r2=r2;
    }

    public boolean visit(IMediaResource res, IProgressMonitor mon) {
        if (r1.visit(res, mon)) {
            return r2.visit(res, mon);
        } else {
            return false;
        }
    }
}
