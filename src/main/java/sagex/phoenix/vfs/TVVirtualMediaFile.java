package sagex.phoenix.vfs;

import java.util.Date;

import com.omertron.thetvdbapi.model.Episode;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.util.DateUtils;

/**
 * A Virtual TV media file to represent a missing Episode from a specific Show,
 * Season and Episode number
 *
 * @author jusjoken
 */
public class TVVirtualMediaFile extends VirtualMediaFile {
    private String series;
    private int season;
    private int episode;
    private String episodeName;
    private String description;
    private IMediaResource refIMR;
    private IMetadata episodeInfo;
    private Date episodeOAD;

    public TVVirtualMediaFile(IMediaFolder parent, IMediaResource refIMR, String series, int season, int episode, IMetadata episodeInfo) {
        super(parent, series + "S" + season + "E" + episode, refIMR, series);
        this.refIMR = refIMR;
        this.series = series;
        this.season = season;
        this.episode = episode;
        this.episodeInfo = episodeInfo;
        log.debug("**** Creating folder from: series:" + series + " season:" + season + " episode:" + episode + " episodeInfo:" + episodeInfo);
        createEpisodeDetailsFromSource(episodeInfo);
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

    private void createEpisodeDetailsFromSource(String epName, String epDescritopn, Date epOAD){
        this.episodeName = "Missing: " + epName;
        this.description = "Missing Episode: " + epDescritopn;
        this.episodeOAD = epOAD;
    }
    private void createEpisodeDetailsFromSource(IMetadata episode){
        this.episodeName = "Missing: " + episode.getEpisodeName();
        this.description = "Missing Episode: " + episode.getDescription();
        this.episodeOAD = episode.getOriginalAirDate();
    }

    protected void createMediaFile() {
        this.getMetadata().setEpisodeName(this.episodeName);
        this.getMetadata().setEpisodeNumber(this.episode);
        this.getMetadata().setSeasonNumber(this.season);
        this.getMetadata().setMediaTitle(this.series);
        this.getMetadata().setMediaType("TV");
        this.getMetadata().setDescription(this.description);
        this.getMetadata().setOriginalAirDate(this.episodeOAD);
        this.getMetadata().setMediaProviderID(this.episodeInfo.getMediaProviderID());
        this.getMetadata().setMediaProviderDataID(this.episodeInfo.getMediaProviderDataID());
        return;
    }


}
