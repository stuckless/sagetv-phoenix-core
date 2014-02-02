package sagex.phoenix.vfs.sources;

import java.io.File;
import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.impl.FileResourceFactory;

/**
 * Creates FileSystem folder for the given 'dir' in the option. This is normally
 * only used by file browsers.
 * 
 * @author seans
 */
public class FileSystemSourceFactory extends Factory<IMediaFolder> {
	public FileSystemSourceFactory() {
		super();
		addOption(new ConfigurableOption("dir", "Directory", null, DataType.directory));
	}

	public IMediaFolder create(Set<ConfigurableOption> configurableOptions) {
		String dir = getOption("dir", configurableOptions).getString(null);
		log.info("Creating Source for dir " + dir);
		if (dir != null) {
			return (IMediaFolder) FileResourceFactory.createResource(null, new File(dir));
		}
		return null;
	}
}
