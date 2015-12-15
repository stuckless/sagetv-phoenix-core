package sagex.phoenix.metadata.provider.xbmc;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.provider.imdb.IMDBUtils;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.HasFindByIMDBID;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;
import sagex.phoenix.scrapers.xbmc.XbmcMovieProcessor;
import sagex.phoenix.scrapers.xbmc.XbmcScraper;
import sagex.phoenix.scrapers.xbmc.XbmcScraperParser;
import sagex.phoenix.scrapers.xbmc.XbmcUrl;
import sagex.phoenix.util.DOMUtils;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.ParserUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XbmcMetadataProvider extends MetadataProvider implements HasFindByIMDBID {
    private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    private XbmcScraper scraper;

    public XbmcMetadataProvider(String providerXml) {
        XbmcScraperParser parser = new XbmcScraperParser();
        XbmcScraper scr;
        try {
            scr = parser.parseScraper(new File(providerXml));
        } catch (Exception e) {
            log.error("Failed to Load XBMC Scraper: " + providerXml);
            throw new RuntimeException("Failed to Load XBMC Scraper: " + providerXml, e);
        }

        init(scr);
    }

    public XbmcMetadataProvider(XbmcScraper scraper) {
        init(scraper);
    }

    private void init(XbmcScraper scraper) {
        this.scraper = scraper;

        MetadataProviderInfo info = new MetadataProviderInfo(scraper.getId(), scraper.getName(), scraper.getDescription(),
                scraper.getThumb(), null);
        setInfo(info);

        String content = scraper.getContent();
        if (!StringUtils.isEmpty(content)) {
            Pattern p = Pattern.compile("([^,]+)");
            Matcher m = p.matcher(content);
            while (m.find()) {
                String type = m.group(1).trim();
                log.debug("Provider:  " + scraper.getId() + "; content type: " + type);
                if ("movies".equalsIgnoreCase(type)) {
                    log.debug("Using Movies for Provider:  " + scraper.getId() + "; content type: " + type);
                    getInfo().getSupportedSearchTypes().add(MediaType.MOVIE);
                } else if ("tvshows".equalsIgnoreCase(type)) {
                    log.debug("Using TV for Provider:  " + scraper.getId() + "; content type: " + type);
                    getInfo().getSupportedSearchTypes().add(MediaType.TV);
                } else if ("music".equalsIgnoreCase(type)) {
                    getInfo().getSupportedSearchTypes().add(MediaType.MUSIC);
                } else {
                    log.debug("Unknown XBMC Scraper type: " + type + " for scraper: " + scraper);
                }
            }

        } else {
            log.warn("No Content Type for provider: " + scraper.getId());
        }
    }

    public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
        log.debug("Xbmc: getMetadata(): " + result);

        IMetadata md = MetadataProxy.newInstance();

        // TODO: Do we need provider id
        // md.setMediaProviderDataID(getInfo().getId());
        // updateMDValue(md, MetadataKey.METADATA_PROVIDER_ID,
        // getInfo().getId());

        md.setMediaProviderDataID(result.getId());

        try {
            XbmcMovieProcessor processor = new XbmcMovieProcessor(scraper);
            String xmlDetails = processor.getDetails(new XbmcUrl(result.getUrl()), result.getId());

            if (result.getMediaType() == MediaType.TV) {
                md.setMediaType(MediaType.TV.sageValue());
                processXmlContentForTV(xmlDetails, md, result);
            } else {
                processXmlContent(xmlDetails, md);
            }
        } catch (Exception e) {
            throw new MetadataException("XBMC: Failed to get metadata for result: " + result, result, e);
        }

        // try to parse an imdb id from the url
        if (!StringUtils.isEmpty(result.getUrl()) && StringUtils.isEmpty(md.getIMDBID())) {
            md.setIMDBID(IMDBUtils.parseIMDBID(result.getUrl()));
        }

        return md;
    }

    public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
        List<IMetadataSearchResult> l = new ArrayList<IMetadataSearchResult>();
        String arg = query.get(SearchQuery.Field.QUERY);

        // xbmc wants title and year separated, so let's do that
        String args[] = ParserUtils.parseTitle(arg);
        String title = args[0];
        String year = query.get(Field.YEAR);

        try {
            XbmcMovieProcessor processor = new XbmcMovieProcessor(scraper);
            XbmcUrl url = processor.getSearchUrl(title, year);
            String xmlString = processor.getSearchReulsts(url);

            log.debug("========= BEGIN XBMC Scraper Search Xml Results: Url: " + url);
            log.debug(xmlString);
            log.debug("========= End XBMC Scraper Search Xml Results: Url: " + url);

            Document xml = parseXmlString(xmlString);

            NodeList nl = xml.getElementsByTagName("entity");
            for (int i = 0; i < nl.getLength(); i++) {
                try {
                    Element el = (Element) nl.item(i);
                    NodeList titleList = el.getElementsByTagName("title");
                    String t = titleList.item(0).getTextContent();
                    NodeList urlList = el.getElementsByTagName("url");
                    XbmcUrl u = new XbmcUrl((Element) urlList.item(0));

                    MediaSearchResult sr = new MediaSearchResult();
                    String id = DOMUtils.getElementValue(el, "id");
                    sr.setId(id);
                    sr.setUrl(u.toExternalForm());
                    sr.setProviderId(getInfo().getId());
                    sr.getExtra().put("mediatype", query.getMediaType().name());

                    // populate extra args
                    MetadataSearchUtil.copySearchQueryToSearchResult(query, sr);

                    if (u.toExternalForm().indexOf("imdb") != -1) {
                        sr.addExtraArg("xbmcprovider", "imdb");
                        sr.addExtraArg("imdbid", id);
                    } else if (u.toExternalForm().indexOf("thetvdb.com") != -1) {
                        sr.addExtraArg("xbmcprovider", "tvdb");
                        sr.addExtraArg("tvdbid", id);
                    }

                    String v[] = ParserUtils.parseTitle(t);
                    sr.setTitle(v[0]);
                    sr.setYear(NumberUtils.toInt(v[1]));
                    sr.setScore(MetadataSearchUtil.calculateScore(arg, v[0]));
                    l.add(sr);
                } catch (Exception e) {
                    log.error("Error process an xml node!  Ignoring it from the search results.");
                }
            }
        } catch (Exception e) {
            throw new MetadataException("XBMC: Search Failed", query, e);
        }

        return l;
    }

    public IMetadata getMetadataForIMDBId(String imdbid) {
        if (getInfo().getId().contains("imdb")) {
            MediaSearchResult sr = new MediaSearchResult();
            sr.setIMDBId(imdbid);
            sr.setId(imdbid);
            sr.setUrl(IMDBUtils.createDetailUrl(imdbid));
            try {
                return getMetaData(sr);
            } catch (Exception e) {
                log.warn("Failed to search by IMDB URL: " + sr.getUrl(), e);
            }
        }
        return null;
    }

    private void processXmlContent(String xmlDetails, IMetadata md) throws Exception {
        if (xmlDetails == null || StringUtils.isEmpty(xmlDetails)) {
            log.warn("Cannot process empty Xml Contents.");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("******* BEGIN XML ***********");
            log.debug(xmlDetails);
            log.debug("******* END XML ***********");
        }

        Document xml = parseXmlString(xmlDetails);
        addMetadata(md, xml.getDocumentElement());
    }

    private void processXmlContentForTV(String xmlDetails, IMetadata md, IMetadataSearchResult result) throws Exception {
        log.debug("*** PROCESSING TV ***");
        if (xmlDetails == null || StringUtils.isEmpty(xmlDetails)) {
            log.warn("Cannot process empty Xml Contents.");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("******* BEGIN XML ***********");
            log.debug(xmlDetails);
            log.debug("******* END XML ***********");
        }

        Document xml = parseXmlString(xmlDetails);

        addMetadata(md, xml.getDocumentElement());

        log.debug("Fetching Episode Guide url");

        // now check for episode and guide url
        String episodeUrl = DOMUtils.getElementValue(xml.getDocumentElement(), "episodeguide");
        if (StringUtils.isEmpty(episodeUrl)) {
            log.error("No Episode Data!");
        } else {
            if (!StringUtils.isEmpty(result.getExtra().get(SearchQuery.Field.SEASON.name()))) {
                int findEpisode = NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.EPISODE.name()));
                int findSeason = NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.SEASON.name()));
                int findDisc = NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.DISC.name()));

                XbmcUrl url = new XbmcUrl(episodeUrl);
                // Call get Episode List
                XbmcMovieProcessor processor = new XbmcMovieProcessor(scraper);

                if (findEpisode > 0) {
                    String epListXml = processor.getEpisodeList(url);

                    log.debug("******** BEGIN EPISODE LIST XML ***********");
                    log.debug(epListXml);
                    log.debug("******** END EPISODE LIST XML ***********");

                    Document epListDoc = parseXmlString(epListXml);

                    NodeList nl = epListDoc.getElementsByTagName("episode");
                    int s = nl.getLength();
                    int season, ep;
                    String id = null;
                    String epUrl = null;
                    for (int i = 0; i < s; i++) {
                        Element el = (Element) nl.item(i);
                        season = DOMUtils.getElementIntValue(el, "season");
                        ep = DOMUtils.getElementIntValue(el, "epnum");
                        if (season == findSeason && ep == findEpisode) {
                            id = DOMUtils.getElementValue(el, "id");
                            epUrl = DOMUtils.getElementValue(el, "url");
                            break;
                        }
                    }

                    if (id == null) {
                        throw new Exception("Could Not Find Seaons and Episode for: " + findSeason + "x" + findEpisode);
                    }

                    log.debug("We have an episdoe id for season and episode... fetching details...");

                    processor = new XbmcMovieProcessor(scraper);
                    xmlDetails = processor.getEpisodeDetails(new XbmcUrl(epUrl), id);

                    log.debug("******** BEGIN EPISODE DETAILS XML ***********");
                    log.debug(xmlDetails);
                    log.debug("******** END EPISODE DETAILS XML ***********");

                    // update again, using the episode specific data
                    xml = parseXmlString(xmlDetails);
                    Element el = xml.getDocumentElement();
                    addMetadata(md, el);

                    // add/update tv specific stuff
                    String plot = DOMUtils.getElementValue(el, "plot");
                    if (!StringUtils.isEmpty(plot)) {
                        md.setDescription(plot);
                    }

                    md.setEpisodeNumber(findEpisode);
                    md.setOriginalAirDate(DateUtils.parseDate(DOMUtils.getElementValue(el, "aired")));
                    md.setEpisodeName(DOMUtils.getElementValue(el, "title"));
                } else if (findDisc > 0) {
                    md.setDiscNumber(findDisc);
                }

                if (findSeason > 0) {
                    md.setSeasonNumber(findSeason);
                }
            }
        }

    }

    private void addMetadata(IMetadata md, Element details) {
        log.debug("Processing <details> node....");
        NodeList nl = details.getElementsByTagName("fanart");
        for (int i = 0; i < nl.getLength(); i++) {
            Element fanart = (Element) nl.item(i);
            String url = fanart.getAttribute("url");
            NodeList thumbs = fanart.getElementsByTagName("thumb");
            if (thumbs != null && thumbs.getLength() > 0) {
                processMediaArt(md, MediaArtifactType.BACKGROUND, "Backgrounds", thumbs, url);
            } else {
                if (!StringUtils.isEmpty(url)) {
                    processMediaArt(md, MediaArtifactType.BACKGROUND, "Background", url);
                }
            }
        }

        nl = details.getElementsByTagName("thumbs");
        for (int i = 0; i < nl.getLength(); i++) {
            Element fanart = (Element) nl.item(i);
            processMediaArt(md, MediaArtifactType.POSTER, "Poster", fanart.getElementsByTagName("thumb"), null);
        }

        nl = details.getElementsByTagName("actor");
        for (int i = 0; i < nl.getLength(); i++) {
            Element actor = (Element) nl.item(i);
            CastMember cm = new CastMember(DOMUtils.getElementValue(actor, "name"), DOMUtils.getElementValue(actor, "role"));
            md.getActors().add(cm);
        }

        nl = details.getElementsByTagName("director");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            CastMember cm = new CastMember();
            cm.setName(StringUtils.trim(el.getTextContent()));
            md.getDirectors().add(cm);
        }

        nl = details.getElementsByTagName("credits");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            CastMember cm = new CastMember();
            cm.setName(StringUtils.trim(el.getTextContent()));
            md.getWriters().add(cm);
        }

        nl = details.getElementsByTagName("genre");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            md.getGenres().add(StringUtils.trim(el.getTextContent()));
        }

        // updateMDValue(md, MetadataKey.COMPANY,
        // DOMUtils.getElementValue(details, "studio"));
        md.setDescription(DOMUtils.getMaxElementValue(details, "plot"));
        md.setRated(MetadataSearchUtil.parseMPAARating(DOMUtils.getElementValue(details, "mpaa")));
        // updateMDValue(md, MetadataKey.MPAA_RATING_DESCRIPTION,
        // DOMUtils.getElementValue(details, "mpaa"));

        md.setOriginalAirDate(DateUtils.parseDate(DOMUtils.getElementValue(details, "year")));
        md.setRunningTime(MetadataSearchUtil.parseRunningTime(DOMUtils.getElementValue(details, "runtime"),
                MetadataSearchUtil.IMDB_RUNNING_TIME_REGEX));
        md.setEpisodeName(DOMUtils.getElementValue(details, "title"));
        md.setRated(DOMUtils.getElementValue(details, "rating"));
        md.setYear(NumberUtils.toInt(DOMUtils.getElementValue(details, "year")));
    }

    private void processMediaArt(IMetadata md, MediaArtifactType type, String label, NodeList els, String baseUrl) {
        for (int i = 0; i < els.getLength(); i++) {
            Element e = (Element) els.item(i);
            String image = e.getTextContent();
            if (image != null)
                image = image.trim();
            if (baseUrl != null) {
                baseUrl = baseUrl.trim();
                image = baseUrl + image;
            }
            processMediaArt(md, type, label, image);
        }
    }

    private void processMediaArt(IMetadata md, MediaArtifactType type, String label, String image) {
        MediaArt ma = new MediaArt();
        ma.setDownloadUrl(image);
        ma.setType(type);
        md.getFanart().add(ma);
    }

    /**
     * added because some xml strings are not parsable using utf-8
     *
     * @param xml
     * @return
     * @throws Exception
     */
    private Document parseXmlString(String xml) throws Exception {
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document doc = null;
        for (String charset : new String[]{"UTF-8", "ISO-8859-1", "US-ASCII"}) {
            try {
                doc = parser.parse(new ByteArrayInputStream(xml.getBytes(charset)));
                break;
            } catch (Throwable t) {
                log.error("Failed to parse xml using charset: " + charset, t);
            }
        }

        if (doc == null) {
            log.error("Unabled to parse xml string");
            log.error(xml);
            throw new Exception("Unable to parse xml!");
        }

        return doc;
    }
}
