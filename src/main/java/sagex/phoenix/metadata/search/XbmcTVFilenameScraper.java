package sagex.phoenix.metadata.search;

import java.io.File;
import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import sagex.api.AiringAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.scrapers.xbmc.XbmcScraperProcessor;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.util.PathUtils;

public class XbmcTVFilenameScraper extends XbmcFilenameScraper {
    public XbmcTVFilenameScraper(File scraperFile) throws Exception {
        super(scraperFile);
    }

    public void set(SearchQuery q, Field field, String value) {
        if (!StringUtils.isEmpty(value)) {
            q.set(field, value);
        }
    }

    @Override
    public SearchQuery createSearchQuery(IMediaFile res, Hints hints) {
        // important, or else the filename parser will find %20 in the file
        // names, not good.
        String filenameUri = null;
        try {
            filenameUri = URLDecoder.decode(PathUtils.getLocation(res));
        } catch (Throwable t) {
            filenameUri = PathUtils.getLocation(res);
        }
        SearchQuery q = new SearchQuery(hints);
        q.setMediaType(MediaType.TV);
        String args[] = new String[]{"", filenameUri};

        XbmcScraperProcessor proc = new XbmcScraperProcessor(scraper);
        String title = proc.executeFunction("GetShowName", args);
        if (StringUtils.isEmpty(title)) {
            log.debug("Scraper: " + scraper.getName() + " failed for GetShowName for: " + filenameUri);
            return null;
        }

        // trim it
        title = title.trim();
        set(q, SearchQuery.Field.RAW_TITLE, title);

        // get the year
        String year = proc.executeFunction("GetYear", args);
        if (StringUtils.isEmpty(year)) {
            log.debug("Scraper " + scraper + " failed to parse year for: " + filenameUri);
        } else {
            set(q, SearchQuery.Field.YEAR, year.trim());
        }

        // check if title matches an aired date query
        String airedDate = proc.executeFunction("GetAiredDate", args);
        if (!StringUtils.isEmpty(airedDate)) {
            log.debug("We have a hit for a tv show for: " + filenameUri + "; by aired date: " + airedDate);
            set(q, SearchQuery.Field.EPISODE_DATE, airedDate);
        }

        // continue testing if title has season and episode
        String season = proc.executeFunction("GetSeason", args);
        if (!StringUtils.isEmpty(season)) {
            String ep = proc.executeFunction("GetEpisode", args);
            String dp = "";

            if (StringUtils.isEmpty(ep)) {
                dp = proc.executeFunction("GetDisc", args);
            }

            log.debug("We have a hit for a tv show for: " + filenameUri + " by season: " + season + "; episode/disc: " + ep + "/"
                    + dp);
            set(q, SearchQuery.Field.SEASON, season);
            set(q, SearchQuery.Field.EPISODE, ep);
            set(q, SearchQuery.Field.DISC, dp);
        }

        if (StringUtils.isEmpty(q.get(SearchQuery.Field.EPISODE_TITLE))) {
            // try get episdoe title from
            set(q, SearchQuery.Field.EPISODE_TITLE, proc.executeFunction("GetEpisodeTitle", args));
        }

        // try to find a sage title/airing
        // TODO: If sage is not enabled, then just do the compressed
        // airing title
        String sageAiringId = proc.executeFunction("GetAiringId", args);
        if (!StringUtils.isEmpty(sageAiringId)) {
            log.debug("Using sage airing info to find a title/episode for airing: " + sageAiringId);
            Object airing = AiringAPI.GetAiringForID(NumberUtils.toInt(sageAiringId));
            if (airing != null) {
                set(q, SearchQuery.Field.RAW_TITLE, AiringAPI.GetAiringTitle(airing));
                set(q, SearchQuery.Field.EPISODE_TITLE, ShowAPI.GetShowEpisode(AiringAPI.GetShow(airing)));
                set(q, Field.YEAR, ShowAPI.GetShowYear(AiringAPI.GetShow(airing)));
            }
        }

        // no title
        if (StringUtils.isEmpty(q.get(Field.RAW_TITLE))) {
            log.debug("No TV Query for: " + filenameUri);
            return null;
        }

        // Fixes an issue where the RAW_TITLE may be in CamelCase
        if (sagex.phoenix.util.StringUtils.isCamelCase(q.get(SearchQuery.Field.RAW_TITLE))) {
            set(q, SearchQuery.Field.RAW_TITLE, ScraperUtils.uncompressTitle(q.get(SearchQuery.Field.RAW_TITLE)));
        }

        if (sagex.phoenix.util.StringUtils.isCamelCase(q.get(SearchQuery.Field.EPISODE_TITLE))) {
            set(q, SearchQuery.Field.EPISODE_TITLE, ScraperUtils.uncompressTitle(q.get(SearchQuery.Field.EPISODE_TITLE)));
        }

        log.debug("Created TV Query: " + q);
        return q;
    }
}
