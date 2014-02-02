package sagex.phoenix.upnp;

import java.util.List;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import sagex.phoenix.Phoenix;
import sagex.phoenix.util.ElapsedTimer;
import sagex.phoenix.vfs.DummyMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualOnlineMediaFolder;

public class BaseUPnPMediaFolder extends VirtualOnlineMediaFolder {
	protected Service service = null;
	protected String browseId = null;
	protected boolean loaded = false;

	public BaseUPnPMediaFolder(IMediaFolder parent, String id, Object resource, String title) {
		super(parent, id, resource, title);
	}

	@Override
	protected void populateChildren(final List<IMediaResource> list) {
		final ElapsedTimer t = new ElapsedTimer();

		Browse browse = new Browse(getService(), getBrowseId(), BrowseFlag.DIRECT_CHILDREN) {
			@Override
			public void received(ActionInvocation action, DIDLContent didl) {
				populateContent(list, didl);
				log.debug("Fetched UPnP resources for " + getBrowseId() + " is " + t.delta() + "ms");
				loaded = true;
			}

			@Override
			public void updateStatus(Status arg0) {
			}

			@Override
			public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
				log.warn("Failed to browse UPNP Device; Error: " + arg2);
				list.clear();
				list.add(new DummyMediaFile("ERROR: " + arg2));

				// will force a reload if necessary
				loaded = false;
				setChanged(true);
			}
		};

		Phoenix.getInstance().getUPnPServer().executeAndWait(browse);
		// loaded=false;
		// Phoenix.getInstance().getUPnPServer().executeAsync(browse);

		// wait for the videos to load for 5 seconds
		// isLoaded(OnlineViewFolder.FOLDER_TIMEOUT);
	}

	protected void populateContent(List<IMediaResource> list, DIDLContent didl) {
		if (didl != null) {
			addFolders(list, didl);
			addItems(list, didl);
		}
	}

	private void addItems(List<IMediaResource> list, DIDLContent didl) {
		for (Item i : didl.getItems()) {
			UPnPMediaFile mf = new UPnPMediaFile(this, service, i);
			list.add(mf);
		}
	}

	private void addFolders(List<IMediaResource> list, DIDLContent didl) {
		for (Container c : didl.getContainers()) {
			UPnPMediaFolder mf = new UPnPMediaFolder(this, service, c);
			list.add(mf);
		}
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public String getBrowseId() {
		return browseId;
	}

	public void setBrowseId(String browseId) {
		this.browseId = browseId;
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
	public IMediaResource getChild(String name) {
		if (isLoaded(10000)) {
			return super.getChild(name);
		}
		return null;
	}

	@Override
	public IMediaResource getChildById(String id) {
		if (isLoaded(10000)) {
			return super.getChildById(id);
		}
		return null;
	}

}
