/**
 *
 */
package test;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

public class SimpleSTDOUTVisitor implements IMediaResourceVisitor {
    public SimpleSTDOUTVisitor() {
    }

    public boolean visit(IMediaResource res, IProgressMonitor mon) {
        if (res instanceof IMediaFolder) {
            System.out.println("\nFolder: " + res.getTitle() + "; Parent: " + getParent(res));
        } else {
            System.out.println("  File: " + res.getTitle() + "; Parent: " + getParent(res));
        }
        return true;
    }

    private String getParent(IMediaResource res) {
        if (res == null)
            return "NULL";
        if (res.getParent() != null)
            return res.getParent().getTitle();
        return "ROOT";
    }
}