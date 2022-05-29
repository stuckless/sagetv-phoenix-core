package sagex.phoenix.metadata.provider.tvdb;

import com.omertron.thetvdbapi.model.Actor;
import com.omertron.thetvdbapi.model.Series;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;

import java.util.List;

public class TVDBSeriesParser {
    private static final Logger log = Logger.getLogger(TVDBSeriesParser.class);
    private final TVDBMetadataProvider provider;

    private String seriesId = null;

    public TVDBSeriesParser(TVDBMetadataProvider provider, String seriesId) {
        this.provider=provider;
        this.seriesId = seriesId;

        if (seriesId == null || seriesId.isEmpty()) {
            throw new RuntimeException("Can't get series info without a Series Id, and series id was null.");
        }
    }

    public ISeriesInfo getSeriesInfo() throws MetadataException {
        log.info("TVDB Series: " + seriesId);

        try {
            Series series= provider.getTVDBApi().getSeries(seriesId, provider.getLanguage());
            ISeriesInfo sinfo = MetadataProxy.newInstance(ISeriesInfo.class);

            sinfo.setContentRating(MetadataUtil.fixContentRating(MediaType.TV, series.getContentRating()));
            sinfo.setPremiereDate(series.getFirstAired());

            List<String> genres = series.getGenres();
            if (genres!=null) {
                for (String g : genres) {
                    if (!StringUtils.isEmpty(g)) {
                        sinfo.getGenres().add(g.trim());
                    }
                }
            }

            sinfo.setDescription(series.getOverview());
            sinfo.setUserRating(MetadataSearchUtil.parseUserRating(series.getRating()));
            sinfo.setAirDOW(series.getAirsDayOfWeek());
            sinfo.setAirHrMin(series.getAirsTime());
            // sinfo.setFinaleDate(date);
            // sinfo.setHistory();
            //sinfo.setImage(TVDBMetadataProvider.getFanartURL(getValue(series, "banner")));
            sinfo.setImage(series.getBanner());
            log.info("***** TVDB Series: getBanner '" + series.getBanner() + "'");
            sinfo.setNetwork(series.getNetwork());
            sinfo.setTitle(series.getSeriesName());
            sinfo.setZap2ItID(series.getZap2ItId());

            // external information for lookup later, if needed
            sinfo.setRuntime(MetadataSearchUtil.convertTimeToMillissecondsForSage(series.getRuntime()));

            // actors
            addActors(sinfo);

            return sinfo;
        } catch (Exception e) {
            throw new MetadataException("Failed to get series for " + seriesId, e);
        }
    }

    private void addActors(ISeriesInfo info) {
        try {
            List<Actor> actors = provider.getTVDBApi().getActors(seriesId);

            if (actors != null) {
                for (Actor actor : actors) {
                    CastMember cm = new CastMember();
                    cm.setName(actor.getName());
                    cm.setRole(actor.getRole());
                    cm.setImage(actor.getImage());
                    info.getCast().add(cm);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to process the Actors for series: " + seriesId, e);
        }
    }

}
