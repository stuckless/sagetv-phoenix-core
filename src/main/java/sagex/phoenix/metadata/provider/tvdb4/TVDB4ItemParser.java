package sagex.phoenix.metadata.provider.tvdb4;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DateUtils;

import java.util.Date;

public class TVDB4ItemParser {
    private static final Logger log = Logger.getLogger(TVDB4ItemParser.class);

    private IMetadata md = null;
    private IMetadataSearchResult result = null;
    private TVDB4MetadataProvider provider;

    public TVDB4ItemParser(TVDB4MetadataProvider prov, IMetadataSearchResult result) {
        this.provider = prov;
        this.result = result;
    }

    public IMetadata getMetadata() throws MetadataException {
        if (md == null) {
            try {
                log.debug("Getting Metadata for Result: " + result);

                // parse and fill
                md = MetadataProxy.newInstance();

                // set our provider info
                md.setMediaProviderID(provider.getInfo().getId());
                md.setMediaProviderDataID(result.getId());

                // update with the query args, and then overwrite if needed
                md.setSeasonNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.SEASON.name())));
                md.setEpisodeNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.EPISODE.name())));
                md.setDiscNumber(NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.DISC.name())));
                md.setOriginalAirDate(DateUtils.parseDate(result.getExtra().get(SearchQuery.Field.EPISODE_DATE.name())));
                md.setMediaType(result.getMediaType().sageValue());

                Boolean continueGetMetadata = false;
                continueGetMetadata = addSeriesInfo(md);

                String season = result.getExtra().get(SearchQuery.Field.SEASON.name());
                String episode = result.getExtra().get(SearchQuery.Field.EPISODE.name());
                String date = result.getExtra().get(SearchQuery.Field.EPISODE_DATE.name());
                String title = result.getExtra().get(SearchQuery.Field.EPISODE_TITLE.name());

                if (continueGetMetadata && !StringUtils.isEmpty(season) && !StringUtils.isEmpty(episode)) {
                    continueGetMetadata = addSeasonEpisodeInfo(md, season, episode);
                }

                if (continueGetMetadata && md.getEpisodeNumber() == 0 && !StringUtils.isEmpty(date)) {
                    continueGetMetadata = addSeasonEpisodeInfoByDate(md, date);
                }

                if (continueGetMetadata && md.getEpisodeNumber() == 0 && !StringUtils.isEmpty(title)) {
                    continueGetMetadata = addSeasonEpisodeInfoByTitle(title);
                }

                // now add in fanart, no point in doing it early
                if(continueGetMetadata){
                    addFanart(md, season);
                }

            } catch (MetadataException me) {
                throw me;
            } catch (Throwable e) {
                throw new MetadataException("Failed while parsing series: " + result, e);
            }

        }

        return md;
    }

    private Boolean addFanart(IMetadata md, String season) {
        try {
            TVDB4JsonHandler jsonHandler = new TVDB4JsonHandler();
            if(jsonHandler.validConfig()){
                jsonHandler.GetFanart(result.getId(), md, season);
            }else{
                log.warn("addFanart: TVDB4 configuration is not valid.");
                return false;
            }
        } catch (Exception e) {
            log.warn("Failed to get fanart for " + result.getId() + "; Season: " + season, e);
        }
        return true;
    }

    private Boolean addSeriesInfo(IMetadata md) throws Exception {
        ISeriesInfo info = ((ITVMetadataProvider) provider).getSeriesInfo(md.getMediaProviderDataID());

        if(info!=null){
            // copy some of the series info to the IMetadata object
            md.getActors().addAll(info.getCast());
            md.setParentalRating(MetadataUtil.fixContentRating(MediaType.TV, info.getContentRating()));
            md.getGenres().addAll(info.getGenres());
            md.setUserRating(info.getUserRating());

            md.setRelativePathWithTitle(info.getTitle());
            md.setMediaTitle(info.getTitle());

            md.setRunningTime(info.getRuntime());
            return true;
        }else{
            //a null info indicates an invalid TVDB4 config so do not continue
            return false;
        }
    }

    private Boolean addSeasonEpisodeInfo(IMetadata md, String season, String episode) {
        int inSeason = NumberUtils.toInt(season, -1);
        int inEpisode = NumberUtils.toInt(episode, -1);

        if (inSeason > 0 && inEpisode > 0) {
            try {
                TVDB4JsonHandler jsonHandler = new TVDB4JsonHandler();
                if(jsonHandler.validConfig()){
                    jsonHandler.GetEpisode(result.getId(), md, inSeason,inEpisode,null, null);
                }else{
                    log.warn("addSeasonEpisodeInfo: TVDB4 configuration is not valid.");
                    return false;
                }
            } catch (Exception e) {
                log.warn("Failed to get season/episode specific information for " + result.getId() + "; Season: " + season
                        + "; episode: " + episode, e);
            }
        } else {
            // TODO: Someday, allow for Season 0 and specials
            log.warn("Can't do lookup by season/epsidoe for season: " + season + "; episode: " + episode);
        }
        return true;
    }

    private Boolean addSeasonEpisodeInfoByDate(IMetadata md, String date) {
        // tvdb requires dashes not dots
        if (date != null)
            date = date.replace('.', '-');

        Date searchDate = DateUtils.parseDate(date);

        if (searchDate!=null) {
            try {
                TVDB4JsonHandler jsonHandler = new TVDB4JsonHandler();
                if(jsonHandler.validConfig()){
                    jsonHandler.GetEpisode(result.getId(), md, null, null,searchDate, null);
                }else{
                    log.warn("addSeasonEpisodeInfoByDate: TVDB4 configuration is not valid.");
                    return false;
                }
            } catch (Exception e) {
                log.warn("Failed to get episode information for " + result.getId() + "; Date: " + searchDate, e);
            }
        } else {
            log.warn("Can't do lookup by Date as searchDate was null for date: " + searchDate);
        }
        return true;
    }

    private Boolean addSeasonEpisodeInfoByTitle(String title) {
        log.debug("addSeasonEpisodeInfoByTitle: Title: " + title);

        if (title!=null) {
            try {
                TVDB4JsonHandler jsonHandler = new TVDB4JsonHandler();
                if(jsonHandler.validConfig()){
                    jsonHandler.GetEpisode(result.getId(), md, null, null, null, title);
                }else{
                    log.warn("addSeasonEpisodeInfoByTitle: TVDB4 configuration is not valid.");
                    return false;
                }
            } catch (Exception e) {
                log.warn("Failed to get episode information for " + result.getId() + "; Title: " + title, e);
            }
        } else {
            // TODO: Someday, allow for Season 0 and specials
            log.warn("Can't do lookup by Title as title was null");
        }
        return true;
    }

}
