package sagex.phoenix.vfs.filters;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.sage.SageMediaFile;

public class ReRunFilter extends Filter {
    public ReRunFilter(boolean rerun) {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "ReRun", "true", DataType.string, true, ListSelection.single,
                "true:Yes,false:No"));
        setValue(String.valueOf(rerun));
    }

    public ReRunFilter() {
        this(true);
    }

    public boolean canAccept(IMediaResource res) {
        boolean rerun = getOption(OPT_VALUE).getBoolean(true);
        if (res instanceof IMediaFolder)
            return true;
        if (res instanceof SageMediaFile) {
            return ((SageMediaFile) res).isShowReRun() == rerun;
        } else if (res instanceof DecoratedMediaFile) {
            return canAccept(((DecoratedMediaFile) res).getDecoratedItem());
        } else {
            return false;
        }
    }
}
