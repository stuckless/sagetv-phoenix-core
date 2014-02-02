package sagex.phoenix.vfs;

import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "Media Configuration", path = "phoenix/media", description = "Common media related options")
public class MediaConfiguration extends GroupProxy {
	@AField(label = "Recording Title Mask", description = "Title mask used by GetFormattedTitle when the media type is a Recording")
	private FieldProxy<String> recordingTitleMask = new FieldProxy<String>("${Title} - ${EpisodeName}");

	@AField(label = "TV Title Mask", description = "Title mask used by GetFormattedTitle when the media type is a TV file")
	private FieldProxy<String> tvTitleMask = new FieldProxy<String>(
			"${Title} - S${SeasonNumber:java.text.DecimalFormat:00}E${EpisodeNumber:java.text.DecimalFormat:00} - ${EpisodeName}");

	@AField(label = "TV Title Mask (Multi CD)", description = "Title mask used by GetFormattedTitle when the media type is a TV file with multiple discs")
	private FieldProxy<String> tvTitleMaskMultiCD = new FieldProxy<String>(
			"${Title} (${Year}) - Disc ${DiscNumber:java.text.DecimalFormat:00}");

	@AField(label = "Movie Title Mask", description = "Title mask used by GetFormattedTitle when the media type is a Movie File")
	private FieldProxy<String> movieTitleMask = new FieldProxy<String>("${EpisodeName} (${Year})");

	@AField(label = "Movie Title Mask (Multi CD)", description = "Title mask used by GetFormattedTitle when the media type is a Movie File with multiple discs")
	private FieldProxy<String> movieTitleMaskMultiCD = new FieldProxy<String>(
			"${EpisodeName} (${Year}) - Disc ${DiscNumber:java.text.DecimalFormat:00}");

	public String getRecordingTitleMask() {
		return recordingTitleMask.get();
	}

	public void setRecordingTitleMask(String recordingTitleMask) {
		this.recordingTitleMask.set(recordingTitleMask);
	}

	public String getTvTitleMask() {
		return tvTitleMask.get();
	}

	public void setTvTitleMask(String tvTitleMask) {
		this.tvTitleMask.set(tvTitleMask);
	}

	public String getTvTitleMaskMultiCD() {
		return tvTitleMaskMultiCD.get();
	}

	public void setTvTitleMaskMultiCD(String tvTitleMaskMultiCD) {
		this.tvTitleMaskMultiCD.set(tvTitleMaskMultiCD);
	}

	public String getMovieTitleMask() {
		return movieTitleMask.get();
	}

	public void setMovieTitleMask(String movieTitleMask) {
		this.movieTitleMask.set(movieTitleMask);
	}

	public String getMovieTitleMaskMultiCD() {
		return movieTitleMaskMultiCD.get();
	}

	public void setMovieTitleMaskMultiCD(String movieTitleMaskMultiCD) {
		this.movieTitleMaskMultiCD.set(movieTitleMaskMultiCD);
	}

	public MediaConfiguration() {
		super();
		init();
	}
}
