package sagex.phoenix.vfs;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.Hints;

@API(group = "media", proxy = true, prefix = "Media", resolver = "phoenix.media.GetMediaResource")
public interface IMediaResource extends Comparable<IMediaResource> {
    /**
     * Constant to reflect unlimited recursion when applying visitors * *
     * {@value}
     */
    public static final int DEEP_UNLIMITED = Integer.MAX_VALUE;

    /**
     * Hint to pass the delete() api to tell the delete() method that the
     * recording was incorrect * * {@value}
     */
    public static final String HINT_DELETE_WITHOUT_PREJUDICE = "delete_without_prejudice";

    /**
     * Return the Media Object that backs this Media Resource. Different kinds
     * of resources may wrap different kinds of "native" objects. This will
     * always return the native object that is being wrapper by the resource.
     *
     * @return
     */
    public Object getMediaObject();

    /**
     * Get Title should be the title/name of the resource (for files, it would
     * include the extension)
     *
     * @return
     */
    public String getTitle();

    /**
     * Thumbnail can be anything that can be consumed by sagetv STV.
     *
     * @return
     */
    public Object getThumbnail();

    /**
     * Return the Parent of this resource
     *
     * @return
     */
    public IMediaFolder getParent();

    /**
     * One of the types listed {@link MediaResourceType}, or a custom type.
     * <p/>
     * An item can have multiple types. isType(FILE) should always return true
     * for a MediaFile and isType(FOLDER) should always return true for a folder
     *
     * @param type
     * @return true if the media resource is the given type
     */
    public boolean isType(int type);

    /**
     * Return true if this resource "exists". Exists may mean local on the
     * filesystem, or a valid url, if it's on only resource.
     *
     * @return
     */
    public boolean exists();

    /**
     * Deletes this resource, if it's able to be deleted
     * <p/>
     * You can pass a Hint of HINT_DELETE_WITH_PREJUDICE=true. It is up the
     * implementation to determine if it will handle hints, etc.
     * <p/>
     * Return true if the resource was deleted
     */
    public boolean delete(Hints hints);

    /**
     * Return the last modified date for this resource
     *
     * @return
     */
    public long lastModified();

    /**
     * Sets the last modified data/time for this resource if possible. ie online
     * resources may not support this, and it should silently fail
     *
     * @param time
     */
    public void touch(long time);

    /**
     * Returns a unique id for this resource. Each resource should be uniquely
     * identifiable.
     *
     * @return
     */
    public String getId();

    /**
     * Visit this resource using the visitor and progress monitor. if the
     * visitor returns true and the visitor has children, then those children
     * will be traversed, so long as it does not surpass the 'deepness'.
     * <p/>
     * At any time the visiting can be stopped by setting the monitor to
     * cancelled.
     * <p/>
     * An implementation should ensure that that monitor is never null.
     * <p/>
     * Passing 0 for deep ensures that only this item is visited. Passing 1 will
     * ensure that this item plus any child elements will be visited.
     *
     * @param visitor
     * @param monitor
     * @param deep    how many levels deep to traverse.
     */
    public void accept(IMediaResourceVisitor visitor, IProgressMonitor monitor, int deep);

    /**
     * Returns true if this resource is watched
     *
     * @return
     */
    public boolean isWatched();

    /**
     * Sets the watched status for this resource
     *
     * @param watched
     */
    public void setWatched(boolean watched);

    /**
     * returns true if this resource was liked
     *
     * @return
     */
    public boolean isDontLike();

    /**
     * Sets whether or not this resource was liked
     *
     * @return
     */
    public void setDontLike(boolean like);

    /**
     * Returns true if this resource is a favourite
     *
     * @return
     */
    public boolean isFavorite();

    /**
     * Returns true if this resource was manually recorded
     *
     * @param manual
     */
    public void setManualRecord(boolean manual);

    /**
     * return true if the this media file was manually recorded
     *
     * @param manual
     */
    public boolean isManualRecord();

    /**
     * returns true if this resource is a part of the managed media library
     *
     * @return
     */
    public boolean isLibraryFile();

    /**
     * returns true if this resource is a part of the managed media library
     *
     * @return
     */
    public void setLibraryFile(boolean library);

    /**
     * Returns the virtual path of this resource within the hierarchy of vfs
     * items. This would be analogous to a file path, but this path is NOT the
     * real path, but rather a virtual path.
     *
     * @return
     */
    public String getPath();
}
