package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class YearGrouper implements IGrouper {

    public YearGrouper() {
    }

    public String getGroupName(IMediaResource res) {
        int year=0;
        if (res instanceof IMediaFile) {
            year = ((IMediaFile) res).getMetadata().getYear();
        }
        
        if (year>0) return String.valueOf(year);
        return null;
    }
}
