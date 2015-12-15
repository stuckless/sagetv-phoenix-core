package sagex.phoenix.fanart;

import sagex.phoenix.metadata.MediaType;

/**
 * Simply Media container to hold the Title, Type, and Season for use when doing
 * a Fanart Query
 *
 * @author seans
 */
public class SimpleMediaFile {
    private MediaType mediaType = null;
    private String title = null;
    private int season = 0;

    public SimpleMediaFile() {
    }

    public SimpleMediaFile(MediaType type, String title, int seaon) {
        this.mediaType = type;
        this.title = title;
        this.season = 0;
    }

    public SimpleMediaFile(MediaType type, String title) {
        this(type, title, 0);
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return "MediaFile: [" + mediaType + ", " + title + "]";
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }
}
