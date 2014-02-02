package sagex.phoenix.metadata.provider.tmdb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.url.IUrl;
import sagex.phoenix.util.url.UrlFactory;

public class TheMovieDBItemParser {
	private static final Logger log = Logger.getLogger(TheMovieDBItemParser.class);

	private IUrl url;
	private IMetadata md = null;
	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	private String theMovieDBID;
	private TheMovieDBMetadataProvider provider;

	public TheMovieDBItemParser(String providerDataUrl, TheMovieDBMetadataProvider provider) {
		this.url = UrlFactory.newUrl(providerDataUrl);
		this.provider = provider;
		log.info("Fetching TMDB Metadata: " + providerDataUrl);
	}

	public IMetadata getMetadata() {
		if (md == null) {
			try {
				// parse and fill
				DocumentBuilder parser = factory.newDocumentBuilder();
				log.info("Parsing TheMovieDB url: " + url);
				Document doc = parser.parse(url.getInputStream(null, true));

				md = MetadataProxy.newInstance();

				NodeList nl = doc.getElementsByTagName("movie");
				if (nl == null || nl.getLength() == 0) {
					throw new Exception("Movie Node not found!");
				}

				if (nl.getLength() > 1) {
					log.warn("Found more than 1 movie node.  Using the first.");
				}

				Element movie = (Element) nl.item(0);
				theMovieDBID = getElementValue(movie, "id");

				MediaType mt = MediaType.toMediaType(getElementValue(movie, "type"));
				if (mt == null) {
					mt = MediaType.MOVIE;
				}

				md.setMediaType(mt.sageValue());
				addPeople(md, movie);
				md.getGenres().addAll(getGenres(movie));
				md.setDescription(getElementValue(movie, "overview"));
				md.setOriginalAirDate(DateUtils.parseDate(getElementValue(movie, "released")));
				md.setRunningTime(MetadataSearchUtil.convertTimeToMillissecondsForSage(getElementValue(movie, "runtime")));
				addFanart(md, movie);
				md.setMediaTitle(sagex.phoenix.util.StringUtils.unquote(getElementValue(movie, "name")));
				md.setEpisodeName(md.getMediaTitle());
				if (StringUtils.isEmpty(md.getMediaTitle())) {
					log.warn("The MovieDB Result didn't contain a title. Url: " + url + TheMovieDBMetadataProvider.getApiKey());
					return null;
				}
				md.setRated(getElementValue(movie, "certification"));

				md.setUserRating(MetadataSearchUtil.parseUserRating(getElementValue(movie, "rating")));
				md.setYear(DateUtils.parseYear(getElementValue(movie, "released")));
				md.setMediaProviderID(provider.getInfo().getId());
				md.setMediaProviderDataID(theMovieDBID);
				md.setIMDBID(getElementValue(movie, "imdb_id"));
			} catch (Exception e) {
				log.error("Failed while parsing: " + url, e);
				md = null;
			}
		}
		return md;
	}

	private void addPeople(IMetadata md, Element movie) {
		NodeList nl = movie.getElementsByTagName("person");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			CastMember cm = new CastMember();
			cm.setName(e.getAttribute("name"));
			String job = e.getAttribute("job");

			if ("director".equalsIgnoreCase(job)) {
				md.getDirectors().add(cm);
			} else if ("writer".equalsIgnoreCase(job) || "screenplay".equalsIgnoreCase(job)) {
				md.getWriters().add(cm);
			} else if ("actor".equalsIgnoreCase(job)) {
				cm.setRole(e.getAttribute("character"));
				md.getActors().add(cm);
			} else {
				cm.setRole(job);
				md.getGuests().add(cm);
			}
		}
	}

	private List<String> getGenres(Element movie) {
		List<String> l = new ArrayList<String>();

		NodeList nl = movie.getElementsByTagName("category");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			if ("genre".equalsIgnoreCase(e.getAttribute("type"))) {
				l.add(e.getAttribute("name"));
			}
		}
		return l;
	}

	private void addFanart(IMetadata md, Element movie) {
		NodeList nl = movie.getElementsByTagName("image");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);

			String size = e.getAttribute("size");
			if ("original".equalsIgnoreCase(size)) {
				MediaArt ma = new MediaArt();
				ma.setDownloadUrl(e.getAttribute("url"));

				String type = e.getAttribute("type");
				if ("poster".equalsIgnoreCase(type)) {
					ma.setType(MediaArtifactType.POSTER);
				} else if ("backdrop".equalsIgnoreCase(type)) {
					ma.setType(MediaArtifactType.BACKGROUND);
				} else if ("banner".equalsIgnoreCase(type)) {
					ma.setType(MediaArtifactType.BANNER);
				}

				md.getFanart().add(ma);
			}
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

	public String getTheMovieDBID() {
		return theMovieDBID;
	}
}
