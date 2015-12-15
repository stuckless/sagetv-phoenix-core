package sagex.phoenix.vfs.filters;

import org.apache.log4j.Logger;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

/**
 * @author jusjoken
 */
public class MissingTVFilter extends Filter {
    static private final Logger LOG = Logger.getLogger(MissingTVFilter.class);

    public MissingTVFilter(boolean missingTV) {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "MissingTV", "true", DataType.string, true, ListSelection.single,
                "true:Yes,false:No"));
        setValue(String.valueOf(missingTV));
    }

    public MissingTVFilter() {
        this(true);
    }

    public boolean canAccept(IMediaResource res) {
        boolean missingTV = getOption(OPT_VALUE).getBoolean(true);
        if (res instanceof IMediaFolder) {
            return true;
        }
        if (res instanceof IMediaFile) {
            return phoenix.media.IsMissingTV(res) == missingTV;
        } else {
            return false;
        }
    }
}
