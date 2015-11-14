package sagex.phoenix.vfs.sources;

import java.util.Date;
import java.util.Set;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.TVVirtualMediaFile;
import sagex.phoenix.vfs.VirtualMediaFolder;

/**
 * Factory to create a source using sage TV media and allowing filling episode gaps
 * 
 * @author jusjoken
 * 
 */
public class TVMediaFilesSourceFactory extends Factory<IMediaFolder> {
	private String BaseView = "phoenix.view.default.allTVseasons"; 
	public TVMediaFilesSourceFactory() {
		this(null);
	}

	public TVMediaFilesSourceFactory(String mediaMask) {
		super();
		addOption(new ConfigurableOption("combine", "Combine adjacent missing episodes", null, DataType.bool));
	}

	public IMediaFolder create(Set<ConfigurableOption> opts) {
		boolean combine = getOption("combine", opts).getBoolean(false);
		log.info("Creating Source Folder: " + getLabel() + " For TV Media Files: combine" + combine);
		//Create a view from an existing TV by Season default Phoenix view 
		IMediaFolder folder = phoenix.umb.CreateView(BaseView);
		//create a destination folder to hold the final media list that includes the real episodes as well as the virtual missing items
		VirtualMediaFolder destFolder = new VirtualMediaFolder(folder.getTitle());
		int totalGaps = 0;
		if (folder == null || folder.getChildren().size() == 0) {
			log.warn("Source didn't return any TV media items");
		} else {
			//first level is the shows (series)
			for (IMediaResource show : folder.getChildren()) {
				//log.info("***GAP REVIEW Found Show: " + show.getTitle());
				//second level is the seasons for the show
				for (IMediaResource season : phoenix.media.GetChildren(show)) {
					//log.info("*****GAP REVIEW Found Season: ID: " + season.getId() + " Name: " + season.getTitle());
					//third level are the episodes for the above season
					int eCount = 0;
					int eCurrent = 0;
					int SeasonNum = 0;
					IMediaResource prevEpisode = null;
					for (IMediaResource episode : phoenix.media.GetChildren(season)) {
						boolean fillGap = true;
						eCount++;
						SeasonNum = phoenix.metadata.GetSeasonNumber(episode);
						eCurrent= phoenix.metadata.GetEpisodeNumber(episode);
						if (eCurrent > eCount) {
							//gap found so check if the previous episode was a double episode
							if (prevEpisode != null){
								int prevEpisodeCount = phoenix.metadata.GetEpisodeCount(prevEpisode);
								if (prevEpisodeCount>eCurrent-eCount){
									//not an actual GAP as previous is a multiEpisode item
									fillGap = false;
									log.debug("GAP REVIEW for: " + show.getTitle() + " prevEpisode count fills GAP = " + prevEpisodeCount);
								}else if (prevEpisodeCount>1){
									//previous is multiEpisode BUT not big enough to close gap
									eCount = eCount + (eCurrent - prevEpisodeCount - 1);
									log.debug("GAP REVIEW for: " + show.getTitle() + " eCount reset to = " + eCount );
									if (eCount >= eCurrent){
										fillGap = false;
										log.debug("GAP REVIEW for: " + show.getTitle() + " prev EpisodeCount too high. Reset eCount to = " + eCount );
									}
								}else{
									//previous has no current value for EpisodeCount so check durations
									long pDur = phoenix.media.GetDuration(phoenix.media.GetMediaFile(prevEpisode));
									long cDur = phoenix.media.GetDuration(phoenix.media.GetMediaFile(episode));
									log.debug("GAP REVIEW Checking duration for: " + show.getTitle() + " previous = " + pDur + " : current = " + cDur);
									if (pDur > (cDur * 1.5)){
										//previous episode is much larger than current so assume it is a double episode
										log.debug("GAP REVIEW for: " + show.getTitle() + " double episode found");
										//check if the prev Episode Count has a value - we will not override a user defined value
										if (prevEpisodeCount==0){
											//set the EpisodeCount on the previous episode
											phoenix.metadata.SetEpisodeCount(prevEpisode,2);
											fillGap = false;
											log.debug("GAP REVIEW for: " + show.getTitle() + " double episode - skipping as there is no user setting for the Episode Count");
										}
									}
								}
							}
							if (fillGap){
								if (combine){
									totalGaps = totalGaps + eCurrent - eCount;
									//add the missing episodes - combined
									TVVirtualMediaFile gapItem = new TVVirtualMediaFile(destFolder, episode, show.getTitle(), SeasonNum, eCount, eCurrent-1);
									destFolder.addMediaResource(gapItem);
									eCount = eCurrent;
									log.debug("GAP REVIEW Found: " + gapItem.getTitle() + " : " + gapItem.getId());
								}else{
									for (int eGap = eCount; eGap < eCurrent; eGap++){
										totalGaps++;
										eCount++;
										//add the missing episode
										TVVirtualMediaFile gapItem = new TVVirtualMediaFile(destFolder, episode, show.getTitle(), SeasonNum, eGap);
										destFolder.addMediaResource(gapItem);
										log.debug("GAP REVIEW Found: " + gapItem.getTitle() + " : " + gapItem.getId());
									}
								}
							}else{
								eCount = eCurrent;
							}
						}
						//handle the current item
						if (episode instanceof DecoratedMediaFile) {
							episode = ((DecoratedMediaFile) episode).getDecoratedItem();
						}
						destFolder.addMediaResource(episode);
						//log.info("*******Existing Item: mediaTitle: " + sagex.api.ShowAPI.GetShowTitle(episode.getMediaObject()) + " S" + sagex.api.ShowAPI.GetShowSeasonNumber(episode.getMediaObject()) + "E" + sagex.api.ShowAPI.GetShowEpisodeNumber(episode.getMediaObject()) + " Episode:" + sagex.api.ShowAPI.GetShowEpisode(episode.getMediaObject()) );
						prevEpisode = episode;
					}
				}
			}
			log.info("GAP REVIEW Found: " + totalGaps + " Gaps" );
		}
		return destFolder;
	}
}
