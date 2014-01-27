package sagex.phoenix.vfs.sage;

import java.util.List;

import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;

/**
 * Used to create a MediaFolder when you have an Array of Sage MediaFile objects
 * 
 * @author seans
 * 
 */
public class MediaFilesMediaFolder extends VirtualMediaFolder {
	public MediaFilesMediaFolder(IMediaFolder parent, Object[] mediaFiles,
			String title) {
		super(parent, title, (mediaFiles==null?new Object[]{}:mediaFiles), title, true);
	}

	protected void addSageMediaFile(List<IMediaResource> children, Object mf) {
		if (mf == null) {
			// don't add it, it's not a sage file
			return;
		}

		children.add(new SageMediaFile(this, mf));
	}

	@Override
	protected void populateChildren(List<IMediaResource> children) {
		if (getMediaObject() != null) {
			for (Object mf : (Object[])getMediaObject()) {
				addSageMediaFile(children, mf);
			}
		}
	}
}
