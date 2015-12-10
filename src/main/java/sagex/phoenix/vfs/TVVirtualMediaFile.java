package sagex.phoenix.vfs;

import java.util.Date;

import com.omertron.thetvdbapi.model.Episode;

import sagex.phoenix.util.DateUtils;

/**
 * A Virtual TV media file to represent a missing Episode from a specific Show,
 * Season and Episode number
 * 
 * @author jusjoken
 * 
 */
public class TVVirtualMediaFile extends VirtualMediaFile {
	private String series;
	private int season;
	private int episode;
	private String episodeName;
	private String description;
	private IMediaResource refIMR;
	private Episode episodeInfo;
	private Date episodeOAD;
	
	public TVVirtualMediaFile(IMediaFolder parent, IMediaResource refIMR, String series, int season, int episode, Episode episodeInfo) {
		super(parent, series + "S" + season + "E" + episode, series, series);
		this.refIMR = refIMR;
		this.series = series;
		this.season = season;
		this.episode = episode;
		this.episodeInfo = episodeInfo;
		createEpisodeDetails();
		//set the metadata for the tv virtual media file
		createMediaFile();
	}

	public TVVirtualMediaFile(IMediaFolder parent, String id, Object resource, String title) {
		super(parent, id, resource, title);
	}

	@Override
	public boolean isType(int type) {
		if (type == MediaResourceType.MISSINGTV.value()) {
			return true;
		} else if (type == MediaResourceType.TV.value()) {
			return true;
		}
		return super.isType(type);
	}	
	
	protected void createEpisodeDetails() {
		if (this.episodeInfo==null){
			//use default info
			this.episodeName = "Missing Episode: " + episode;
			this.description = "This is a virtual episode record as the episode has been identified as missing.";
			this.episodeOAD = phoenix.metadata.GetOriginalAirDate(this.refIMR); 
		}else{
			//use the info from the passed in TVDB record
			this.episodeName = "Missing: " + this.episodeInfo.getEpisodeName();
			this.description = "Missing Episode: " + this.episodeInfo.getOverview();
			//convert the OAD string
			this.episodeOAD = DateUtils.parseDate(this.episodeInfo.getFirstAired()); 
		}
		return;
	}
	
	protected void createMediaFile() {
		this.getMetadata().setEpisodeName(this.episodeName);
		this.getMetadata().setEpisodeNumber(this.episode);
		this.getMetadata().setSeasonNumber(this.season);
		this.getMetadata().setMediaTitle(this.series);
		this.getMetadata().setMediaType("TV");
		this.getMetadata().setDescription(this.description);
		this.getMetadata().setOriginalAirDate(this.episodeOAD);
		return;
	}
	

}
