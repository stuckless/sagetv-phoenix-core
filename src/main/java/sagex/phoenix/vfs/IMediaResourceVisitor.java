package sagex.phoenix.vfs;

import sagex.phoenix.progress.IProgressMonitor;

/**
 * Visitor for visiting Media Resources.
 * 
 * @author seans
 *
 */
public interface IMediaResourceVisitor {
    /**
     * A visit(IMediaResource) will be called for item withing a collection of items.
     * 
     * If a visitor has child items, it can return "true" to have those items visited as well.  It is OK to return
     * true for files and folders, although returning true or false for Files will have no effect.
     * 
     * The monitor can be used to update progress, or cancel the visiting operation.  The monitor will never be null.
     * 
     * @param res resource being visited
     * @param monitor progress monitor
     * @return return true to have the child items processed as well, or false to not traverse child items of this member
     */
    public boolean visit(IMediaResource res, IProgressMonitor monitor);
}
