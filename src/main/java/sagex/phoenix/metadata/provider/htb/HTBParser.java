package sagex.phoenix.metadata.provider.htb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.provider.ScoredSearchResultSorter;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.url.IUrl;
import sagex.phoenix.util.url.UrlFactory;

public class HTBParser {
    private static final Logger log = Logger.getLogger(HTBParser.class);
    private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private Map<String, HTBSearchResult> results = new HashMap<String, HTBSearchResult>();

    private String searchTitle = null;

    private HTBMetadataProvider provider;
    private String url;

    public HTBParser(String url, String title, HTBMetadataProvider provider) {
        this.url = url;
        this.provider = provider;
        this.searchTitle = title;
    }

    public void addResults(String searchUrl) {
        // parse
        try {
            IUrl url = UrlFactory.newUrl(searchUrl);
            log.info("Fetching htbackdrops url: " + searchUrl);
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document doc = parser.parse(url.getInputStream(null, true));

            NodeList nl = doc.getElementsByTagName("image");
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                addImage((Element) nl.item(i));
            }
        } catch (Exception e) {
            log.warn("Failed to parse/search using url: " + searchUrl, e);
        }
    }

    public Collection<HTBSearchResult> getResults() {
        // already parsed
        if (results.size() > 0)
            return results.values();

        // parse
        addResults(url);

        if (results.size() == 0) {
            log.warn("HTB Search for " + url + " returned no results.");
        }

        List<HTBSearchResult> items = new ArrayList<HTBSearchResult>(results.values());
        Collections.sort(items, ScoredSearchResultSorter.INSTANCE);
        return items;
    }

    private void addImage(Element item) {
        String mbid = getElementValue(item, "mbid");
        if (mbid == null) {
            log.warn("HTBackdrops Item didn't contain a valid entry: " + item.getTextContent());
            return;
        }

        HTBSearchResult sr = results.get(mbid);
        if (sr == null) {
            sr = new HTBSearchResult();
            sr.setMediaType(MediaType.MUSIC);
            sr.setProviderId(provider.getInfo().getId());
            String title = sagex.phoenix.util.StringUtils.unquote(getElementValue(item, "mb_name"));
            sr.setTitle(title);
            if (searchTitle != null) {
                sr.setScore(getScore(title));
            } else {
                // basically a null search title means an exact lookup by id
                sr.setScore(1.0f);
            }

            sr.setId(mbid);
            sr.setUrl(provider.getDetailUrl(mbid));

            results.put(mbid, sr);
        }

        // add fanart images
        MediaArt ma = new MediaArt();
        if ("1".equals(getElementValue(item, "aid"))) {
            ma.setType(MediaArtifactType.BACKGROUND);
        } else if ("5".equals(getElementValue(item, "aid"))) {
            ma.setType(MediaArtifactType.POSTER);
        } else {
            ma.setType(MediaArtifactType.POSTER);
        }

        ma.setDownloadUrl(provider.getFanartDownloadUrl(getElementValue(item, "id")));
        sr.getArtwork().add(ma);
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
