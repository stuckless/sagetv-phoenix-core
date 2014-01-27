package sagex.phoenix.vfs.filters;

import sagex.phoenix.Phoenix;
import sagex.phoenix.vfs.IMediaResource;

/**
 * will only accept a file, if it has not been excluded
 * @author sean
 *
 */
public class GlobalExcludeFilter extends Filter {
	public GlobalExcludeFilter() {
    	super();
    }
    
    public boolean canAccept(IMediaResource res) {
    	return !Phoenix.getInstance().getMetadataManager().isExcluded(res);
    }
}
