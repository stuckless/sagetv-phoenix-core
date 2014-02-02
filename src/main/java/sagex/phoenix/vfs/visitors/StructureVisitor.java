package sagex.phoenix.vfs.visitors;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

/**
 * Visitor that allows you to visit the specific structure of the resources for
 * the purposes for knowing when you are visiting a file, folder, before file,
 * after file, before folder, and after folder.
 * 
 * This visitor will not honor the DEEP flag, since it will do it's own
 * traversing. It does this, so that it can ensure the correct order of visiting
 * the resources.
 * 
 * Sub-classes should override the various event methods that they care about.
 * 
 * @author seans
 * 
 */
public class StructureVisitor implements IMediaResourceVisitor {
	public StructureVisitor() {
	}

	@Override
	public boolean visit(IMediaResource res, IProgressMonitor monitor) {
		if (res instanceof IMediaFile) {
			beforeFile((IMediaFile) res, monitor);
			file((IMediaFile) res, monitor);
			afterFile((IMediaFile) res, monitor);
		} else if (res instanceof IMediaFolder) {
			beforeFolder((IMediaFolder) res, monitor);
			folder((IMediaFolder) res, monitor);
			children((IMediaFolder) res, monitor);
			afterFolder((IMediaFolder) res, monitor);
		}

		// we going to do our own traversing... so always return false
		return false;
	}

	public void beforeFile(IMediaFile res, IProgressMonitor monitor) {
	}

	public void file(IMediaFile res, IProgressMonitor monitor) {
	}

	public void afterFile(IMediaFile res, IProgressMonitor monitor) {
	}

	public void beforeFolder(IMediaFolder res, IProgressMonitor monitor) {
	}

	public void folder(IMediaFolder res, IProgressMonitor monitor) {
	}

	public void afterFolder(IMediaFolder res, IProgressMonitor monitor) {
	}

	private void children(IMediaFolder res, IProgressMonitor monitor) {
		for (IMediaResource child : res.getChildren()) {
			visit(child, monitor);
			if (monitor != null && monitor.isCancelled())
				break;
		}
	}
}
