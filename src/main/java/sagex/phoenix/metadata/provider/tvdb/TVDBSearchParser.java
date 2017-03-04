package sagex.phoenix.metadata.provider.tvdb;

import com.omertron.thetvdbapi.model.Series;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.remote.streaming.GenericCommandMediaProcess;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.Pair;
import sagex.phoenix.util.ParserUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class TVDBSearchParser {
    private static final Logger log = Logger.getLogger(TVDBSearchParser.class);

    private SearchQuery query = null;
    private List<IMetadataSearchResult> results = new LinkedList<IMetadataSearchResult>();
    private String searchTitle;

    private Comparator<IMetadataSearchResult> sorter = new Comparator<IMetadataSearchResult>() {
        public int compare(IMetadataSearchResult o1, IMetadataSearchResult o2) {
            if (o1.getScore() > o2.getScore())
                return -1;
            if (o1.getScore() < o2.getScore())
                return 1;
            return 0;
        }
    };

    private TVDBMetadataProvider provider;

    public TVDBSearchParser(TVDBMetadataProvider prov, SearchQuery query) {
        this.provider = prov;
        this.query = query;

        searchTitle = query.get(SearchQuery.Field.QUERY);
    }

    public List<IMetadataSearchResult> getResults() throws MetadataException {
        // already parsed
        if (results.size() > 0)
            return results;

        // parse
        try {
            List<Series> list = provider.getTVDBApi().searchSeries(searchTitle, provider.getLanguage());

            int len = list.size();
            if (len == 0) {
                log.warn("Could not find any results for: " + searchTitle);
            }

            for (int i = 0; i < len; i++) {
                addItem(list.get(i));
            }
            Collections.sort(results, sorter);
        } catch (Exception e) {
            // we got a parse exception, let's try to log the response
            log.debug("Search Failed using URL " + searchTitle);

            throw new MetadataException("Failed to get/parse search query", query, e);
        }
        return results;
    }

    private void addItem(Series item) {
        String title = item.getSeriesName();

        if (StringUtils.isEmpty(title)) {
            log.warn("TVDB Item didn't contain a title");
            return;
        }

        log.debug("Series Item" + item);

        MediaSearchResult sr = new MediaSearchResult();
        MetadataSearchUtil.copySearchQueryToSearchResult(query, sr);
        sr.setMediaType(MediaType.TV);
        sr.setProviderId(provider.getInfo().getId());
        Pair<String, String> pair = ParserUtils.parseTitleAndDateInBrackets(sagex.phoenix.util.StringUtils.unquote(title));
        sr.setTitle(pair.first());
        sr.setScore(getScore(pair.first()));
        sr.setYear(DateUtils.parseYear(item.getFirstAired()));
        sr.setId(sagex.phoenix.util.StringUtils.firstNonEmpty(item.getSeriesId(), item.getId(), item.getImdbId()));
        sr.setUrl(sagex.phoenix.util.StringUtils.firstNonEmpty(item.getFanart(), item.getPoster(), item.getBanner()));
        sr.setIMDBId(item.getImdbId());

        results.add(sr);
        log.debug("Added TVDB Title: " + sr.getTitle() + "; ID: " + item.getSeriesId() + "; IMDB: " + item.getImdbId());
    }

    private float getScore(String title) {
        if (title == null)
            return 0.0f;
        try {
            float score = MetadataSearchUtil.calculateCompressedScore(searchTitle, title);
            log.debug(String.format("Comparing:[%s][%s]: %s", searchTitle, title, score));
            return score;
        } catch (Exception e) {
            return 0.0f;
        }
    }
}
