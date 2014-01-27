package sagex.phoenix.vfs.sources;

import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.custom.CustomFolders;

/**
 * Simple factory for creating the Custom Folders node.
 * 
 * @author sean
 */
public class CustomFoldersSourceFactory extends Factory<IMediaFolder> {
	public CustomFoldersSourceFactory() {
	}

	@Override
	public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
		return new CustomFolders();
	}
}
