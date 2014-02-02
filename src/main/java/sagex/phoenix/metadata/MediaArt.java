package sagex.phoenix.metadata;

public class MediaArt implements IMediaArt {
	private String downloadUrl;
	private MediaArtifactType type;
	private int season;

	public MediaArt(IMediaArt ma) {
		this.downloadUrl = ma.getDownloadUrl();
		this.type = ma.getType();
		this.season = ma.getSeason();
	}

	public MediaArt(MediaArtifactType type, String downloadUrl, int season) {
		super();
		this.type = type;
		this.downloadUrl = downloadUrl;
		this.season = season;
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

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	@Override
	public String toString() {
		return "MediaArt [type=" + type + ", downloadUrl=" + downloadUrl + ", season=" + season + "]";
	}

}
