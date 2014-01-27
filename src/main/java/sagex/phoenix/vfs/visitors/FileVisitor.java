package sagex.phoenix.vfs.visitors;

import org.apache.log4j.Logger;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

/**
 * Convenience Base class for creating File visitors.  It tracks the number of files visisted and you
 * can track the number of files affected by calling incrementAffected from your visitFile method.  you
 * also don't needt test if it's a file, since that check will be done for you as well.
 * 
 * @author seans
 */
public abstract class FileVisitor implements IMediaResourceVisitor {
	protected Logger log = Logger.getLogger(this.getClass());
	
	private int visitedCount = 0;
	private int affectedCount = 0;
	
	public FileVisitor() {
	}

	@Override
	public boolean visit(IMediaResource res, IProgressMonitor monitor) {
		if (res instanceof IMediaFile) {
			try {
				return visitFile((IMediaFile) res, monitor);
			} finally {
				if (monitor !=null) monitor.worked(1);
			}
		}
		return true;
	}
	
	public abstract boolean visitFile(IMediaFile res, IProgressMonitor monitor);

	public int getVisitedCount() {
		return visitedCount;
	}

	public int getAffectedCount() {
		return affectedCount;
	}
	
	public int incrementAffected() {
		return ++affectedCount;
	}
}
