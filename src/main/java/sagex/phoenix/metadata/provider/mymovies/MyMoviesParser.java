package sagex.phoenix.metadata.provider.mymovies;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.DOMUtils;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.StringUtils;

public class MyMoviesParser {
    private static final Logger      log         = Logger.getLogger(MyMoviesParser.class);

    private String                   id          = null;

    private MyMoviesMetadataProvider provider    = null;
    private MyMoviesXmlFile          movieFile   = null;
    private Element                  node        = null;

    private IMetadata            metadata;

    public MyMoviesParser(String id, MyMoviesMetadataProvider provider) throws MetadataException {
        this.id = id;
        this.provider = provider;
        this.movieFile = this.provider.getMyMoviesXmlFile();
        this.node = movieFile.findMovieById(id);
    }

    public IMetadata getMetaData() {
        metadata = MetadataProxy.newInstance();
        metadata.setMediaType(MediaType.MOVIE.sageValue());

        addCastMembers(metadata);
        addGenres(metadata);
        updateMPAARating(metadata);
        
        metadata.setDescription(getPlot());
        metadata.setOriginalAirDate(getReleaseDate());
        metadata.setRunningTime(getRuntime());

        IMediaArt ma = getMediaArtImage("f");
        if (ma != null) {
            metadata.getFanart().add(ma);
        }

        setTitle(metadata, getTitle());
        metadata.setYear(getYear());

        metadata.setMediaProviderID(MyMoviesMetadataProvider.PROVIDER_ID);
        metadata.setMediaProviderDataID(id);
        metadata.setIMDBID(MyMoviesXmlFile.getElementValue(node, "IMDB"));
        
        return metadata;
    }

    public void setTitle(IMetadata md, String title) {
    	if (title==null) return;
    	title=title.trim();
    	title = StringUtils.unquote(title);    	
    	md.setEpisodeName(title);
    	md.setMediaTitle(title);
    }

	private void addCastMembers(IMetadata md) {
        // add in others
        NodeList nl = node.getElementsByTagName("Person");
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            String credType = DOMUtils.getElementValue(e, "Type");
            CastMember cm = new CastMember();
            cm.setName(DOMUtils.getElementValue(e, "Name"));
            cm.setRole(DOMUtils.getElementValue(e, "Role"));
            if ("Director".equals(credType)) {
            	md.getDirectors().add(cm);
            } else if ("Writer".equals(credType)) {
                md.getWriters().add(cm);
            } else if ("Actor".equals(credType)) {
            	md.getActors().add(cm);
            } else {
            	md.getGuests().add(cm);
            }
        }
    }

    public String getAspectRatio() {
        return MyMoviesXmlFile.getElementValue(node, "AspectRatio");
    }

    public String getCompany() {
        return MyMoviesXmlFile.getElementValue(node, "Studio");
    }

    public void addGenres(IMetadata md) {
        NodeList nl = node.getElementsByTagName("Genre");
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            md.getGenres().add(e.getTextContent());
        }
    }

    public void updateMPAARating(IMetadata md) {
        NodeList nl = node.getElementsByTagName("ParentalRating");
        Element el = null;
        if (nl.getLength() > 0) {
            el = (Element) nl.item(0);
        }

        md.setRated(covertMyMoviesRating(DOMUtils.getElementValue(el, "Value")));
        md.setExtendedRatings(DOMUtils.getElementValue(el, "Description"));
    }

    private String covertMyMoviesRating(String rating) {
    	if ("3".equals(rating)) return "PG";
    	if ("4".equals(rating)) return "PG-13";
    	if ("6".equals(rating)) return "R";
    	log.warn("Unknown MyMovies Rating # " + rating);
    	return null;
	}

	public String getPlot() {
        return StringUtils.removeHtml(MyMoviesXmlFile.getElementValue(node, "Description"));
    }

    public String getProviderUrl() {
        return id;
    }

    public String getProviderId() {
        return MyMoviesMetadataProvider.PROVIDER_ID;
    }

    public Date getReleaseDate() {
        return DateUtils.parseDate(MyMoviesXmlFile.getElementValue(node, "ReleaseDate"));
    }

	public long getRuntime() {
        return MetadataSearchUtil.convertTimeToMillissecondsForSage(MyMoviesXmlFile.getElementValue(node, "RunningTime"));
    }

    public IMediaArt getMediaArtImage(String type) {
        Element el = DOMUtils.getElementByTagName(node, "Covers");
        if (el != null) {
            try {
                File f = new File(DOMUtils.getElementValue(el, "Front"));
                String uri = f.toURI().toURL().toExternalForm();
                MediaArt ma = new MediaArt();
                ma.setDownloadUrl(uri);
                ma.setType(MediaArtifactType.POSTER);
                return ma;
            } catch (MalformedURLException e) {
                log.error("Failed to create url for thumbnail on movie: " + id + "; " + getTitle());
            }
        }
        return null;
    }

    public String getTitle() {
        return StringUtils.removeHtml(MyMoviesXmlFile.getElementValue(node, "LocalTitle"));
    }

    public int getYear() {
        return DateUtils.parseYear(MyMoviesXmlFile.getElementValue(node, "ReleaseDate"), "MM/dd/yyyy");
    }
}
