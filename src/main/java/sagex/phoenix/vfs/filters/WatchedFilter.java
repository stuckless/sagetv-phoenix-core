package sagex.phoenix.vfs.filters;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

public class WatchedFilter extends Filter {
	public WatchedFilter(boolean watched) {
		super();
		addOption(new ConfigurableOption(OPT_VALUE, "Watched", "true", DataType.string, true, ListSelection.single,
				"true:Yes,false:No"));
		setValue(String.valueOf(watched));
	}

	public WatchedFilter() {
		this(true);
	}

	public boolean canAccept(IMediaResource res) {
		boolean watched = getOption(OPT_VALUE).getBoolean(true);
		if (res instanceof IMediaFolder)
			return true;
		if (res instanceof IMediaFile) {
			return ((IMediaFile) res).isWatched() == watched;
		} else {
			return false;
		}
	}
}
