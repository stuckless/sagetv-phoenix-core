package sagex.phoenix.metadata;

public class MediaArt implements IMediaArt {
    private String downloadUrl;
    private MediaArtifactType type;
    private int season;
    private int collectionID;

    public MediaArt(IMediaArt ma) {
        this.downloadUrl = ma.getDownloadUrl();
        this.type = ma.getType();
        this.season = ma.getSeason();
        this.collectionID = ma.getCollectionID();
    }

    public MediaArt(MediaArtifactType type, String downloadUrl, int season, int collectionID) {
        super();
        this.type = type;
        this.downloadUrl = downloadUrl;
        if (collectionID>0){
            this.season = 0;
            this.collectionID = collectionID;
        }else{
            this.season = season;
            this.collectionID = 0;
        }
    }

    public MediaArt(MediaArtifactType type, String downloadUrl, int season) {
        super();
        this.type = type;
        this.downloadUrl = downloadUrl;
        this.season = season;
        this.collectionID = 0;
    }

    public MediaArt(MediaArtifactType type, String downloadUrl) {
        this(type, downloadUrl, 0);
    }

    public MediaArt() {
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public MediaArtifactType getType() {
        return type;
    }

    public void setType(MediaArtifactType type) {
        this.type = type;
    }

    public int getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(int collectionID) {
        this.collectionID = collectionID;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }


    @Override
    public String toString() {
        return "MediaArt [type=" + type + ", downloadUrl=" + downloadUrl + ", season=" + season + ", collectionID=" + collectionID + "]";
    }

}
