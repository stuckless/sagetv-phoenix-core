package sagex.phoenix.vfs.sources;

import java.util.Set;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.CombinedMediaFolder;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.sage.SageSourcesMediaFolder;

/**
 * Simple factory for creating a structured source from a sage media mask. ie,
 * this factory will create a hierarch structure of sage media files.
 *
 * @author seans
 */
public class SageSourcesFactory extends Factory<IMediaFolder> {
    public SageSourcesFactory() {
        super();

        // defined options
        addOption(new ConfigurableOption("mediamask", "Sage Media Mask", null, DataType.string, true, ListSelection.multi,
                "T:TV,D:DVD,B:BluRay,V:Video,M:Music,P:Pictures"));
        addOption(new ConfigurableOption("combine", "Combine Folders", null, DataType.bool));
    }

    public IMediaFolder create(Set<ConfigurableOption> altOptions) {
        boolean combine = getOption("combine", altOptions).getBoolean(false);
        String mediamask = getOption("mediamask", altOptions).getString(null);
        if (combine) {
            return new CombinedMediaFolder(new SageSourcesMediaFolder(mediamask, getLabel()), combine);
        } else {
            return new SageSourcesMediaFolder(mediamask, getLabel());
        }
    }
}
