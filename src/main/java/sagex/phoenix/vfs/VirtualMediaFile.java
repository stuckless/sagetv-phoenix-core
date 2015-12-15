package sagex.phoenix.vfs;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.filters.HomeVideosFilter;
import sagex.phoenix.vfs.impl.AlbumInfo;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class VirtualMediaFile extends AbstractMediaResource implements IMediaFile {

    private List<File> files;
    private IMetadata metadata;
    private IAlbumInfo album;

    public VirtualMediaFile(String title) {
        super(null, title, title, title);
    }

    @Override
    public boolean isType(int type) {
        if (type == MediaResourceType.FILE.value()) {
            return true;
        } else if (type == MediaResourceType.ONLINE.value()) {
            return false;
        } else if (type == MediaResourceType.HOME_MOVIE.value()) {
            return HomeVideosFilter.isHomeVideo(this);
        }

        return super.isType(type);
    }

    public VirtualMediaFile(IMediaFolder parent, String id) {
        super(parent, id);
    }

    public VirtualMediaFile(IMediaFolder parent, String id, Object resource, String title) {
        super(parent, id, resource, title);
    }

    @Override
    public IAlbumInfo getAlbumInfo() {
        if (album == null) {
            this.album = createAlbumInfo();
        }
        return album;
    }

    /**
     * Subclasses can override this to provide their own implementation for
     * album info.
     *
     * @return
     */
    protected IAlbumInfo createAlbumInfo() {
        return new AlbumInfo();
    }

    @Override
    public List<File> getFiles() {
        if (files == null)
            files = createFiles();
        return files;
    }

    /**
     * Subclasses need to override this to create a modifiable list if they need
     * to store files in their media resource.
     *
     * @return
     */
    protected List<File> createFiles() {
        return Collections.emptyList();
    }

    /**
     * Adds a managed file part to this media file resource
     *
     * @param file
     */
    public void addFile(File file) {
        getFiles().add(file);
    }

    @Override
    public IMetadata getMetadata() {
        if (metadata == null)
            metadata = createMetadata();
        return metadata;
    }

    /**
     * subclasses can override this to create their own metadata instances
     *
     * @return
     */
    protected IMetadata createMetadata() {
        return MetadataProxy.newInstance();
    }

    @Override
    public long getWatchedDuration() {
        return 0;
    }

    @Override
    public boolean delete(Hints hints) {
        if (getParent() != null) {
            getParent().removeChild(this);
        }
        return true;
    }
}
