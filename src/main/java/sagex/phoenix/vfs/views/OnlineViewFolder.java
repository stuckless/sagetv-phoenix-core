package sagex.phoenix.vfs.views;

import java.util.ArrayList;
import java.util.List;

import sagex.phoenix.Phoenix;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.DummyMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;

public class OnlineViewFolder extends ViewFolder {
	public static int FOLDER_TIMEOUT = 5000;
	private boolean loaded = false;

	public OnlineViewFolder(ViewFactory factory, int level, ViewFolder parent, IMediaFolder decorate) {
		super(factory, level, parent, decorate);
	}

	@Override
	public void accept(IMediaResourceVisitor visitor, IProgressMonitor monitor, int deep) {
		boolean visitChildren = visitor.visit(this, monitor);
		if (visitChildren && deep >= 0 && isLoaded(FOLDER_TIMEOUT)) {
			for (IMediaResource r : getChildren()) {
				// basically we will visit each of our children, but no deeper
				// for online videos, for any visitor, we DO NOT want to force
				// it walk the online tree
				r.accept(visitor, monitor, 0);
				if (monitor != null && monitor.isCancelled())
					break;
			}
		}
	}

	/**
	 * Count returns the known size of the items, and doesn't cause a force load
	 * of the items if they have not been loaded, like getChildren().size()
	 * does. For online videos, it is recommended that you never call
	 * getChildren() except when you really want to resolve the children, ie,
	 * fetch them.
	 * 
	 * @return
	 */
	public int count() {
		if (decoratedChildren != null) {
			return decoratedChildren.size();
		}
		return 0;
	}

	@Override
	public List<IMediaResource> getChildren() {
		if (changed) {
			changed = false;
			if (!loaded) {
				loadChilren();
			}
		}
		return decoratedChildren;
	}

	private void loadChilren() {
		// set a temporary list to ask the UI to wait
		decoratedChildren = new ArrayList<IMediaResource>();
		decoratedChildren.add(new ViewItem(this, new DummyMediaFile("Loading Items... Please wait and refresh later.")));

		// create a background task to load the items
		Runnable r = new Runnable() {
			@Override
			public void run() {
				decoratedChildren = decorate(originalFolder.getChildren());
				loaded = true;
				// set the changed flag, so that we can tell the view to show
				// these children
				setChanged();
			}
		};

		// schedule this task to run later
		Phoenix.getInstance().invokeLater(r);
	}

	/**
	 * Returns true is the folder has been loaded. Returns false if the folder
	 * could not be loaded within the waitFor timeout millisecods. For exmample,
	 * if you need to cause the folder items to load, and you need those items
	 * before you can continue then call isLoaded() and check for true. For
	 * online videos getChildren() will always return a list, even if the list
	 * has not been loaded, so this method blocks until the list is fully
	 * loaded.
	 * 
	 * @param waitFor
	 * @return
	 */
	public boolean isLoaded(long waitFor) {
		if (loaded)
			return true;
		// force children to load
		loadChilren();

		// wait until they are loaded
		long expired = System.currentTimeMillis() + waitFor;
		while (System.currentTimeMillis() < expired) {
			if (loaded)
				return true;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
		return false;
	}

	@Override
	protected IMediaResource newViewFolder(ViewFactory factory, int level, ViewFolder parent, IMediaFolder decorate) {
		return new OnlineViewFolder(factory, level, parent, decorate);
	}

	@Override
	public IMediaResource getChild(String name) {
		if (!isLoaded(FOLDER_TIMEOUT)) {
			log.warn("Failed to load childen in folder " + getTitle() + " within the folder timeout");
			return null;
		}
		return super.getChild(name);
	}

	@Override
	public IMediaResource getChildById(String id) {
		if (!isLoaded(FOLDER_TIMEOUT)) {
			log.warn("Failed to load childen in folder " + getTitle() + " within the folder timeout");
			return null;
		}
		return super.getChildById(id);
	}
}
