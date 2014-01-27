package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaResource;

public class ShowGrouper implements IGrouper {

    public ShowGrouper() {
    }

    public String getGroupName(IMediaResource res) {
        return res.getTitle();
    }
}
