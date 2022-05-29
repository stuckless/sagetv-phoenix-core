package sagex.phoenix.metadata.provider.tmdb;

import com.omertron.themoviedbapi.AppendToResponseBuilder;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.enumeration.ArtworkType;
import com.omertron.themoviedbapi.interfaces.AppendToResponse;
import com.omertron.themoviedbapi.interfaces.AppendToResponseMethod;
import com.omertron.themoviedbapi.enumeration.TVMethod;
import com.omertron.themoviedbapi.methods.TmdbEpisodes;
import com.omertron.themoviedbapi.methods.TmdbSeasons;
import com.omertron.themoviedbapi.methods.TmdbTV;
import com.omertron.themoviedbapi.model.artwork.Artwork;
import com.omertron.themoviedbapi.model.credits.MediaCredit;
import com.omertron.themoviedbapi.model.credits.MediaCreditCast;
import com.omertron.themoviedbapi.model.credits.MediaCreditCrew;
import com.omertron.themoviedbapi.model.media.MediaCreditList;
import com.omertron.themoviedbapi.model.tv.TVEpisodeInfo;
import com.omertron.themoviedbapi.model.tv.TVInfo;
import com.omertron.themoviedbapi.model.tv.TVSeasonBasic;
import com.omertron.themoviedbapi.results.ResultList;
import com.omertron.themoviedbapi.tools.*;
import com.omertron.thetvdbapi.TvDbException;
import com.omertron.thetvdbapi.model.Banner;
import com.omertron.thetvdbapi.model.BannerType;
import com.omertron.thetvdbapi.model.Banners;
import com.omertron.thetvdbapi.model.Episode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.yamj.api.common.http.SimpleHttpClientBuilder;
import sage.media.exif.metadata.Metadata;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.json.JSON;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.provider.tvdb.TVDBItemParser;
import sagex.phoenix.metadata.provider.tvdb.TVDBMetadataProvider;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DateUtils;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

import java.net.URL;
import java.util.*;

import static sagex.phoenix.metadata.provider.tmdb.TMDBTVMetadataProvider.getApiKey;

/**
 * Created by jusjoken on 9/19/2021.
 */
public class TMDBTVItemParser {
    private static final Logger log = Logger.getLogger(TMDBTVItemParser.class);

    private IMetadata md = null;
    //private IMetadataSearchResult result = null;
    private IMetadataSearchResult result = null;
    private TMDBTVMetadataProvider provider;
    private TMDBConfiguration config = null;

    public TMDBTVItemParser(TMDBTVMetadataProvider prov, IMetadataSearchResult result) {
        this.provider = prov;
        this.result = result;
        this.config = GroupProxy.get(TMDBConfiguration.class);
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

                addSeriesInfo(md);

                String season = result.getExtra().get(SearchQuery.Field.SEASON.name());
                String episode = result.getExtra().get(SearchQuery.Field.EPISODE.name());
                String date = result.getExtra().get(SearchQuery.Field.EPISODE_DATE.name());
                String title = result.getExtra().get(SearchQuery.Field.EPISODE_TITLE.name());

                if (!StringUtils.isEmpty(season) && !StringUtils.isEmpty(episode)) {
                    addSeasonEpisodeInfo(md, season, episode);
                }

                if (md.getEpisodeNumber() == 0 && !StringUtils.isEmpty(date)) {
                    addSeasonEpisodeInfoByDate(md, date);
                }

                if (md.getEpisodeNumber() == 0 && !StringUtils.isEmpty(title)) {
                    addSeasonEpisodeInfoByTitle(title);
                }

                // now add in banners, no point in doing it early
                addArtwork(md, season, episode);
            } catch (MetadataException me) {
                throw me;
            } catch (Throwable e) {
                throw new MetadataException("Failed while parsing series: " + result, e);
            }
        }

        return md;
    }

    private void addSeriesInfo(IMetadata md) throws Exception {
        ISeriesInfo info = ((ITVMetadataProvider) provider).getSeriesInfo(md.getMediaProviderDataID());
        // copy some of the series info to the IMetadata object
        md.setOriginalAirDate(DateUtils.parseDate(info.getPremiereDate()));

        md.setIMDBID(info.getIMDBID());
        md.setRated(info.getContentRating());
        for (ICastMember member:info.getCast()) {
            if(member.getRole().equalsIgnoreCase("executive producer")){
                md.getExecutiveProducers().add(member);
            }else if(member.getRole().equalsIgnoreCase("producer")){
                md.getProducers().add(member);
            }else if(member.getRole().equalsIgnoreCase("writer")){
                md.getWriters().add(member);
            }else if(member.getRole().equalsIgnoreCase("screenplay")){
                md.getWriters().add(member);
            }else if(member.getRole().startsWith("GUEST:")){
                md.getGuests().add(member);
            }else{
                md.getActors().add(member);
            }
        }

        //md.getActors().addAll(info.getCast());
        md.setParentalRating(MetadataUtil.fixContentRating(MediaType.TV, info.getContentRating()));
        md.getGenres().addAll(info.getGenres());
        md.setUserRating(info.getUserRating());

        md.setRelativePathWithTitle(info.getTitle());
        md.setMediaTitle(info.getTitle());

        md.setRunningTime(info.getRuntime());
    }

    private void updateMetadataFromElement(IMetadata md, TVEpisodeInfo el) {

        md.setSeasonNumber(el.getSeasonNumber());
        md.setEpisodeNumber(el.getEpisodeNumber());
        md.setEpisodeName(sagex.phoenix.util.StringUtils.unquote(el.getName()));

        // actually this is redundant because the tmdb is already YYYY-MM-DD,
        // but this will
        // ensure that we are safe if out internal mask changes
        md.setOriginalAirDate(DateUtils.parseDate(el.getAirDate()));
        // YEAR is not set for TV Metadata, we get that from the Series Info
        // md.setYear(DateUtils.parseYear(DOMUtils.getElementValue(el,
        // "FirstAired")));
        md.setDescription(el.getOverview());

        String epImage = el.getPosterPath();
        if (!StringUtils.isEmpty(epImage)) {
            // Added for EvilPenguin
            md.getFanart().add(
                    new MediaArt(MediaArtifactType.EPISODE, epImage, md.getSeasonNumber()));
        }

        MediaCreditList credits = el.getCredits();
        //process the guestStars
        for (MediaCreditCast cast: credits.getGuestStars()) {
            CastMember cm = new CastMember();
            cm.setName(cast.getName());
            cm.setRole(cast.getCharacter());
            cm.setImage(cast.getArtworkPath());
            md.getGuests().add(cm);
        }
        //process the writers and directors
        //TODO: add other crew
        for (MediaCreditCrew crew: credits.getCrew()) {
            if (crew.getJob().equalsIgnoreCase("writer")){
                CastMember cm = new CastMember();
                cm.setName(crew.getName());
                cm.setRole(crew.getJob());
                cm.setImage(crew.getArtworkPath());
                md.getWriters().add(cm);
            }
            if (crew.getJob().equalsIgnoreCase("screenplay")){
                CastMember cm = new CastMember();
                cm.setName(crew.getName());
                cm.setRole(crew.getJob());
                cm.setImage(crew.getArtworkPath());
                md.getWriters().add(cm);
            }
            if (crew.getJob().equalsIgnoreCase("director")){
                CastMember cm = new CastMember();
                cm.setName(crew.getName());
                cm.setRole(crew.getJob());
                cm.setImage(crew.getArtworkPath());
                md.getDirectors().add(cm);
            }
            if (crew.getJob().equalsIgnoreCase("producer")){
                CastMember cm = new CastMember();
                cm.setName(crew.getName());
                cm.setRole(crew.getJob());
                cm.setImage(crew.getArtworkPath());
                md.getProducers().add(cm);
            }
            if (crew.getJob().equalsIgnoreCase("executive producer")){
                CastMember cm = new CastMember();
                cm.setName(crew.getName());
                cm.setRole(crew.getJob());
                cm.setImage(crew.getArtworkPath());
                md.getExecutiveProducers().add(cm);
            }
        }

    }

    private void addSeasonEpisodeInfoByDate(IMetadata md, String date) throws TvDbException {
        // tvdb requires dashes not dots
        if (date != null)
            date = date.replace('.', '-');


        List<IMetadata> episodes = null;
        try {
            episodes = getAllEpisodes(NumberUtils.toInt(result.getId()),provider.getLanguage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Date inDate = DateUtils.parseDate(date);

        if (episodes!=null && episodes.size()>0) {
            for (IMetadata ep : episodes) {

                if (inDate.equals(ep.getOriginalAirDate())) {
                    // we get the by date xml and then request season/episode
                    // specific one, because the by date xml
                    // is not the same as the by season/episode

                    //Update by copying the IMetaData source to the md destination
                    try {
                        MetadataUtil.copyMetadata(ep,md);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //updateMetadataFromElement(md, e);
                    break;
                }
            }
        }
    }

    private void addSeasonEpisodeInfoByTitle(String title) throws TvDbException {
        log.info("TMDB Title: " + title);

        List<IMetadata> nl = null;
        try {
            nl = getAllEpisodes(NumberUtils.toInt(result.getId()),provider.getLanguage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        boolean updated = updateIfScored(nl, title, 1.0f);
        if (!updated) {
            float matchScore = 0.8f;
            log.debug("Couldn't find an exact title match, so using a fuzzy match score of " + matchScore);

            // do another search, this time use a less sensitive matching
            // criteria
            updated = updateIfScored(nl, title, matchScore);
        }

        if (!updated) {
            log.info("Unable to match a direct title for: " + title);
        }
    }

    private String getSeasonInfo(int tvID, String lang, int multiplier){
        String tmdbResult = "";
        HttpTools http = new HttpTools(new SimpleHttpClientBuilder().build());
        TmdbParameters parameters = new TmdbParameters();
        parameters.add(Param.ID, tvID);
        parameters.add(Param.LANGUAGE, lang);

        int startSeason = 20 * multiplier;

        String tSeasons = "season/" + startSeason;
        for (int i = 1 + startSeason; i < 20 + startSeason; i++) {
            tSeasons += ",season/" + i;
        }
        parameters.add(Param.APPEND, tSeasons);

        URL url = new ApiUrl(getApiKey(), MethodBase.TV).buildUrl(parameters);
        try {
            tmdbResult = http.getRequest(url);
        } catch (MovieDbException e) {
            e.printStackTrace();
        }
        return tmdbResult;
    }

    /*
    * This is used to search through a list of related episodes for either a title, date or other metadata
    * to find a specific episode
    * TMDB does not offer a solution to get all episodes so this approach is to get max 20 seasons at a time
    * and grab the episode info all using a direct call as TheTmdb API does not have this feature
     */
    private List<IMetadata> getAllEpisodes(int id, String lang) throws JSONException{

        final List<IMetadata> epList = new ArrayList<>();

        Boolean hasMoreSeasons = false;
        Integer seasonGroup = 0;
        Integer seasonCount = 0;
        final List<Integer> seasonNums = new ArrayList<>();
        do {
            hasMoreSeasons = false;
            String episodeResult = getSeasonInfo(id,lang,seasonGroup);

            //process the JSON and collect all episodes into the IMetadata list

            JSONObject epJSON = null;
            epJSON = new JSONObject(episodeResult);
            if (epJSON == null) throw new JSONException("JSON Response for TMDBTV did not contain a valid response");

            //get a list of the seasons included in the tv show. Only need to do this once as each loop would have the same list
            if(seasonGroup==0){
                JSON.each("seasons", epJSON, new JSON.ArrayVisitor() {
                    public void visitItem(int i, JSONObject item) {
                        seasonNums.add(JSON.getInt("season_number", item, -1));
                    }
                });
                seasonCount = seasonNums.size();
            }

            //first 0-19 unless less than 19
            //second 20-39 unless less then 39 etc
            int startSeason = 20 * seasonGroup;
            int endSeason = NumberUtils.min(startSeason + 20, seasonCount,seasonCount);

            //determine if another loop is needed to get all the seasons
            if(seasonCount > endSeason){
                hasMoreSeasons = true;
                seasonGroup++;
            }

            //process each season in the JSON season/0 ...
            for (int i = startSeason; i < endSeason  ; i++) {
                final int sNum = seasonNums.get(i);
                JSONObject Season = JSON.get("season/" + sNum, epJSON);
                JSON.each("episodes", Season, new JSON.ArrayVisitor() {
                    public void visitItem(int i, JSONObject item) {
                        final IMetadata tEpisode = MetadataProxy.newInstance();
                        try {
                            MetadataUtil.copyMetadata(md,tEpisode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tEpisode.setEpisodeName(JSON.getString("name", item));
                        tEpisode.setEpisodeNumber(JSON.getInt("episode_number", item));

                        tEpisode.setSeasonNumber(sNum);
                        tEpisode.setOriginalAirDate(DateUtils.parseDate(JSON.getString("air_date", item)));
                        tEpisode.setDescription(JSON.getString("overview", item));
                        tEpisode.setUserRating(MetadataSearchUtil.parseUserRating(JSON.getString("vote_average", item)));

                        //add Crew
                        JSON.each("crew", item, new JSON.ArrayVisitor() {
                            public void visitItem(int i, JSONObject item) {
                                String job = JSON.getString("job", item);
                                if ("director".equalsIgnoreCase(job)) {
                                    tEpisode.getDirectors().add(new CastMember(JSON.getString("name", item), null));
                                } else if ("producer".equalsIgnoreCase(job)) {
                                    tEpisode.getProducers().add(new CastMember(JSON.getString("name", item), null));
                                } else if ("executive producer".equalsIgnoreCase(job)) {
                                    tEpisode.getExecutiveProducers().add(new CastMember(JSON.getString("name", item), null));
                                } else if ("writer".equalsIgnoreCase(job)) {
                                    tEpisode.getWriters().add(new CastMember(JSON.getString("name", item), null));
                                } else if ("screenplay".equalsIgnoreCase(job)) {
                                    tEpisode.getWriters().add(new CastMember(JSON.getString("name", item), null));
                                }
                            }
                        });

                        //add GuestStars
                        JSON.each("guest_stars", item, new JSON.ArrayVisitor() {
                            public void visitItem(int i, JSONObject item) {
                                tEpisode.getGuests().add(new CastMember(JSON.getString("name", item), JSON.getString("character", item)));
                            }
                        });

                        epList.add(tEpisode);
                    }
                });

            }


        }
        while (hasMoreSeasons);



        return epList;
    }

    private boolean updateIfScored(List<IMetadata> nl, String title, float scoreToMatch) {
        if (nl==null) return false;

        boolean updated = false;
        int s = nl.size();
        for (int i = 0; i < s; i++) {
            IMetadata el = nl.get(i);
            String epTitle = el.getEpisodeName();
            float score = MetadataSearchUtil.calculateCompressedScore(title, epTitle);

            if (score >= scoreToMatch) {
                log.debug("Found a title match: " + epTitle + "; Updating Metadata.");

                //Update by copying the IMetaData source to the md destination
                try {
                    MetadataUtil.copyMetadata(el,md);
                    updated = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //updateMetadataFromElement(md, el);
                break;
            }
        }
        return updated;
    }

    private void addSeasonEpisodeInfo(IMetadata md, String season, String episode) {
        int inSeason = NumberUtils.toInt(season, -1);
        int inEpisode = NumberUtils.toInt(episode, -1);

        if (inSeason > 0 && inEpisode > 0) {
            try {
                updateMetadataFromElement(md, provider.getTVApi().getEpisodeInfo(NumberUtils.toInt(result.getId()), inSeason, inEpisode, provider.getLanguage(),"credits,externalIDs,images"));
            } catch (Exception e) {
                log.warn("Failed to get season/episode specific information for " + result.getId() + "; Season: " + season
                        + "; episode: " + episode, e);
            }
        } else {
            // TODO: Someday, allow for Season 0 and specials
            log.warn("Can't do lookup by season/epsidoe for season: " + season + "; episode: " + episode);
        }
    }

    protected void processArt(IMetadata md, ResultList<Artwork> results) {
        if (results == null) {
            return;
        }

        List<Artwork> tmdbResultsList = results.getResults();
        if (tmdbResultsList == null)
            return;

        Collections.sort(tmdbResultsList, Collections.reverseOrder(new Comparator<Artwork>() {
            @Override
            public int compare(Artwork o1, Artwork o2) {
                // prefer english
                int c = sagex.phoenix.util.StringUtils.compare(o1.getLanguage(), o2.getLanguage(), false);
                if (c == 0) {
                    // if both the same, then prefer size or rating
                    if (TMDBConfiguration.PREFER_HIRES.equals(config.getFanartPriorityOrdering())) {
                        c = NumberUtils.compare(o1.getWidth(), o2.getWidth());
                        if (c == 0) {
                            c = NumberUtils.compare(o1.getVoteAverage(), o2.getVoteAverage());
                        }
                    } else if (TMDBConfiguration.PREFER_USER_RATING.equals(config.getFanartPriorityOrdering())) {
                        c = NumberUtils.compare(o1.getVoteAverage(), o2.getVoteAverage());
                        if (c == 0) {
                            c = NumberUtils.compare(o1.getWidth(), o2.getWidth());
                        }
                    } else {
                        // do nothing, no additional sort
                    }
                }
                return c;
            }
        }));

        int maxPosters = 0, maxBackgrounds = 0;
        for (Artwork a : tmdbResultsList) {
            if (a.getLanguage() != null && !a.getLanguage().equalsIgnoreCase(config.getLanguage())) {
                // skip if the language is set, but our language
                // we still process null languages, though.
                continue;
            }

            MediaArt ma = new MediaArt();
            if (a.getArtworkType() == ArtworkType.POSTER) {
                if (maxPosters >= config.getMaxPosters())
                    continue;
                ma.setType(MediaArtifactType.POSTER);
                URL u;
                try {
                    u = provider.getTVApi().createImageUrl(a.getFilePath(), "original");
                    if (u != null) {
                        ma.setDownloadUrl(u.toExternalForm());
                        ma.setCollectionID(md.getCollectionID());
                        maxPosters++;
                    }
                } catch (MovieDbException e) {
                    ma = null;
                }
            } else if (a.getArtworkType() == ArtworkType.BACKDROP) {
                if (maxBackgrounds >= config.getMaxBackgrounds())
                    continue;

                ma.setType(MediaArtifactType.BACKGROUND);
                URL u;
                try {
                    u = provider.getTVApi().createImageUrl(a.getFilePath(), "original");
                    if (u != null) {
                        ma.setDownloadUrl(u.toExternalForm());
                        ma.setCollectionID(md.getCollectionID());
                        maxBackgrounds++;
                    }
                } catch (MovieDbException e) {
                    ma = null;
                }
            } else if (a.getArtworkType() == ArtworkType.STILL) {
                ma.setType(MediaArtifactType.EPISODE);
                URL u;
                try {
                    u = provider.getTVApi().createImageUrl(a.getFilePath(), "original");
                    if (u != null) {
                        ma.setDownloadUrl(u.toExternalForm());
                        ma.setCollectionID(md.getCollectionID());
                    }
                } catch (MovieDbException e) {
                    ma = null;
                }
            } else {
                ma = null;
            }
            if (ma != null) {
                md.getFanart().add(ma);
            }
        }
    }


    private void addArt(IMetadata md, MediaArtifactType mat, Artwork art) {
        if (provider.getLanguage().equals(art.getLanguage())) {
            MediaArt ma = new MediaArt();
            ma.setType(mat);
            addFanartUrl(md, ma, art.getFilePath());
        }
    }

    private void addArtwork(IMetadata md, String season, String episode) throws TvDbException {
        int inSeason = NumberUtils.toInt(season, -9);
        int inEpisode = NumberUtils.toInt(episode, -9);
        List<Artwork> arts = Collections.emptyList();

        //Add series images
        try {
            processArt(md,provider.getTVApi().getTVImages(NumberUtils.toInt(result.getId()),provider.getLanguage()));
        } catch (MovieDbException e) {
            e.printStackTrace();
        }

        //Add episode images
        try {
            processArt(md,provider.getTVApi().getEpisodeImages(NumberUtils.toInt(result.getId()),inSeason,inEpisode));
        } catch (MovieDbException e) {
            e.printStackTrace();
        }

        /* replaced by above
        try {
            arts = provider.getTVApi().getEpisodeImages(NumberUtils.toInt(result.getId()),inSeason,inEpisode).getResults();
            //TODO: seems TMDB does not have banners so no banners added here for now...perhaps from fanart.tv ??
            for (Artwork art:arts ) {
                if (art.getArtworkType().equals(ArtworkType.POSTER)){
                    addArt(md,MediaArtifactType.POSTER,art);
                }else if (art.getArtworkType().equals(ArtworkType.BACKDROP)){
                    addArt(md,MediaArtifactType.EPISODE,art);
                }
            }
        } catch (MovieDbException e) {
            e.printStackTrace();
        }
        */

        //add season images
        try {
            processArt(md,provider.getTVApi().getSeasonImages(NumberUtils.toInt(result.getId()),inSeason,provider.getLanguage()));
        } catch (MovieDbException e) {
            e.printStackTrace();
        }

        /* replaced by above
        try {
            arts = provider.getTVApi().getSeasonImages(NumberUtils.toInt(result.getId()),inSeason,provider.getLanguage()).getResults();
            //TODO: seems TMDB does not have banners so no banners added here for now...perhaps from fanart.tv ??
            for (Artwork art:arts ) {
                if (art.getArtworkType().equals(ArtworkType.POSTER)){
                    addArt(md,MediaArtifactType.POSTER,art);
                }else if (art.getArtworkType().equals(ArtworkType.BACKDROP)){
                    addArt(md,MediaArtifactType.BACKGROUND,art);
                }
            }
        } catch (MovieDbException e) {
            e.printStackTrace();
        }
        */

    }

    private void addFanartUrl(IMetadata md, MediaArt ma, String path) {
        if (StringUtils.isEmpty(path))
            return;
        ma.setDownloadUrl(path);
        md.getFanart().add(ma);
    }

}
