package sagex.phoenix.vfs.sources;

import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.IMediaFolder;

/**
 * Simple factory to create a source using a sage media mask
 * 
 * @author seans
 * 
 */
public class SageMediaFilesFactory extends Factory<IMediaFolder> {
	public SageMediaFilesFactory() {
		this(null);
	}

	public SageMediaFilesFactory(String mediaMask) {
		super();
		addOption(new ConfigurableOption("mediamask", "Sage Media Mask", mediaMask, DataType.string, true, ListSelection.multi,
				"T:TV,D:DVD,B:BluRay,V:Video,M:Music,P:Pictures"));
	}

	public IMediaFolder create(Set<ConfigurableOption> opts) {
		String mask = getOption("mediamask", opts).getString(null);
		log.info("Creating Source Folder: " + getLabel() + " For Sage Media Files: " + mask);
		IMediaFolder folder = phoenix.umb.GetSageMediaFiles(mask, getLabel());
		if (folder == null || folder.getChildren().size() == 0) {
			log.warn("Sage Source didn't return any media items for mask: " + mask);
		} else {
			log.info("Returned " + folder.getChildren().size() + " items for " + getLabel() + "; mask: " + mask);
		}
		return folder;
	}
}
