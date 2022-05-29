package sagex.phoenix.metadata.provider.tmdb;

import com.omertron.themoviedbapi.model.Genre;
import com.omertron.themoviedbapi.model.credits.MediaCredit;
import com.omertron.themoviedbapi.model.credits.MediaCreditCast;
import com.omertron.themoviedbapi.model.credits.MediaCreditCrew;
import com.omertron.themoviedbapi.model.media.MediaCreditList;
import com.omertron.themoviedbapi.model.tv.TVInfo;
import com.omertron.thetvdbapi.model.Actor;
import com.omertron.thetvdbapi.model.Series;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.provider.tvdb.TVDBMetadataProvider;
import sagex.phoenix.metadata.provider.tvdb.TVDBSeriesParser;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;

import java.util.List;

/**
 * Created by jusjoken on 9/19/2021.
 */
public class TMDBTVSeriesParser {
    private static final Logger log = Logger.getLogger(TMDBTVSeriesParser.class);
    private final TMDBTVMetadataProvider provider;

    private String seriesId = null;

    public TMDBTVSeriesParser(TMDBTVMetadataProvider provider, String seriesId) {
        this.provider=provider;
        this.seriesId = seriesId;

        if (seriesId == null || seriesId.isEmpty()) {
            throw new RuntimeException("Can't get series info without a Series Id, and series id was null.");
        }
    }

    public ISeriesInfo getSeriesInfo() throws MetadataException {
        log.info("TMDB Series: " + seriesId);

        try {
            TVInfo series= provider.getTVApi().getTVInfo(NumberUtils.toInt(seriesId), provider.getLanguage(),"external_ids");
            ISeriesInfo sinfo = MetadataProxy.newInstance(ISeriesInfo.class);

            //metadata does not have this field so not used but set anyway
            sinfo.setFinaleDate(series.getLastAirDate());

            //used to capture OriginalAirDate
            sinfo.setPremiereDate(series.getFirstAirDate());

            sinfo.setContentRating(MetadataUtil.fixContentRating(MediaType.TV, String.valueOf(series.getRating())));
            System.out.println("****RATINGS**** - getContentRatings List:" + series.getContentRatings());
            System.out.println("****RATINGS**** - getRating:" + series.getRating());
            System.out.println("****RATINGS**** - getPopularity:" + series.getPopularity());
            System.out.println("****RATINGS**** - getVoteCount:" + series.getVoteCount());
            System.out.println("****RATINGS**** - getVoteAverage:" + series.getVoteAverage());

            List<Genre> genres = series.getGenres();
            if (genres!=null) {
                for (Genre g : genres) {
                    sinfo.getGenres().add(g.getName());
                }
            }

            sinfo.setDescription(series.getOverview());
            sinfo.setUserRating(MetadataSearchUtil.parseUserRating(String.valueOf(series.getVoteAverage())));
            //sinfo.setAirDOW(series..getAirsDayOfWeek());
            //sinfo.setAirHrMin(series.getAirsTime());
            // sinfo.setHistory();
            sinfo.setImage(series.getPosterPath());
            sinfo.setNetwork(String.valueOf(series.getNetworks().get(0).getName()));
            sinfo.setTitle(series.getName());

            sinfo.setIMDBID(series.getExternalIDs().getImdbId());

            //sinfo.setZap2ItID(series.getZap2ItId());

            // external information for lookup later, if needed
            sinfo.setRuntime( MetadataSearchUtil.convertTimeToMillissecondsForSage(series.getEpisodeRunTime().get(0).toString()));

            // actors
            addActors(sinfo);

            // crew
            addCrew(sinfo);

            // guests
            addGuests(sinfo);

            return sinfo;
        } catch (Exception e) {
            throw new MetadataException("Failed to get series for " + seriesId, e);
        }
    }

    private void addActors(ISeriesInfo info) {
        try {
            MediaCreditList credits = provider.getTVApi().getTVCredits(NumberUtils.toInt(seriesId),provider.getLanguage());

            for (MediaCreditCast actor: credits.getCast()) {
                CastMember cm = new CastMember();
                cm.setName(actor.getName());
                cm.setRole(actor.getCharacter());
                cm.setImage(actor.getArtworkPath());
                info.getCast().add(cm);
            }

        } catch (Exception e) {
            log.warn("Failed to process the Actors for series: " + seriesId, e);
        }
    }

    private void addGuests(ISeriesInfo info) {
        try {
            MediaCreditList credits = provider.getTVApi().getTVCredits(NumberUtils.toInt(seriesId),provider.getLanguage());

            for (MediaCreditCast guest: credits.getGuestStars()) {
                CastMember cm = new CastMember();
                cm.setName(guest.getName());
                cm.setRole("GUEST:" + guest.getCharacter());
                cm.setImage(guest.getArtworkPath());
                info.getCast().add(cm);
            }

        } catch (Exception e) {
            log.warn("Failed to process the Actors for series: " + seriesId, e);
        }
    }

    private void addCrew(ISeriesInfo info) {
        try {
            MediaCreditList credits = provider.getTVApi().getTVCredits(NumberUtils.toInt(seriesId),provider.getLanguage());

            for (MediaCreditCrew crew: credits.getCrew()) {
                CastMember cm = new CastMember();

                String job = crew.getJob();
                if (job == null)
                    continue;

                cm.setName(crew.getName());
                cm.setRole(crew.getJob());
                cm.setImage(crew.getArtworkPath());

                info.getCast().add(cm);
            }

        } catch (Exception e) {
            log.warn("Failed to process the Crew for series: " + seriesId, e);
        }
    }

}
