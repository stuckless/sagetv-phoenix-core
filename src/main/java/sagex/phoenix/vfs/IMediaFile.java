package sagex.phoenix.vfs;

import java.io.File;
import java.util.List;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.tools.annotation.API;

@API(group = "media", proxy = true, prefix = "MediaFile", resolver = "phoenix.media.GetMediaFile")
public interface IMediaFile extends IMediaResource {
    public IAlbumInfo getAlbumInfo();

    public IMetadata getMetadata();

    public long getWatchedDuration();

    public long getStartTime();

    public long getEndTime();

    /**
     * Get the Physical Files that make up this MediaFile
     *
     * @return
     */
    public List<File> getFiles();
}
