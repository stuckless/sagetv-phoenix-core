package sagex.phoenix.metadata.provider.tmdb;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.provider.ScoredSearchResultSorter;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.url.IUrl;
import sagex.phoenix.util.url.UrlFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TheMovieDBSearchParser {
    private static final Logger log = Logger.getLogger(TheMovieDBMetadataProvider.class);
    private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private List<IMetadataSearchResult> results = new ArrayList<IMetadataSearchResult>();
    private SearchQuery query;
    private MetadataConfiguration cfg = GroupProxy.get(MetadataConfiguration.class);
    private String searchTitle = null;

    private TheMovieDBMetadataProvider provider;

    private static class ScoredTitle {
        String title;
        float score;

        public ScoredTitle(String title, float score) {
            this.title = title;
            this.score = score;
        }
    }

    public TheMovieDBSearchParser(SearchQuery query, TheMovieDBMetadataProvider provider) {
        this.query = query;
        this.provider = provider;
    }

    public void addResults(String searchUrl) {
        // parse
        try {
            IUrl url = UrlFactory.newUrl(searchUrl);
            log.info("Fetching tmdb url: " + searchUrl);
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document doc = parser.parse(url.getInputStream(null, true));

            NodeList nl = doc.getElementsByTagName("movie");
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                addMovie((Element) nl.item(i));
            }
        } catch (Exception e) {
            log.warn("Failed to parse/search using url: " + searchUrl, e);
        }
    }

    public List<IMetadataSearchResult> getResults() {
        // already parsed
        if (results.size() > 0)
            return results;

        // parse
        searchTitle = query.get(SearchQuery.Field.QUERY);
        String url = provider.getSearchUrl(searchTitle);
        addResults(url);

        if (results.size() == 0) {
            log.warn("TMDB Search for " + query + " returned no results.");
        }

        Collections.sort(results, ScoredSearchResultSorter.INSTANCE);
        return results;
    }

    private void addMovie(Element item) {
        if (StringUtils.isEmpty(getElementValue(item, "name"))) {
            log.warn("TheMovieDB Item didn't contain a title: " + item.getTextContent());
            return;
        }

        MediaSearchResult sr = new MediaSearchResult();
        sr.setMediaType(MediaType.MOVIE);
        MetadataSearchUtil.copySearchQueryToSearchResult(query, sr);
        sr.setProviderId(provider.getInfo().getId());

        String title = sagex.phoenix.util.StringUtils.unquote(getElementValue(item, "name"));
        sr.setTitle(title);
        sr.setScore(getScore(title));

        // add alternate title scoring...
        if (cfg.isScoreAlternateTitles()) {
            log.debug("Looking for alternate Titles");
            List<ScoredTitle> scoredTitles = new LinkedList<ScoredTitle>();
            NodeList nl = item.getElementsByTagName("alternative_name");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    String altTitle = el.getTextContent();
                    if (!StringUtils.isEmpty(altTitle)) {
                        altTitle = altTitle.trim();
                        ScoredTitle st = new ScoredTitle(altTitle, getScore(altTitle));
                        scoredTitles.add(st);
                        log.debug("Adding Alternate Title: " + st.title + "; score: " + st.score);
                    }
                }
            }

            if (scoredTitles.size() > 0) {
                ScoredTitle curTitle = new ScoredTitle(sr.getTitle(), sr.getScore());
                for (ScoredTitle st : scoredTitles) {
                    if (st.score > curTitle.score) {
                        curTitle = st;
                    }
                }

                if (curTitle.score > sr.getScore()) {
                    log.debug("Using Alternate Title Score: " + curTitle.score + "; Title: " + curTitle.title);
                    sr.setScore(curTitle.score);
                    sr.setTitle(sr.getTitle() + " (aka " + curTitle.title + ") ");
                }
            }
        }

        sr.setYear(DateUtils.parseYear(getElementValue(item, "released")));
        sr.setId(getElementValue(item, "id"));
        sr.setUrl(provider.getItemUrl(sr.getId()));
        sr.setIMDBId(getElementValue(item, "imdb_id"));

        results.add(sr);
    }

    private float getScore(String title) {
        if (title == null)
            return 0.0f;
        try {
            float score = MetadataSearchUtil.calculateScore(searchTitle, title);
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
