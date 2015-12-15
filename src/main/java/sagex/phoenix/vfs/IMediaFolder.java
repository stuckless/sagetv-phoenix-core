package sagex.phoenix.vfs;

import sagex.phoenix.tools.annotation.API;

import java.util.List;

@API(group = "media", proxy = true, prefix = "Media", resolver = "phoenix.umb.GetFolder")
public interface IMediaFolder extends IMediaResource, Iterable<IMediaResource> {
    /**
     * Return the children for this folder. Should never return null, but it
     * could return an empty list.
     *
     * @return
     */
    public List<IMediaResource> getChildren();

    /**
     * Gets a child by "name" or "title". NOTE: A view can have multiple
     * children of the same name, so this method would return the first child
     * with that name.
     *
     * @param name
     * @return
     */
    public IMediaResource getChild(String name);

    /**
     * Gets the child of this view with the given id. Each child withing a
     * folder should never have the same id.
     *
     * @param name
     * @return
     */
    public IMediaResource getChildById(String id);

    /**
     * Finds a child within the hierarchy of folder items that matches this
     * complete path. Will return null if the path cannot be found.
     *
     * @param path
     * @return
     */
    public IMediaResource findChild(String path);

    /**
     * Removes a child fromt he parent, but it does not physically delete the
     * child, it just removes it.
     *
     * @param virtualMediaFile
     */
    public boolean removeChild(IMediaResource child);
}
