package sagex.phoenix.vfs.sources;

import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;

/**
 * Simple Factory that holds a reference to an existing MediaFolder object, and it
 * uses that Folder for all sources.
 * 
 * This is mainly used for testing, but can also be used programatically.  It will
 * never be available from the xml, since it requires a pre-built MediaFolder as
 * its source.
 * 
 * @author seans
 */
public class MediaFolderSourceFactory extends Factory<IMediaFolder> {
	IMediaFolder folder = null;
	
	public MediaFolderSourceFactory(IMediaFolder folder) {
		super();

		this.folder=folder;
	}

	public IMediaFolder create(Set<ConfigurableOption> altOptions) {
		return folder;
	}
}
