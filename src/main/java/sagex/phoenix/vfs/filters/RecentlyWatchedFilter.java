package sagex.phoenix.vfs.filters;

import sagex.api.AiringAPI;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

/**
 * A filter for files that were watch within the last configurable interval (one
 * day, by default)
 *
 * @author skiingwiz
 */
public class RecentlyWatchedFilter extends Filter {
    protected static final String OPT_DURATION = "duration";
    /**
     * The default duration, in minutes
     */
    protected static final int DEF_DURATION = 1440;

    public RecentlyWatchedFilter() {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "Recently Watched", "true", DataType.string, true, ListSelection.single,
                "true:Yes,false:No"));
        addOption(new ConfigurableOption(OPT_DURATION, "Duration (minutes)", String.valueOf(DEF_DURATION), DataType.integer));
    }

    @Override
    public boolean canAccept(IMediaResource res) {
        boolean value = getOption(OPT_VALUE).getBoolean(true);
        long duration = getOption(OPT_DURATION).getInt(DEF_DURATION) * 60000L;

        if (res instanceof IMediaFolder)
            return true;
        if (res instanceof IMediaFile) {
            IMediaFile file = (IMediaFile) res;

            long watchTime = AiringAPI.GetRealWatchedEndTime(file.getMediaObject());
            return (System.currentTimeMillis() - watchTime <= duration) && value;
        } else {
            return false;
        }
    }
}
