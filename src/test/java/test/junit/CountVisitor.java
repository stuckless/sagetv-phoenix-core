package test.junit;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

import java.util.ArrayList;
import java.util.List;

public class CountVisitor implements IMediaResourceVisitor {
    public int folders = 0;
    public int files = 0;
    public List<IMediaResource> folderList = new ArrayList<IMediaResource>();
    public List<IMediaResource> fileList = new ArrayList<IMediaResource>();

    public CountVisitor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean visit(IMediaResource res, IProgressMonitor mon) {
        if (res instanceof IMediaFolder) {
            folders++;
            folderList.add(res);
        } else {
            files++;
            fileList.add(res);
        }
        return true;
    }

    public void reset() {
        folders = 0;
        files = 0;
        folderList.clear();
        fileList.clear();
    }
}
