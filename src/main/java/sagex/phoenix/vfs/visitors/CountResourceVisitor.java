package sagex.phoenix.vfs.visitors;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

public class CountResourceVisitor implements IMediaResourceVisitor {
	private int count = 0;

	public boolean visit(IMediaResource resource, IProgressMonitor mon) {
		count++;
		return true;
	}

	public int getCount() {
		return count;
	}
}
