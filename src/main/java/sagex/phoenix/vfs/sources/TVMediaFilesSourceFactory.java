package sagex.phoenix.vfs.sources;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Episode;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.factory.ConfigurableOption.DataType;
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
	private TheTVDBApi tvDB = null;
	private TVSeries seriesInfo = null;
	public TVMediaFilesSourceFactory() {
		addOption(new ConfigurableOption("seriesID", "Series ID", null, DataType.string));
		addOption(new ConfigurableOption("seasonNum", "Season Number", null, DataType.string));
		try {
			tvDB = new TheTVDBApi("5645B594A3F32D27");
			
		} catch (Throwable t) {
			log.error("Failed to create The TVDB v1.8 API", t);
			throw new RuntimeException(t);
		}
		log.info("tvDB v1.8 API instance created");
	}

	public IMediaFolder create(Set<ConfigurableOption> opts) {
		String optSeriesID = getOption("seriesID", opts).getString(null);
		String optSeasonNum = null;
		if (optSeriesID!=null){
			optSeasonNum = getOption("seasonNum", opts).getString(null);
		}
		log.info("GAP REVIEW Creating Source Folder: " + getLabel() + " For TV Media Files with optSeriesID '" + optSeriesID + "' optSeasonNum '" + optSeasonNum + "'");
		//Create a view from an existing TV by Season default Phoenix view 
		IMediaFolder folder = phoenix.umb.CreateView(BaseView);
		//create a destination folder to hold the final media list that includes the real episodes as well as the virtual missing items
		VirtualMediaFolder destFolder = new VirtualMediaFolder(folder.getTitle());
		int totalGaps = 0;
		if (folder == null || folder.getChildren().size() == 0) {
			log.warn("GAP REVIEW Source didn't return any TV media items");
		} else {
			//first level is the shows (series)
			for (IMediaResource show : folder.getChildren()) {
				//second level is the seasons for the show
				//get a SeriesID and then retrieve the TVDB info for the series
				String SeriesID= phoenix.metadata.GetMediaProviderDataID(phoenix.media.GetAllChildren((IMediaFolder) show,1).get(0));
				//see if we are optionally handling a specific seriesID or any seriesID
				if (optSeriesID==null || optSeriesID.equals(SeriesID) ){
					log.debug("GAP REVIEW Getting episode info for SERIESID = " + SeriesID + " for show " + show.getTitle());
					seriesInfo = new TVSeries(SeriesID);
					int sCount = 0;
					for (IMediaResource season : phoenix.media.GetChildren(show)) {
						log.debug("GAP REVIEW Found Season: ID: " + season.getId() + " Title: " + season.getTitle());
						//third level are the episodes for the above season
						sCount++;
						//see if we are processing ALL seasons or only a specific season
						if (optSeasonNum==null || String.format("Season %02d", optSeasonNum)==season.getTitle()){
							int eCount = 0;
							int eCurrent = 0;
							int SeasonNum = 0;
							IMediaResource prevEpisode = null;
							for (IMediaResource episode : phoenix.media.GetChildren(season)) {
								boolean fillGap = true;
								eCount++;
								SeasonNum = phoenix.metadata.GetSeasonNumber(episode);
								eCurrent= phoenix.metadata.GetEpisodeNumber(episode);
								SeriesID= phoenix.metadata.GetMediaProviderDataID(episode);
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
											log.debug("GAP REVIEW for: " + show.getTitle() + " Checking duration. previous = " + pDur + " : current = " + cDur);
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
										FillEpisodeGap(destFolder, episode, show.getTitle(), SeasonNum, eCount, eCurrent-1);
										totalGaps = totalGaps + eCurrent - eCount;
										eCount = eCurrent;
									}else{
										eCount = eCurrent;
									}
								}
								//handle the current item
								if (episode instanceof DecoratedMediaFile) {
									episode = ((DecoratedMediaFile) episode).getDecoratedItem();
								}
								destFolder.addMediaResource(episode);
								prevEpisode = episode;
							}
							if(optSeasonNum==null){
								//check if we missed any entire Seasons
								if (SeasonNum > sCount){
									log.debug("GAP REVIEW for: " + show.getTitle() + " Entire Seasons Missing " + sCount + " to " + (SeasonNum-1));
									for (int sGap = sCount; sGap < SeasonNum; sGap++){
										if (seriesInfo.seasonList.containsKey(sGap)){
											FillEpisodeGap(destFolder, prevEpisode, show.getTitle(), sGap, 1, seriesInfo.GetMaxEpisode(sGap));
											totalGaps = totalGaps + seriesInfo.GetMaxEpisode(sGap);
										}
									}
								}
							}
							//check if there are any more episodes at the end of the season
							if (seriesInfo.seasonList.containsKey(SeasonNum) && seriesInfo.GetMaxEpisode(SeasonNum) > eCount){
								log.debug("GAP REVIEW for: " + show.getTitle() + " End of Season " + SeasonNum + " missing episode " + (eCount + 1) + " to " + seriesInfo.GetMaxEpisode(SeasonNum));
								FillEpisodeGap(destFolder, prevEpisode, show.getTitle(), SeasonNum, eCount + 1, seriesInfo.GetMaxEpisode(SeasonNum));
								totalGaps = totalGaps + seriesInfo.GetMaxEpisode(SeasonNum) - eCount + 1;
							}
							
						}
					}
					
				}
				
			}
			log.info("GAP REVIEW Found: " + totalGaps + " Missing Episodes" );
		}
		return destFolder;
	}

	private void FillEpisodeGap(VirtualMediaFolder destFolder, IMediaResource episode, String showTitle, int SeasonNum, int startEpisode, int endEpisode){
		for (int eGap = startEpisode; eGap <= endEpisode; eGap++){
			//add the missing episode
			TVVirtualMediaFile gapItem = new TVVirtualMediaFile(destFolder, episode, showTitle, SeasonNum, eGap, seriesInfo.GetEpisode(SeasonNum, eGap));
			destFolder.addMediaResource(gapItem);
			log.debug("GAP REVIEW Found: " + gapItem.getTitle() + " : " + gapItem.getId());
		}
	}

	//take a seriesID and lookup specific Season and Episode info
	private class TVSeries{
		private String seriesID;
		//private List<Episode> seriesEpisodes = null;
		private HashMap<Integer, TVSeason> seasonList = null;
		private TVSeries(String seriesID){
			this.seriesID = seriesID;
			seasonList = new HashMap<Integer, TVSeason>();
			//this.seriesEpisodes = seriesEpisodes;
			if (this.seriesID != null){
				try {
					loadSeriesInfo();
				} catch (TvDbException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		private Episode GetEpisode(int seasonNum, int episodeNum){
			if (this.seasonList.containsKey(seasonNum)){
				if (this.seasonList.get(seasonNum).episodeList.containsKey(episodeNum)){
					return this.seasonList.get(seasonNum).episodeList.get(episodeNum);
				}
			}
			return null;
		}
		private Integer GetMaxEpisode(int seasonNum){
			if (this.seasonList.containsKey(seasonNum)){
				return this.seasonList.get(seasonNum).MaxEpisode;
			}else{
				return 0;
			}
		}
		
		private void loadSeriesInfo() throws TvDbException{
			List<Episode> seriesEpisodes = null;
			seriesEpisodes = tvDB.getAllEpisodes(this.seriesID, "en");
			if (seriesEpisodes != null){
				for(Episode e: seriesEpisodes ){
					log.debug("loading Episode '" + e + "'");
					if (!this.seasonList.containsKey(e.getSeasonNumber())){
						this.seasonList.put(e.getSeasonNumber(), new TVSeason(e.getEpisodeNumber(), e));
						log.debug("Created a new TVSeason record for Season '" + e.getSeasonNumber() + "'");
					}else{
						//add the episode into the episodeList based on it's episode number
						this.seasonList.get(e.getSeasonNumber()).episodeList.put(e.getEpisodeNumber(), e);
						//check and set the maxEpisode for the season
						if (e.getEpisodeNumber() > this.seasonList.get(e.getSeasonNumber()).MaxEpisode){
							this.seasonList.get(e.getSeasonNumber()).MaxEpisode = e.getEpisodeNumber();
							log.debug("Updating the Max Episode number to '" + e.getEpisodeNumber() + "'");
						}
					}
				}
				
			}
		}
		
		private class TVSeason{
			private int MaxEpisode = 0;
			private HashMap <Integer, Episode> episodeList = null;
			private TVSeason(Integer episodeNum, Episode e){
				episodeList = new HashMap<Integer, Episode>();
				this.episodeList.put(episodeNum, e);
				this.MaxEpisode = episodeNum;
			}
		}
	}
}
