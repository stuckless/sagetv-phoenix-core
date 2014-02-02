package sagex.phoenix.vfs.visitors;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;

public class WatchedVisitor extends FileVisitor {
	private boolean watchedState = true;

	public WatchedVisitor(Boolean watchedState) {
		this.watchedState = watchedState;
	}

	@Override
	public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
		res.setWatched(watchedState);
		incrementAffected();
		return true;
	}
}
