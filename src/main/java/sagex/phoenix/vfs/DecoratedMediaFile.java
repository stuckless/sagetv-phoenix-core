package sagex.phoenix.vfs;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.Hints;

import java.io.File;
import java.util.List;

/**
 * Decorator for a MediaItem
 *
 * @param <T>
 * @author seans
 */
public class DecoratedMediaFile<T extends IMediaFolder> implements IMediaFile {
    private T parent = null;
    private IMediaFile file = null;

    public DecoratedMediaFile(T parent, IMediaFile file) {
        this.parent = parent;
        this.file = file;
    }

    public boolean isDontLike() {
        return file.isDontLike();
    }

    public boolean isFavorite() {
        return file.isFavorite();
    }

    public void setDontLike(boolean like) {
        file.setDontLike(like);
    }

    public void setLibraryFile(boolean library) {
        file.setLibraryFile(library);
    }

    public void setManualRecord(boolean manual) {
        file.setManualRecord(manual);
    }

    public void accept(IMediaResourceVisitor visitor, IProgressMonitor monitor, int deep) {
        if (monitor != null && monitor.isCancelled())
            return;
        if (deep >= 0) {
            visitor.visit(this, monitor);
        }
    }

    public int compareTo(IMediaResource o) {
        return file.compareTo(o);
    }

    public IAlbumInfo getAlbumInfo() {
        return file.getAlbumInfo();
    }

    public List<File> getFiles() {
        return file.getFiles();
    }

    public String getId() {
        return file.getId();
    }

    public Object getMediaObject() {
        return file.getMediaObject();
    }

    public IMediaFolder getParent() {
        return parent;
    }

    public Object getThumbnail() {
        return file.getThumbnail();
    }

    public String getTitle() {
        return file.getTitle();
    }

    public boolean isLibraryFile() {
        return file.isLibraryFile();
    }

    public boolean isWatched() {
        return file.isWatched();
    }

    public void setWatched(boolean watched) {
        file.setWatched(watched);
    }

    public boolean isType(int type) {
        return file.isType(type);
    }

    public long getWatchedDuration() {
        return file.getWatchedDuration();
    }

    public boolean delete(Hints hints) {
        if (file.delete(hints)) {
            if (getParent() != null && getParent() instanceof DecoratedMediaFolder) {
                ((DecoratedMediaFolder) getParent()).removeChild(this);
            }
            return true;
        }
        return false;
    }

    public boolean exists() {
        return file.exists();
    }

    public long lastModified() {
        return file.lastModified();
    }

    public void touch(long time) {
        file.touch(time);
    }

    public IMediaFile getDecoratedItem() {
        return file;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DecoratedMediaFile other = (DecoratedMediaFile) obj;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        return true;
    }

    public String toString() {
        return "DecoratedItem: [" + String.valueOf(file) + "]";
    }

    @Override
    public IMetadata getMetadata() {
        return file.getMetadata();
    }

    @Override
    public boolean isManualRecord() {
        return file.isManualRecord();
    }

    @Override
    public long getEndTime() {
        return file.getEndTime();
    }

    @Override
    public long getStartTime() {
        return file.getStartTime();
    }

    public String getPath() {
        return (getParent() == null ? "" : getParent().getPath()) + "/" + getTitle();
    }
}
