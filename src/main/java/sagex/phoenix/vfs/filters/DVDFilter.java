package sagex.phoenix.vfs.filters;

import sagex.api.MediaFileAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

public class DVDFilter extends Filter {
    public DVDFilter() {
        super();
    }

    public boolean canAccept(IMediaResource res) {
        if (res instanceof IMediaFolder)
            return true;

        if (res instanceof IMediaFile) {
            Object o = res.getMediaObject();
            return (MediaFileAPI.IsDVD(o) && !MediaFileAPI.IsBluRay(o));
        } else {
            return false;
        }
    }
}
