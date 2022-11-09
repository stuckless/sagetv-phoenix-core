package sagex.phoenix.vfs.sources;

import java.util.*;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.enumeration.ExternalSource;
import com.omertron.themoviedbapi.model.FindResults;
import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Episode;

import com.omertron.thetvdbapi.model.Series;
import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.provider.tmdb.TMDBTVItemParser;
import sagex.phoenix.metadata.provider.tmdb.TMDBTVMetadataProvider;
import sagex.phoenix.metadata.provider.tvdb4.TVDB4JsonHandler;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.vfs.*;
import sagex.remote.json.JSONException;

/**
 * Factory to create a source using sage TV media and allowing filling episode gaps
 * 
 * @author jusjoken
 * 
 */
public class TVMediaFilesSourceFactory extends Factory<IMediaFolder> {
	private String BaseView = "phoenix.view.default.allTVseasons"; 
	private TVSeries seriesInfo = null;
	private String defaultTVProvider = "";
	private TheTVDBApi tvDB = null;
	private TMDBTVMetadataProvider tmdbtv = null;
	MetadataConfiguration config = null;

	public TVMediaFilesSourceFactory() {
		addOption(new ConfigurableOption("seriesID", "Series ID", null, DataType.string));
		addOption(new ConfigurableOption("seasonNum", "Season Number", null, DataType.string));
		config = GroupProxy.get(MetadataConfiguration.class);
		setDefaultTVProvider();
	}

	private void setDefaultTVProvider(){
		List<IMetadataProvider> provs = Phoenix.getInstance().getMetadataManager().getProviders(MediaType.TV);
		log.debug("setDefaultTVProvider: available providers:" + provs);
		if(provs.size() > 0 && provs.get(0)!=null){
			defaultTVProvider = provs.get(0).getInfo().getId();
			log.debug("Setting defaultProvider to first TV provider:" + defaultTVProvider);
			//check if user provided a default provider to use and make sure it's valid
			String[] tvProviders = config.getTVProviders().split(",");
			log.debug("TVProviders:" + tvProviders);
			String firstUserTVProvider = "";
			if(tvProviders.length>0){
				firstUserTVProvider = tvProviders[0];
				log.debug("Found users first TV provider:" + firstUserTVProvider);
			}
			if(!firstUserTVProvider.isEmpty()){
				for(IMetadataProvider provider: provs){
					if(provider.getInfo().getId().equals(firstUserTVProvider)){
						defaultTVProvider = firstUserTVProvider;
						log.debug("Setting defaultProvider to users first TV provider:" + defaultTVProvider);
						break;
					}
				}
			}
		}
		log.info("setDefaultTVProvider: setting provider to use:" + defaultTVProvider);

		//load tvdb older api if that is the one selected
		if(defaultTVProvider.equals("tvdb")){
			try {
				tvDB = new TheTVDBApi("5645B594A3F32D27");
			} catch (Throwable t) {
				log.error("Failed to create The TVDB v1.10 API", t);
				throw new RuntimeException(t);
			}
			log.debug("tvDB v1.10 API instance created");
		}else if(defaultTVProvider.equals("tmdb")){
			MetadataManager mgr;
			mgr = Phoenix.getInstance().getMetadataManager();
			IMetadataProvider prov;
			prov = mgr.getProvider("tmdb");
			if(prov==null){
				log.warn("loadSeriesInfoTMDB: failed to load TMDBTV provider");
				return;
			}
			tmdbtv = new TMDBTVMetadataProvider(prov.getInfo());
		}

	}

	public IMediaFolder create(Set<ConfigurableOption> opts) {
		setDefaultTVProvider();
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
				//get a SeriesID and then retrieve the TV info for the series
				Object firstShow = phoenix.media.GetAllChildren((IMediaFolder) show,1).get(0);
				String ProviderID= phoenix.metadata.GetMediaProviderID(firstShow);
				String SeriesID= phoenix.metadata.GetMediaProviderDataID(firstShow);
				String IMDBID= phoenix.metadata.GetIMDBID(firstShow);
				if(!ProviderID.equals(defaultTVProvider)){
					log.info("GAP REVIEW: Search ProviderID:" + defaultTVProvider + " not the same as Source ProviderID:" + ProviderID + " IMDB:" + IMDBID + " for show:" + show.getTitle());
					if(IMDBID==null){
						log.warn("GAP REVIEW: Cannot search for a series ID as IMDB ID is not available for:" + show.getTitle() + " SKIPPING.");
						continue;
					}
					String newSeriesID = getSeriesID(IMDBID,show.getTitle());
					if(newSeriesID!=null){
						SeriesID = newSeriesID;
						log.info("GAP REVIEW: Found new seriesID for the current provider:" + defaultTVProvider + " SeriesID:" + SeriesID);
					}else{
						log.warn("GAP REVIEW: No new seriesID found for the current provider:" + defaultTVProvider + " Skipping:" + show.getTitle());
						continue;
					}
				}
				if(SeriesID==null || SeriesID.isEmpty()){
					log.warn("GAP REVIEW: Skipping as No SERIESID for show " + show.getTitle());
					continue;
				}
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
								log.debug("GAP REVIEW for: " + show.getTitle() + " Check for Entire Seasons Missing sCount:" + sCount + " SeasonNum:" + SeasonNum);
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
					//check if we are missing full seasons AFTER the last season we have
					for (Integer seasonNum: seriesInfo.seasonList.keySet()) {
						log.debug("GAP REVIEW EXTRA SEASONS: checking season:" + seasonNum);
						if(seasonNum > sCount){
							if (seriesInfo.seasonList.containsKey(seasonNum)){
								log.debug("GAP REVIEW EXTRA SEASONS: found season to add:" + seasonNum);
								FillEpisodeGap(destFolder, (IMediaResource) firstShow, show.getTitle(), seasonNum, 1, seriesInfo.GetMaxEpisode(seasonNum));
								totalGaps = totalGaps + seriesInfo.GetMaxEpisode(seasonNum);
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
			if(seriesInfo.GetEpisode(SeasonNum,eGap)!=null){
				TVVirtualMediaFile gapItem = new TVVirtualMediaFile(destFolder, episode, showTitle, SeasonNum, eGap, seriesInfo.GetEpisode(SeasonNum, eGap));
				destFolder.addMediaResource(gapItem);
				log.debug("GAP REVIEW Found: " + gapItem.getTitle() + " : " + gapItem.getId());
			}else{
				log.debug("GAP REVIEW Skipping Null Episode Found for Season: " + SeasonNum + " Episode: " + eGap);
			}
		}
	}

	private String getSeriesID(String IMDBID, String title){
		if(IMDBID==null || IMDBID.isEmpty()){
			log.debug("getSeriesID: called with empty or null IMDBID");
			return null;
		}
		String seriesID = null;
		if(defaultTVProvider.isEmpty()){
			log.debug("getSeriesID: called with empty TV provider");
		}else if(defaultTVProvider.equals("tvdb")){
			try {
				log.debug("getSeriesID: getting seriesID for provider:" + defaultTVProvider + " title:" + title);
				List<Series> seriesResult = tvDB.searchSeries(title, "en");
				if(seriesResult!=null){
					Integer checkMax = 3;
					Integer checkCount = 0;
					String firstSeriesID = null;
					log.debug("getSeriesID: checking for:" + IMDBID + " seriesResult:" + seriesResult);
					for (Series series: seriesResult) {
						checkCount++;
						if(checkCount==1){
							firstSeriesID = series.getId();
						}
						log.debug("getSeriesID: checking for:" + IMDBID + " series imdb:" + series.getImdbId() + " series title:" + series.getSeriesName() + " Id:" + series.getId() + " seriesID:" + series.getSeriesId());
						//if no series found use the first result and check more info
						Series checkSeries = tvDB.getSeries(series.getId(),"en");
						log.debug("getSeriesID: checkSeries for:" + IMDBID + " series imdb:" + checkSeries.getImdbId() + " series title:" + checkSeries.getSeriesName() + " Id:" + checkSeries.getId() + " seriesID:" + checkSeries.getSeriesId());
						if(checkSeries.getImdbId().equals(IMDBID)){
							log.debug("getSeriesID: found seriesID getId:" + checkSeries.getId());
							seriesID = checkSeries.getId();
							return seriesID;
						}
						if(checkCount>=checkMax){
							log.debug("getSeriesID: Max IMDB check of " + checkMax + " so returning first seriesID getId:" + firstSeriesID);
							return firstSeriesID;
						}
					}
				}
			} catch (TvDbException e) {
				e.printStackTrace();
			}
		}else if(defaultTVProvider.equals("tmdb")){
			log.debug("getSeriesID: called with TV provider:" + defaultTVProvider);
			FindResults findResults = null;
			try {
				findResults = tmdbtv.getTVApi().find(IMDBID, ExternalSource.IMDB_ID, "en");
				log.debug("getSeriesID: findResults:" + findResults);
			} catch (MovieDbException e) {
				e.printStackTrace();
			}
			if(findResults!=null){
				log.debug("getSeriesID: findResults.getTvResults:" + findResults.getTvEpisodeResults());
				if(findResults.getTvEpisodeResults().size()>0){
					log.debug("getSeriesID: findResults.getTvResults first item:" + findResults.getTvEpisodeResults().get(0));
					seriesID = String.valueOf(findResults.getTvEpisodeResults().get(0).getShowId());
				}
			}
		}else if(defaultTVProvider.equals("tvdb4")){
			log.debug("getSeriesID: called with TV provider:" + defaultTVProvider);
			//TODO: need TVDB4 method to get a new series ID from IMDBID
			TVDB4JsonHandler handler = new TVDB4JsonHandler();
			try {
				if(handler.validConfig()){
					seriesID = handler.GetSeriesIDFromIMDBID(IMDBID);
				}else{
					log.warn("loadSeriesInfoTVDB4: TVDB4 configuration is not valid.");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			log.warn("getSeriesID: called with invalid TV provider:" + defaultTVProvider);
		}
		log.debug("getSeriesID: returning seriesID:" + seriesID);
		return seriesID;
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
				//get series info based on the default TV Provider in use
				loadSeriesInfo();
			}
		}
		private IMetadata GetEpisode(int seasonNum, int episodeNum){
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

		private void loadSeriesInfo(){
			if(defaultTVProvider.isEmpty()){
				log.debug("loadSeriesInfo: called with empty TV provider");
			}else if(defaultTVProvider.equals("tvdbold")){
				try {
					loadSeriesInfoTVDB();
				} catch (TvDbException e) {
					e.printStackTrace();
				}
			}else if(defaultTVProvider.equals("tmdb")){
				log.debug("loadSeriesInfo: called with TV provider:" + defaultTVProvider);
				loadSeriesInfoTMDB();
			}else if(defaultTVProvider.equals("tvdb")){
				log.debug("loadSeriesInfo: called with TV provider:" + defaultTVProvider);
				loadSeriesInfoTVDB4();
			}else if(defaultTVProvider.equals("tvdb4")){
				log.debug("loadSeriesInfo: called with TV provider:" + defaultTVProvider);
				loadSeriesInfoTVDB4();
			}else{
				log.warn("loadSeriesInfo: called with invalid TV provider:" + defaultTVProvider);
			}
		}
		
		private void loadSeriesInfoTVDB() throws TvDbException{
			log.debug("loadSeriesInfoTVDB: provider specific code STARTS here");
			List<Episode> seriesEpisodes = null;
			seriesEpisodes = tvDB.getAllEpisodes(this.seriesID, "en");
			if (seriesEpisodes != null){
				for(Episode e: seriesEpisodes ){
					log.debug("loadSeriesInfoTVDB: loading Episode '" + e + "'");
					if (!this.seasonList.containsKey(e.getSeasonNumber())){
						this.seasonList.put(e.getSeasonNumber(), new TVSeason(e.getEpisodeNumber(), e));
						log.debug("loadSeriesInfoTVDB: Created a new TVSeason record for Season '" + e.getSeasonNumber() + "'");
					}else{
						//add the episode into the episodeList based on it's episode number
						this.seasonList.get(e.getSeasonNumber()).episodeList.put(e.getEpisodeNumber(), getIMetadataFromEpisode(e));
						//check and set the maxEpisode for the season
						if (e.getEpisodeNumber() > this.seasonList.get(e.getSeasonNumber()).MaxEpisode){
							this.seasonList.get(e.getSeasonNumber()).MaxEpisode = e.getEpisodeNumber();
							log.debug("loadSeriesInfoTVDB: Updating the Max Episode number to '" + e.getEpisodeNumber() + "'");
						}
					}
				}
				
			}
			log.debug("loadSeriesInfoTVDB: provider specific code ENDS here");
		}

		private void loadSeriesInfoTMDB(){
			TMDBTVItemParser tmdbtvItemParser = new TMDBTVItemParser(tmdbtv,null);
			List<IMetadata> seriesEpisodes = new ArrayList<>();

			try {
				seriesEpisodes = tmdbtvItemParser.getAllEpisodes(Integer.parseInt(this.seriesID),"en");
				log.debug("loadSeriesInfoTMDB: retrieved:" + seriesEpisodes.size() + " episodes");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			loadSeriesInfoShared(seriesEpisodes);
		}

		private void loadSeriesInfoTVDB4(){
			TVDB4JsonHandler handler = new TVDB4JsonHandler();
			List<IMetadata> seriesEpisodes = null;
			try {
				if(handler.validConfig()){
					seriesEpisodes = handler.GetEpisodes(this.seriesID);
				}else{
					log.warn("loadSeriesInfoTVDB4: TVDB4 configuration is not valid.");
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			loadSeriesInfoShared(seriesEpisodes);
		}

		private void loadSeriesInfoShared(List<IMetadata> seriesEpisodes){
			if (seriesEpisodes != null){
				for(IMetadata e: seriesEpisodes ){
					log.debug("loadSeriesInfoShared: loading Episode '" + e + "'");
					if (!this.seasonList.containsKey(e.getSeasonNumber())){
						this.seasonList.put(e.getSeasonNumber(), new TVSeason(e.getEpisodeNumber(), e));
						log.debug("loadSeriesInfoShared: Created a new TVSeason record for Season '" + e.getSeasonNumber() + "'");
					}else{
						//add the episode into the episodeList based on it's episode number
						this.seasonList.get(e.getSeasonNumber()).episodeList.put(e.getEpisodeNumber(), e);
						//check and set the maxEpisode for the season
						if (e.getEpisodeNumber() > this.seasonList.get(e.getSeasonNumber()).MaxEpisode){
							this.seasonList.get(e.getSeasonNumber()).MaxEpisode = e.getEpisodeNumber();
							log.debug("loadSeriesInfoShared: Updating the Max Episode number to '" + e.getEpisodeNumber() + "'");
						}
					}
				}
			}
		}

		private class TVSeason{
			private int MaxEpisode = 0;
			private HashMap <Integer, IMetadata> episodeList = null;
			private TVSeason(Integer episodeNum, IMetadata e){
				episodeList = new HashMap<Integer, IMetadata>();
				this.episodeList.put(episodeNum, e);
				this.MaxEpisode = episodeNum;
			}
			private TVSeason(Integer episodeNum, Episode e){
				episodeList = new HashMap<Integer, IMetadata>();
				this.episodeList.put(episodeNum, getIMetadataFromEpisode(e));
				this.MaxEpisode = episodeNum;
			}
		}

		private IMetadata getIMetadataFromEpisode(Episode e){
			IMetadata thisEpisode = MetadataProxy.newInstance();
			thisEpisode.setEpisodeNumber(e.getEpisodeNumber());
			thisEpisode.setSeasonNumber(e.getSeasonNumber());
			thisEpisode.setEpisodeName(e.getEpisodeName());
			thisEpisode.setDescription(e.getOverview());
			thisEpisode.setOriginalAirDate(DateUtils.parseDate(e.getFirstAired()));
			thisEpisode.setMediaProviderID("tvdb");
			thisEpisode.setMediaProviderDataID(e.getId());

			return thisEpisode;
		}

	}
}
