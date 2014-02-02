package sagex.phoenix.vfs;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.Hints;

/**
 * Folder Implementation that "decorates" an underlying folder.
 * 
 * @author seans
 * 
 */
public class DecoratedMediaFolder implements IMediaFolder {
	protected Logger log = Logger.getLogger(this.getClass());

	// original folder being "viewed"
	protected IMediaFolder originalFolder = null;

	// tells the view to reapply it's view settings, since the children have
	// changed
	protected boolean changed = true;

	// current list of decorated children
	protected List<IMediaResource> decoratedChildren;

	public String getId() {
		return originalFolder.getId();
	}

	public Object getMediaObject() {
		return originalFolder.getMediaObject();
	}

	public boolean isDontLike() {
		return originalFolder.isDontLike();
	}

	public boolean isFavorite() {
		return originalFolder.isFavorite();
	}

	public boolean isLibraryFile() {
		return originalFolder.isLibraryFile();
	}

	public boolean isWatched() {
		return originalFolder.isWatched();
	}

	public void setDontLike(boolean like) {
		originalFolder.setDontLike(like);
	}

	public void setManualRecord(boolean manual) {
		originalFolder.setManualRecord(manual);
	}

	public void setWatched(boolean watched) {
		originalFolder.setWatched(watched);
	}

	public void accept(IMediaResourceVisitor visitor, IProgressMonitor monitor, int deep) {
		boolean visitChildren = visitor.visit(this, monitor);
		if (visitChildren && deep >= 0) {
			for (IMediaResource r : getChildren()) {
				r.accept(visitor, monitor, deep - 1);
				if (monitor != null && monitor.isCancelled())
					break;
			}
		}
	}

	public DecoratedMediaFolder(IMediaFolder decorate) {
		this.originalFolder = decorate;
	}

	public int compareTo(IMediaResource o) {
		if (o instanceof DecoratedMediaFolder) {
			return originalFolder.compareTo(((DecoratedMediaFolder) o).getUndecoratedFolder());
		} else {
			return -1;
		}
	}

	public List<IMediaResource> getChildren() {
		if (changed) {
			changed = false;
			if (decoratedChildren != null) {
				releaseChildren(decoratedChildren);
			}
			decoratedChildren = decorate(originalFolder.getChildren());
		}

		return getDecoratedChildren();
	}

	protected void releaseChildren(List<IMediaResource> oldChildren) {
	}

	protected List<IMediaResource> decorate(List<IMediaResource> originalChildren) {
		return originalChildren;
	}

	protected void addMediaResource(IMediaResource res) {
		// NPE bug when called before the children has been decorated
		// getDecoratedChildren().add(res);
		originalFolder.getChildren().add(res);
	}

	protected List<IMediaResource> getDecoratedChildren() {
		return decoratedChildren;
	}

	public IMediaFolder getParent() {
		return originalFolder.getParent();
	}

	public Object getThumbnail() {
		return originalFolder.getThumbnail();
	}

	public String getTitle() {
		return originalFolder.getTitle();
	}

	public IMediaFolder getUndecoratedFolder() {
		return originalFolder;
	}

	public Iterator<IMediaResource> iterator() {
		return getChildren().iterator();
	}

	public void setChanged() {
		this.changed = true;
	}

	public boolean isType(int type) {
		return originalFolder.isType(type);
	}

	public IMediaResource getChild(String name) {
		if (name == null)
			return null;

		for (IMediaResource res : getChildren()) {
			if (name.equals(res.getTitle()))
				return res;
		}

		return null;
	}

	public String toString() {
		return "DecoratedFolder[" + getTitle() + "]";
	}

	public boolean delete(Hints hints) {
		return originalFolder.delete(hints);
	}

	public boolean exists() {
		return originalFolder.exists();
	}

	public long lastModified() {
		return originalFolder.lastModified();
	}

	public void touch(long time) {
		originalFolder.touch(time);
	}

	@Override
	public void setLibraryFile(boolean library) {
		originalFolder.setLibraryFile(library);
	}

	@Override
	public boolean isManualRecord() {
		return originalFolder.isManualRecord();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((originalFolder == null) ? 0 : originalFolder.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DecoratedMediaFolder other = (DecoratedMediaFolder) obj;
		if (originalFolder == null) {
			if (other.originalFolder != null)
				return false;
		} else if (!originalFolder.equals(other.originalFolder))
			return false;
		return true;
	}

	@Override
	public IMediaResource findChild(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String child = StringUtils.substringBefore(path, "/");
		String other = StringUtils.substringAfter(path, "/");

		IMediaResource childRes = getChild(child);
		if (childRes == null) {
			// doesn't matter the path, we didn't find anything
			return null;
		}

		if (StringUtils.isEmpty(other)) {
			return childRes;
		}

		if (!(childRes instanceof IMediaFolder)) {
			// we found a child but there is still an extra path, but we are not
			// a folder
			return null;
		}

		return ((IMediaFolder) childRes).findChild(other);
	}

	public String getPath() {
		return (getParent() == null ? "" : getParent().getPath()) + "/" + getTitle();
	}

	@Override
	public IMediaResource getChildById(String id) {
		if (id == null)
			return null;

		for (IMediaResource res : getChildren()) {
			if (id.equals(res.getId()))
				return res;
		}

		return null;
	}

	@Override
	public boolean removeChild(IMediaResource child) {
		if (child instanceof DecoratedMediaFile) {
			originalFolder.removeChild(((DecoratedMediaFile) child).getDecoratedItem());
		} else {
			originalFolder.removeChild(child);
		}

		boolean decor = decoratedChildren.remove(child);

		if (decoratedChildren.size() == 0) {
			// we have no children, remove ourself from parent
			if (getParent() != null) {
				getParent().removeChild(this);
			}
		}
		return decor;
	}
}
