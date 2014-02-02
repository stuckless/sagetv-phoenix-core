package sagex.phoenix.metadata;

import java.util.Date;

import sagex.phoenix.metadata.proxy.SageProperty;

public interface ISagePropertyRO extends ISageMetadata {
	@SageProperty("Width")
	public int getWidth();

	@SageProperty("Height")
	public int getHeight();

	@SageProperty("Track")
	public int getTrack();

	@SageProperty("TotalTracks")
	public int getTotalTracks();

	@SageProperty("Comment")
	public String getComment();

	@SageProperty("AiringTime")
	public Date getAiringTime();

	@SageProperty("ThumbnailOffset")
	public int getThumbnailOffset();

	@SageProperty("ThumbnailSize")
	public int getThumbnailSize();

	@SageProperty("ThumbnailDesc")
	public String getThumbnailDesc();

	@SageProperty("Duration")
	public long getDuration();

	@SageProperty("Picture.Resolution")
	public String getPictureResolution();
}
