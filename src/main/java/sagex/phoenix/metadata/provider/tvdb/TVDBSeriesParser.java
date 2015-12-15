package sagex.phoenix.metadata.provider.tvdb;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.url.IUrl;
import sagex.phoenix.util.url.UrlFactory;

import java.text.MessageFormat;
import java.util.List;

public class TVDBSeriesParser {
    private static final Logger log = Logger.getLogger(TVDBSeriesParser.class);

    public static final String SERIES_URL = "http://www.thetvdb.com/api/{0}/series/{1}/{2}.xml";
    public static final String ACTORS_URL = "http://www.thetvdb.com/api/{0}/series/{1}/actors.xml";

    private TVDBConfiguration config = null;

    private String seriesId = null;

    public TVDBSeriesParser(String seriesId) {
        this.seriesId = seriesId;

        if (seriesId == null) {
            throw new RuntimeException("Can't get series info without a Series Id, and series id was null.");
        }

        config = GroupProxy.get(TVDBConfiguration.class);
    }

    private String getValue(Element e, String node) {
        Node n = e.element(node);
        ;
        if (n == null)
            return null;
        return n.getText();
    }

    // private void addSeriesInfo(IMetadata md) throws Exception {
    // DocumentBuilder parser = factory.newDocumentBuilder();
    // String seriesUrl = MessageFormat.format(SERIES_URL,
    // TVDBMetadataProvider.getApiKey(), result.getId(), config.getLanguage());
    // log.info("TVDB Series: " + seriesUrl);
    //
    // IUrl url = UrlFactory.newUrl(seriesUrl);
    // Document doc = parser.parse(url.getInputStream(null, true));
    //
    // Element series = DOMUtils.getElementByTagName(doc.getDocumentElement(),
    // "Series");
    // md.setRated(DOMUtils.getElementValue(series, "ContentRating"));
    // md.setOriginalAirDate(DateUtils.parseDate(DOMUtils.getElementValue(series,
    // "FirstAired")));
    // md.setYear(DateUtils.parseYear(DOMUtils.getElementValue(series,
    // "FirstAired")));
    //
    // String genres = DOMUtils.getElementValue(series, "Genre");
    // if (!StringUtils.isEmpty(genres)) {
    // for (String g : genres.split("[,\\|]")) {
    // if (!StringUtils.isEmpty(g)) {
    // md.getGenres().add(g.trim());
    // }
    // }
    // }
    //
    // md.setDescription(DOMUtils.getElementValue(series, "Overview"));
    // md.setUserRating(MetadataSearchUtil.parseUserRating(DOMUtils.getElementValue(series,
    // "Rating")));
    // md.setRunningTime(MetadataSearchUtil.convertTimeToMillissecondsForSage(DOMUtils.getElementValue(series,
    // "Runtime")));
    // // fix title, unquote, and then parse the title if it's Title (year)
    // Pair<String, String> pair =
    // ParserUtils.parseTitleAndDateInBrackets(sagex.phoenix.util.StringUtils.unquote(DOMUtils.getElementValue(series,
    // "SeriesName")));
    // md.setMediaTitle(pair.first());
    //
    // // TV has the show title in the relative path field
    // md.setRelativePathWithTitle(pair.first());
    // md.setRated(DOMUtils.getElementValue(series, "ContentRating"));
    // }

    public ISeriesInfo getSeriesInfo() throws MetadataException {
        String seriesUrl = MessageFormat.format(SERIES_URL, TVDBMetadataProvider.getApiKey(), seriesId, config.getLanguage());
        log.info("TVDB Series: " + seriesUrl);

        try {

            IUrl url = UrlFactory.newUrl(seriesUrl);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(url.getInputStream(null, true));

            ISeriesInfo sinfo = MetadataProxy.newInstance(ISeriesInfo.class);

            Element series = document.getRootElement().element("Series");
            sinfo.setContentRating(MetadataUtil.fixContentRating(MediaType.TV, getValue(series, "ContentRating")));
            sinfo.setPremiereDate(getValue(series, "FirstAired"));

            String genres = getValue(series, "Genre");
            if (!StringUtils.isEmpty(genres)) {
                for (String g : genres.split("[,\\|]")) {
                    if (!StringUtils.isEmpty(g)) {
                        sinfo.getGenres().add(g.trim());
                    }
                }
            }

            sinfo.setDescription(getValue(series, "Overview"));
            sinfo.setUserRating(MetadataSearchUtil.parseUserRating(getValue(series, "Rating")));
            sinfo.setAirDOW(getValue(series, "Airs_DayOfWeek"));
            sinfo.setAirHrMin(getValue(series, "Airs_Time"));
            // sinfo.setFinaleDate(date);
            // sinfo.setHistory();
            sinfo.setImage(TVDBMetadataProvider.getFanartURL(getValue(series, "banner")));
            sinfo.setNetwork(getValue(series, "Network"));
            sinfo.setTitle(getValue(series, "SeriesName"));
            sinfo.setZap2ItID(getValue(series, "zap2it_id"));

            // external information for lookup later, if needed
            sinfo.setRuntime(MetadataSearchUtil.convertTimeToMillissecondsForSage(getValue(series, "Runtime")));

            // actors
            addActors(sinfo);

            return sinfo;
        } catch (Exception e) {
            throw new MetadataException("Failed to get series for " + seriesUrl, e);
        }
    }

    private void addActors(ISeriesInfo info) {
        try {
            String seriesUrl = MessageFormat.format(ACTORS_URL, TVDBMetadataProvider.getApiKey(), seriesId);
            log.info("TVDB Actors: " + seriesUrl);

            IUrl url = UrlFactory.newUrl(seriesUrl);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(url.getInputStream(null, true));

            List<?> nodes = document.getRootElement().elements("Actor");
            if (nodes != null) {
                for (Object o : nodes) {
                    CastMember cm = new CastMember();
                    Element e = (Element) o;
                    cm.setName(getValue(e, "Name"));
                    cm.setRole(getValue(e, "Role"));
                    cm.setImage(TVDBMetadataProvider.getFanartURL(getValue(e, "Image")));
                    info.getCast().add(cm);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to process the Actors for series: " + seriesId, e);
        }
    }

}
