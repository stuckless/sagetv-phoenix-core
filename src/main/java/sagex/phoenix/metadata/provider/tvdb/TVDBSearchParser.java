package sagex.phoenix.metadata.provider.tvdb;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.IMetadataProvider;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.Pair;
import sagex.phoenix.util.ParserUtils;
import sagex.phoenix.util.url.IUrl;
import sagex.phoenix.util.url.UrlFactory;
import sagex.phoenix.util.url.UrlUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class TVDBSearchParser {
    private static final Logger log = Logger.getLogger(TVDBSearchParser.class);
    private static final String SEARCH_URL = "http://www.thetvdb.com/api/GetSeries.php?seriesname=%s&language=%s";
    private static final String SEARCH_URL_NO_LANG = "http://www.thetvdb.com/api/GetSeries.php?seriesname=%s";
    private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private SearchQuery query = null;
    private IUrl url;
    private List<IMetadataSearchResult> results = new LinkedList<IMetadataSearchResult>();
    private String searchTitle;
    private TVDBConfiguration config = null;
    private Comparator<IMetadataSearchResult> sorter = new Comparator<IMetadataSearchResult>() {
        public int compare(IMetadataSearchResult o1, IMetadataSearchResult o2) {
            if (o1.getScore() > o2.getScore())
                return -1;
            if (o1.getScore() < o2.getScore())
                return 1;
            return 0;
        }
    };

    private IMetadataProvider provider;

    public TVDBSearchParser(IMetadataProvider prov, SearchQuery query) {
        this.provider = prov;
        this.query = query;
        config = GroupProxy.get(TVDBConfiguration.class);
        searchTitle = query.get(SearchQuery.Field.QUERY);

        String surl = null;
        // bug in tvdb, where passing language=en causes an issue, sometimes
        if (StringUtils.isEmpty(config.getLanguage()) || "en".equalsIgnoreCase(config.getLanguage())) {
            surl = String.format(SEARCH_URL_NO_LANG, UrlUtil.encode(searchTitle));
        } else {
            surl = String.format(SEARCH_URL, UrlUtil.encode(searchTitle), config.getLanguage().trim());
        }

        log.info("TVDB Search: " + surl);
        this.url = UrlFactory.newUrl(surl);
    }

    public List<IMetadataSearchResult> getResults() throws MetadataException {
        // already parsed
        if (results.size() > 0)
            return results;

        // parse
        try {
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document doc = parser.parse(url.getInputStream(null, true));

            NodeList nl = doc.getElementsByTagName("Series");
            int len = nl.getLength();
            if (len == 0) {
                log.warn("Could not find any results for: " + url);
            }
            for (int i = 0; i < len; i++) {
                addItem((Element) nl.item(i));
            }
            Collections.sort(results, sorter);
        } catch (Exception e) {
            throw new MetadataException("Failed to get/parse search query " + query, e);
        }
        return results;
    }

    private void addItem(Element item) {
        String title = getElementValue(item, "SeriesName");
        if (StringUtils.isEmpty(title)) {
            log.warn("TVDB Item didn't contain a title: " + item.getTextContent());
            return;
        }

        MediaSearchResult sr = new MediaSearchResult();
        MetadataSearchUtil.copySearchQueryToSearchResult(query, sr);
        sr.setMediaType(MediaType.TV);
        sr.setProviderId(provider.getInfo().getId());
        Pair<String, String> pair = ParserUtils.parseTitleAndDateInBrackets(sagex.phoenix.util.StringUtils.unquote(title));
        sr.setTitle(pair.first());
        sr.setScore(getScore(pair.first()));
        sr.setYear(DateUtils.parseYear(getElementValue(item, "FirstAired")));
        sr.setId(getElementValue(item, "seriesid"));
        sr.setUrl(sr.getId());
        sr.setIMDBId(getElementValue(item, "imdb"));

        results.add(sr);
        log.debug("Added TVDB Title: " + sr.getTitle());
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

    public static String getElementValue(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            return n.getTextContent().trim();
        }
        return null;
    }
}
