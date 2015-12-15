package sagex.phoenix.remote.streaming;

public class MediaControlInfo {
    private String mediaUrl;
    private String lockFile;

    /**
     * Streamable URL from which client can access the media stream
     *
     * @param indexFile
     */
    public void setMediaUrl(String indexFile) {
        this.mediaUrl = indexFile;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    /**
     * Physical file on the filesystem that needs to exist before we can return.
     * If this is null, then no 'waiting' will occur. Typically this is just the
     * physical file for the media url
     *
     * @param lockFile
     */
    public void setLockFile(String lockFile) {
        this.lockFile = lockFile;
    }

    public String getLockFile() {
        return lockFile;
    }
}
