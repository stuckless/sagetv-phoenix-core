package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class SeasonGrouper implements IGrouper {
    public SeasonGrouper() {
    }

    public String getGroupName(IMediaResource res) {
        if (res instanceof IMediaFile) {
            int season = ((IMediaFile) res).getMetadata().getSeasonNumber();
            if (season>0) {
                return String.format("Season %02d",season);
            }
        }
        return null;
    }
}
