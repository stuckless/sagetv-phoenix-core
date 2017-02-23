package sagex.phoenix.vfs.groups;

import sagex.api.AiringAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class ChannelGrouper implements IGrouper {

    public ChannelGrouper() {
    }

    @Override
    public String getGroupName(IMediaResource res) {
        String group = null;
        if (res instanceof IMediaFile) {
            group = AiringAPI.GetAiringChannelNumber(res.getMediaObject());
        }

        if (group == null || group.isEmpty()) {
            return res.getTitle();
        } else {
            return group;
        }
    }
}
