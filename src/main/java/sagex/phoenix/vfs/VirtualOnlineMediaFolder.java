package sagex.phoenix.vfs;

/**
 * Convenience Virtual Folder that identifies itself an an online video folder.
 *
 * @author sls
 */
public class VirtualOnlineMediaFolder extends VirtualMediaFolder {

    public VirtualOnlineMediaFolder(String title) {
        super(title);
    }

    public VirtualOnlineMediaFolder(IMediaFolder parent, String title) {
        super(parent, title);
    }

    public VirtualOnlineMediaFolder(IMediaFolder parent, String id, Object resource, String title) {
        super(parent, id, resource, title);
    }

    public VirtualOnlineMediaFolder(IMediaFolder parent, String id, Object resource, String title, boolean thumbnailFromChild) {
        super(parent, id, resource, title, thumbnailFromChild);
    }

    @Override
    public boolean isType(int type) {
        if (type == MediaResourceType.ONLINE.value()) {
            return true;
        }
        return super.isType(type);
    }
}
