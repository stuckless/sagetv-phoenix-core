package test.junit.lib;

import java.util.Set;

import sagex.phoenix.Phoenix;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.views.OnlineViewFolder;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;

/**
 * Simple Stub View that always returns the same items
 * 
 * @author sls
 *
 */
public class StubViewFactory extends ViewFactory {
	private ViewFolder view;
	
	public StubViewFactory(String name, IMediaFolder view) {
		setName(name);
		if (view.isType(MediaResourceType.ONLINE.value())) {
			this.view = new OnlineViewFolder(this, 0,null, view);
		} else {
			this.view = new ViewFolder(this, 0,null, view);
		}
	}
	
	@Override
	public ViewFolder create(Set<ConfigurableOption> options) {
		return view;
	}
	
	public static void registerView(String factoryId, IMediaFolder view) {
		StubViewFactory stub = new StubViewFactory(factoryId, view);
		Phoenix.getInstance().getVFSManager().getVFSViewFactory().addFactory(stub);
	}
}
