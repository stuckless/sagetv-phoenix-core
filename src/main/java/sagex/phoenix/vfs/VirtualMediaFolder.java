package sagex.phoenix.vfs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.Hints;

public class VirtualMediaFolder extends AbstractMediaResource implements IMediaFolder {
    private boolean changed = true;

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    private boolean thumbnailFromChild = false;

    private List<IMediaResource> children = null;

    public VirtualMediaFolder(String title) {
        this(null, title, null, title);
    }

    public VirtualMediaFolder(IMediaFolder parent, String title) {
        this(parent, title, null, title);
    }

    public VirtualMediaFolder(IMediaFolder parent, String id, Object resource, String title) {
        this(parent, id, resource, title, true);
    }

    public VirtualMediaFolder(IMediaFolder parent, String id, Object resource, String title, boolean thumbnailFromChild) {
        super(parent, id, resource, title);
        this.thumbnailFromChild = thumbnailFromChild;
    }

    public List<IMediaResource> getChildren() {
        // only create the children if we've changed
        if (changed == true) {
            changed = false;
            if (children != null) {
                // help gc by clearing out the children
                children.clear();
            }
            createChildren();
            populateChildren(children);
        }

        return children;
    }

    /**
     * this is called after createChildren(). Subclasses can override this
     * method to populate their children. This list will be an empty list to
     * start.
     *
     * @param children2
     */
    protected void populateChildren(List<IMediaResource> children2) {
    }

    /**
     * Sub-Classes can override this method, which will be called whenever the
     * children need to be re-created. You only need to override this method IF
     * you don't want your children loaded into an ArrayList, otherwise, simply
     * override the populateChildren() method, and add your children to that
     * list.
     */
    protected void createChildren() {
        setChildren(new ArrayList<IMediaResource>());
    }

    protected void setChildren(List<IMediaResource> res) {
        this.children = res;
    }

    public Object getThumbnail() {
        if (thumbnailFromChild) {
            if (getChildren().size() > 0) {
                return getChildren().get(0).getThumbnail();
            }
        }
        return super.getThumbnail();
    }

    public void addMediaResource(IMediaResource res) {
        getChildren().add(res);
    }

    public Iterator<IMediaResource> iterator() {
        return getChildren().iterator();
    }

    public boolean isType(int type) {
        if (type == MediaResourceType.FOLDER.value()) {
            return true;
        }
        return super.isType(type);
    }

    /**
     * Gets a child by it's "Title"
     */
    public IMediaResource getChild(String name) {
        if (name == null)
            return null;

        for (IMediaResource res : getChildren()) {
            if (name.equals(res.getTitle()))
                return res;
        }

        return null;
    }

    @Override
    public boolean delete(Hints hints) {
        // copy the children, so that we can delete them
        List<IMediaResource> files = new ArrayList<IMediaResource>(getChildren());
        for (IMediaResource r : files) {
            r.delete(hints);
        }

        if (getParent() != null) {
            getParent().removeChild(this);
        }
        return true;
    }

    @Override
    public void setDontLike(boolean like) {
        for (IMediaResource r : getChildren()) {
            r.setDontLike(like);
        }

        super.setDontLike(like);
    }

    @Override
    public void setManualRecord(boolean manual) {
        for (IMediaResource r : getChildren()) {
            r.setManualRecord(manual);
        }

        super.setManualRecord(manual);
    }

    @Override
    public void setWatched(boolean watched) {
        for (IMediaResource r : getChildren()) {
            r.setWatched(watched);
        }

        super.setWatched(watched);
    }

    @Override
    public void touch(long time) {
        for (IMediaResource r : getChildren()) {
            r.touch(time);
        }

        super.touch(time);
    }

    @Override
    public void accept(IMediaResourceVisitor visitor, IProgressMonitor monitor, int deep) {
        super.accept(visitor, monitor, deep);

        if (deep >= 0) {
            for (IMediaResource r : getChildren()) {
                r.accept(visitor, monitor, deep - 1);
                if (monitor != null && monitor.isCancelled())
                    break;
            }
        }
    }

    @Override
    public IMediaResource findChild(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String child = StringUtils.substringBefore(path, "/");
        String other = StringUtils.substringAfter(path, "/");

        IMediaResource childRes = getChild(child);
        if (childRes == null) {
            // doesn't matter the path, we didn't find anything
            return null;
        }

        if (StringUtils.isEmpty(other)) {
            return childRes;
        }

        if (!(childRes instanceof IMediaFolder)) {
            // we found a child but there is still an extra path, but we are not
            // a folder
            return null;
        }

        return ((IMediaFolder) childRes).findChild(other);
    }

    @Override
    public IMediaResource getChildById(String id) {
        if (id == null)
            return null;

        for (IMediaResource res : getChildren()) {
            if (id.equals(res.getId()))
                return res;
        }

        return null;
    }

    @Override
    public boolean removeChild(IMediaResource child) {
        boolean removed = getChildren().remove(child);
        if (getParent() != null && getChildren().size() == 0) {
            // remove this empty folder from it's parent
            getParent().removeChild(this);
        }
        return removed;
    }
}
