package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

/**
 * Created by jusjoken on 12/28/2015.
 */
public class MovieCollectionGrouper implements IGrouper {
    public MovieCollectionGrouper(){

    }
    public String getGroupName(IMediaResource res) {
        if (res instanceof IMediaFile) {
            int collectionID = ((IMediaFile) res).getMetadata().getCollectionID();
            if (collectionID > 0) {
                return ((IMediaFile) res).getMetadata().getCollectionName();
            }
        }
        return null;
    }

}
