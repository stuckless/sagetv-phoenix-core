package sagex.phoenix.vfs.filters;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

/**
 * A filter for files that the user has started to watch, but
 * has not finished.
 * 
 * @author skiingwiz
 */
public class PartiallyWatchedFilter extends Filter{
    public PartiallyWatchedFilter() {
    	super();
    	addOption(new ConfigurableOption(OPT_VALUE, "Partially Watched", "true", DataType.string, true, ListSelection.single, "true:Yes,false:No"));
    }
    
    @Override
	public boolean canAccept(IMediaResource res) {
        boolean watched = getOption(OPT_VALUE).getBoolean(true); 
        if (res instanceof IMediaFolder) return true;
        if (res instanceof IMediaFile) {
        	IMediaFile file = (IMediaFile)res;
        	
        	//A file is partially watched if some of it is watched, but it hasn't 
        	//  been marked "Watched"
        	return (!file.isWatched() && file.getWatchedDuration() > 0L) == watched;
        } else {
            return false;
        }
    }
}
