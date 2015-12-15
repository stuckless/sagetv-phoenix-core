package sagex.phoenix.vfs.filters;

import org.apache.log4j.Logger;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.sage.SageMediaFile;

/**
 * @author jusjoken
 */
public class DontLikeFilter extends Filter {
    static private final Logger LOG = Logger.getLogger(DontLikeFilter.class);

    public DontLikeFilter(boolean dontlike) {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "DontLike", "true", ConfigurableOption.DataType.string, true,
                ConfigurableOption.ListSelection.single, "true:Yes,false:No"));
        setValue(String.valueOf(dontlike));
    }

    public DontLikeFilter() {
        this(true);
    }

    public boolean canAccept(IMediaResource res) {
        boolean dontlike = getOption(OPT_VALUE).getBoolean(true);
        if (res instanceof IMediaFolder) {
            // LOG.info("canAccept: IMediaFolder found so returning TRUE - OPT_VALUE = '"
            // + dontlike + "'");
            return true;
        }
        if (res instanceof SageMediaFile) {
            // LOG.info("canAccept: SageMediaFile found so returning '" +
            // ((SageMediaFile)res).isDontLike() == dontlike +
            // "' - OPT_VALUE = '" + dontlike + "'");
            return ((SageMediaFile) res).isDontLike() == dontlike;
        } else if (res instanceof DecoratedMediaFile) {
            // LOG.info("canAccept: DecoratedMediaFile found - OPT_VALUE = '" +
            // dontlike + "' res '" + res + "'");
            return canAccept(((DecoratedMediaFile) res).getDecoratedItem());
        } else {
            // LOG.info("canAccept: default returning FALSE - OPT_VALUE = '" +
            // dontlike + "' res '" + res + "'");
            return false;
        }
    }
}
