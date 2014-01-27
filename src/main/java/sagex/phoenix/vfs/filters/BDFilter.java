package sagex.phoenix.vfs.filters;

import sagex.api.MediaFileAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

public class BDFilter extends Filter {
    public BDFilter() {
    	super();
    }
    
    public boolean canAccept(IMediaResource res) {
        if (res instanceof IMediaFolder) return true;

        if (res instanceof IMediaFile) {
            return (MediaFileAPI.IsBluRay(res.getMediaObject()));
        } 
        else {
            return false;
        }
    }
}
