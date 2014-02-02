package sagex.phoenix.vfs.views;

import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Special Decorator for View Items. It's basically a type holder.
 * 
 * @author seans
 */
public class ViewItem extends DecoratedMediaFile<ViewFolder> {

	public ViewItem(ViewFolder parent, IMediaFile file) {
		super(parent, file);
	}
}
