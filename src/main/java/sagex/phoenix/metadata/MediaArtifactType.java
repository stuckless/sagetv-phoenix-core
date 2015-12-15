/**
 *
 */
package sagex.phoenix.metadata;

public enum MediaArtifactType {
    BACKGROUND("Backgrounds"), BANNER("Banners"), POSTER("Posters"), ACTOR("Actors"), EPISODE("Episodes"), ALBUM("Albums");

    private String dir;

    MediaArtifactType(String dir) {
        this.dir = dir;
    }

    public String dirName() {
        return dir;
    }

    public static MediaArtifactType toMediaArtifactType(String artifactType) {
        if (artifactType == null)
            return null;
        artifactType = artifactType.toLowerCase();
        if (artifactType.startsWith("background"))
            return BACKGROUND;
        if (artifactType.startsWith("banner"))
            return BANNER;
        if (artifactType.startsWith("poster"))
            return POSTER;
        if (artifactType.startsWith("actor"))
            return ACTOR;
        if (artifactType.startsWith("episode"))
            return EPISODE;
        if (artifactType.startsWith("album"))
            return ALBUM;
        return null;
    }
}
