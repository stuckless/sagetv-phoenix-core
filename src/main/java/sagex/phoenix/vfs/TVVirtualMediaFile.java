package sagex.phoenix.vfs;

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
	private int endepisode;
	private String episodeName;
	private String description;
	private boolean combine;
	private IMediaResource refIMR;
	
	public TVVirtualMediaFile(IMediaFolder parent, IMediaResource refIMR, String series, int season, int episode) {
		super(parent, series + "S" + season + "E" + episode, series, series);
		this.refIMR = refIMR;
		this.series = series;
		this.season = season;
		this.episode = episode;
		this.endepisode = 0;
		this.combine = false;
		createEpisodeInfo();
		//set the metadata for the tv virtual media file
		createMediaFile();
	}

	//use this constructor if you are combining adjacent episodes
	public TVVirtualMediaFile(IMediaFolder parent, IMediaResource refIMR, String series, int season, int startepisode, int endepisode) {
		super(parent, series + "S" + season + "E" + startepisode + "-" + endepisode, series, series);
		this.refIMR = refIMR;
		this.series = series;
		this.season = season;
		this.episode = startepisode;
		this.endepisode = endepisode;
		this.combine = true;
		createEpisodeInfo();
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
	
	protected void createEpisodeInfo() {
		if (combine){
			this.episodeName = "Missing Episodes: " + episode + "-" + endepisode;
			this.description = "This is a virtual episode record as these episodes have been identified as missing.";
		}else{
			this.episodeName = "Missing Episode: " + episode;
			this.description = "This is a virtual episode record as the episode(s) has been identified as missing.";
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
		this.getMetadata().setOriginalAirDate(phoenix.metadata.GetOriginalAirDate(this.refIMR));
		return;
	}
	

}
