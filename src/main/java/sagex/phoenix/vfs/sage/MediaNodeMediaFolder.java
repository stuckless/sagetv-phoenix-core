package sagex.phoenix.vfs.sage;

import java.util.List;

import sagex.api.MediaFileAPI;
import sagex.api.MediaNodeAPI;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;

public class MediaNodeMediaFolder extends VirtualMediaFolder {
    public MediaNodeMediaFolder(MediaNodeMediaFolder parent, Object node) {
        super(parent, MediaNodeAPI.GetNodePrimaryLabel(node), node, MediaNodeAPI.GetNodePrimaryLabel(node));
    }

    @Override
    protected void populateChildren(List<IMediaResource> children) {
        Object node = getMediaObject();
        for (Object o : MediaNodeAPI.GetNodeChildren(node)) {
            if (MediaNodeAPI.IsNodeFolder(o)) {
                children.add(new MediaNodeMediaFolder(this, o));
            } else {
                if (MediaFileAPI.IsMediaFileObject(o)) {
                    children.add(new SageMediaFile(this, o));
                } else {
                    log.debug("Skipping Non Media File: " + MediaNodeAPI.GetNodePrimaryLabel(o));
                }
            }
        }
    }
}
