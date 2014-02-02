package sagex.phoenix.metadata.provider.dvdprofiler;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.StringUtils;

public class DVDProfilerParser {
	private static final Logger log = Logger.getLogger(DVDProfilerParser.class);

	private String id = null;

	private DVDProfilerMetadataProvider provider = null;
	private DVDProfilerXmlFile dvdFile = null;
	private Element node = null;

	private IMetadata metadata;

	public DVDProfilerParser(String id, DVDProfilerMetadataProvider provider) throws MetadataException {
		this.id = id;
		this.provider = provider;
		this.dvdFile = provider.getDvdProfilerXmlFile();
		this.node = dvdFile.findMovieById(id);
	}

	public IMetadata getMetaData() {
		metadata = MetadataProxy.newInstance();

		// we only support movies
		metadata.setMediaType(MediaType.MOVIE.sageValue());

		addCastMembers(metadata);
		// metadata.setAspectRatio(getAspectRatio());
		// metadata.setCompany(getCompany());
		addGenres();
		// metadata.setParentalRating(getMPAARating());
		metadata.setRated(getMPAARating());
		metadata.setDescription(getPlot());
		metadata.setOriginalAirDate(DateUtils.parseDate(getReleaseDate()));
		metadata.setRunningTime(getRuntime());

		IMediaArt ma = getMediaArtImage("f");
		if (ma != null) {
			metadata.getFanart().add(ma);
		}

		// if use back cover is enabled
		// ma = getMediaArtImage("b");
		// if (ma != null) {
		// metadata.addMediaArt(ma);
		// }

		setTitle(metadata, getTitle());

		// metadata.setUserRating(MetadataUtil.parseUserRating(getUserRating()));
		metadata.setYear(getYear());

		metadata.setMediaProviderID(provider.getInfo().getId());
		metadata.setMediaProviderDataID(id);
		return metadata;
	}

	public void setTitle(IMetadata md, String title) {
		if (title == null)
			return;
		title = title.trim();
		title = StringUtils.unquote(title);
		md.setEpisodeName(title);
		md.setMediaTitle(title);
	}

	private void addCastMembers(IMetadata md) {
		addActors(md);

		// add in others
		@SuppressWarnings("unchecked")
		List nl = node.element("Credits").elements("Credit");
		for (int i = 0; i < nl.size(); i++) {
			Element e = (Element) nl.get(i);
			String credType = e.attributeValue("CreditType");
			CastMember cm = new CastMember();
			cm.setName(String.format("%s %s", e.attributeValue("FirstName"), e.attributeValue("LastName")));
			if ("Direction".equals(credType)) {
				md.getDirectors().add(cm);
			} else if ("Writing".equals(credType)) {
				md.getWriters().add(cm);
			} else {
				md.getGuests().add(cm);
			}
		}
	}

	public void addActors(IMetadata md) {
		@SuppressWarnings("unchecked")
		List nl = node.element("Actors").elements("Actor");
		for (int i = 0; i < nl.size(); i++) {
			Element e = (Element) nl.get(i);
			CastMember cm = new CastMember();
			cm.setName(String.format("%s %s", e.attributeValue("FirstName"), e.attributeValue("LastName")));
			cm.setRole(e.attributeValue("Role"));
			md.getActors().add(cm);
			log.debug("Adding Actor: " + cm);
		}
	}

	public String getAspectRatio() {
		return DVDProfilerXmlFile.getElementValue(node, "FormatAspectRatio");
	}

	public String getCompany() {
		return DVDProfilerXmlFile.getElementValue(node, "Studio");
	}

	public void addGenres() {
		@SuppressWarnings("unchecked")
		List nl = node.element("Genres").elements("Genre");
		for (int i = 0; i < nl.size(); i++) {
			Element e = (Element) nl.get(i);
			metadata.getGenres().add(e.getTextTrim());
		}
	}

	public String getMPAARating() {
		return DVDProfilerXmlFile.getElementValue(node, "Rating");
	}

	public String getPlot() {
		return StringUtils.removeHtml(DVDProfilerXmlFile.getElementValue(node, "Overview"));
	}

	public String getReleaseDate() {
		return DVDProfilerXmlFile.getElementValue(node, "Released");
	}

	public long getRuntime() {
		return MetadataSearchUtil.convertTimeToMillissecondsForSage(DVDProfilerXmlFile.getElementValue(node, "RunningTime"));
	}

	public IMediaArt getMediaArtImage(String type) {
		if (provider.getImagesDir() == null || !provider.getImagesDir().isDirectory())
			return null;

		File f = new File(provider.getImagesDir(), id + type + "." + ImageUtil.EXT_JPG);
		if (!f.exists()) {
			f = new File(provider.getImagesDir(), id + type + "." + ImageUtil.EXT_PNG);
		}

		if (!f.exists()) {
			log.warn("Missing Cover for Movie: " + id + "; " + id + "(" + getTitle() + ")");
			return null;
		}

		try {
			String uri = f.toURI().toURL().toExternalForm();
			MediaArt ma = new MediaArt();
			ma.setDownloadUrl(uri);
			ma.setType(MediaArtifactType.POSTER);
			return ma;
		} catch (MalformedURLException e) {
			log.error("Failed to create url for thumbnail on movie: " + id + ": " + id + "(" + getTitle() + ")");
		}

		return null;
	}

	public String getTitle() {
		return StringUtils.removeHtml(DVDProfilerXmlFile.getElementValue(node, "Title"));
	}

	public int getYear() {
		return DateUtils.parseYear(DVDProfilerXmlFile.getElementValue(node, "Released"));
	}
}
