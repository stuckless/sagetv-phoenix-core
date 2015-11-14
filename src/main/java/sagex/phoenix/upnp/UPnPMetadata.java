package sagex.phoenix.upnp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.DIDLObject.Property;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Item;

import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.metadata.IMediaArt;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.util.DateUtils;

public class UPnPMetadata implements IMetadata {
	private UPnPMediaFile file;
	private Item item;
	private Res resource;

	public UPnPMetadata(UPnPMediaFile file) {
		this.file = file;
		this.item = ((Item) file.getMediaObject());
		if (item.getResources() != null && item.getResources().size() > 0) {
			resource = item.getResources().get(0);
		}
	}

	public <V> String prop(java.lang.Class<? extends Property<V>> propertyClass) {
		if (item != null) {
			Property<V> p = item.getFirstProperty(propertyClass);
			if (p != null) {
				return (String) p.getValue();
			}
		}
		return null;
	}

	public <V> int propInt(java.lang.Class<? extends Property<V>> propertyClass) {
		if (item != null) {
			Property<V> p = item.getFirstProperty(propertyClass);
			if (p != null) {
				return (Integer) p.getValue();
			}
		}
		return 0;
	}

	@Override
	public int getWidth() {
		return resource.getResolutionX();
	}

	@Override
	public int getHeight() {
		return resource.getResolutionY();
	}

	@Override
	public int getTrack() {
		return propInt(DIDLObject.Property.UPNP.ORIGINAL_TRACK_NUMBER.class);
	}

	@Override
	public int getTotalTracks() {
		return 0;
	}

	@Override
	public String getComment() {
		return prop(DIDLObject.Property.DC.DESCRIPTION.class);
	}

	@Override
	public Date getAiringTime() {
		return DateUtils.parseDate(prop(DIDLObject.Property.DC.DATE.class));
	}

	@Override
	public int getThumbnailOffset() {
		return 0;
	}

	@Override
	public int getThumbnailSize() {
		return 0;
	}

	@Override
	public String getThumbnailDesc() {
		return null;
	}

	@Override
	public long getDuration() {
		return DateUtils.parseDuration(resource.getDuration());
	}

	@Override
	public String getPictureResolution() {
		return resource.getResolution();
	}

	@Override
	public boolean isSet(SageProperty key) {
		return true;
	}

	@Override
	public void clear(SageProperty key) {
		// does nothing
	}

	@Override
	public String get(SageProperty key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(SageProperty key, String value) {
		// does nothing
	}

	@Override
	public String getRelativePathWithTitle() {
		return item.getTitle();
	}

	@Override
	public void setRelativePathWithTitle(String title) {
		// does nothing
	}

	@Override
	public String getEpisodeName() {
		return item.getTitle();
	}

	@Override
	public void setEpisodeName(String name) {
	}

	@Override
	public String getAlbum() {
		return prop(DIDLObject.Property.UPNP.ALBUM.class);
	}

	@Override
	public void setAlbum(String album) {
	}

	@Override
	public List<String> getGenres() {
		ArrayList<String> l = new ArrayList<String>();
		l.add(prop(DIDLObject.Property.UPNP.GENRE.class));
		return l;
	}

	@Override
	public String getGenreID() {
		return prop(DIDLObject.Property.UPNP.GENRE.class);
	}

	@Override
	public void setGenreID(String genreId) {
	}

	@Override
	public String getDescription() {
		return prop(DIDLObject.Property.DC.DESCRIPTION.class);
	}

	@Override
	public void setDescription(String desc) {
	}

	@Override
	public int getYear() {
		return DateUtils.parseYear(prop(DIDLObject.Property.DC.DATE.class));
	}

	@Override
	public void setYear(int year) {
	}

	@Override
	public String getLanguage() {
		return prop(DIDLObject.Property.DC.LANGUAGE.class);
	}

	@Override
	public void setLanguage(String lang) {
	}

	@Override
	public String getRated() {
		return prop(DIDLObject.Property.UPNP.RATING.class);
	}

	@Override
	public void setRated(String rated) {
	}

	@Override
	public String getParentalRating() {
		return null;
	}

	@Override
	public void setParentalRating(String rated) {
	}

	@Override
	public long getRunningTime() {
		return getDuration();
	}

	@Override
	public void setRunningTime(long time) {
	}

	@Override
	public Date getOriginalAirDate() {
		return getAiringTime();
	}

	@Override
	public void setOriginalAirDate(Date date) {
	}

	@Override
	public String getExtendedRatings() {
		return null;
	}

	@Override
	public void setExtendedRatings(String ratings) {
	}

	@Override
	public String getMisc() {
		return ("size=" + resource.getSize());
	}

	@Override
	public void setMisc(String misc) {
	}

	@Override
	public int getPartNumber() {
		return 0;
	}

	@Override
	public void setPartNumber(int part) {
	}

	@Override
	public int getTotalParts() {
		return 1;
	}

	@Override
	public void setTotalParts(int parts) {
	}

	@Override
	public boolean isHDTV() {
		return (getWidth() >= 720);
	}

	@Override
	public void setHDTV(boolean hdtv) {
	}

	@Override
	public boolean isCC() {
		return false;
	}

	@Override
	public void setCC(boolean cc) {
	}

	@Override
	public boolean getStereo() {
		return resource.getNrAudioChannels() > 1;
	}

	@Override
	public void setStereo(boolean stereo) {
	}

	@Override
	public boolean isSubtitled() {
		return false;
	}

	@Override
	public void setSubtitled(boolean subs) {
	}

	@Override
	public boolean getPremiere() {
		return false;
	}

	@Override
	public void setPremiere(boolean prem) {
	}

	@Override
	public boolean isSeasonPremiere() {
		return false;
	}

	@Override
	public void setSeasonPremiere(boolean prem) {
	}

	@Override
	public boolean isSeriesPremiere() {
		return false;
	}

	@Override
	public void setSeriesPremiere(boolean prem) {
	}

	@Override
	public boolean isChannelPremiere() {
		return false;
	}

	@Override
	public void setChannelPremiere(boolean prem) {
	}

	@Override
	public boolean isSeasonFinal() {
		return false;
	}

	@Override
	public void setSeasonFinal(boolean fin) {
	}

	@Override
	public boolean isSeriesFinale() {
		return false;
	}

	@Override
	public void setSeriesFinale(boolean fin) {
	}

	@Override
	public boolean isSAP() {
		return false;
	}

	@Override
	public void setSAP(boolean sap) {

	}

	@Override
	public String getExternalID() {
		return item.getId();
	}

	@Override
	public void setExternalID(String id) {
	}

	@Override
	public String getFormatVideoHeight() {
		return String.valueOf(getHeight());
	}

	@Override
	public String getFormatVideoWidth() {
		return String.valueOf(getWidth());
	}

	@Override
	public String getFormatVideoFPS() {
		return null;
	}

	@Override
	public String getFormatVideoInterlaced() {
		return null;
	}

	@Override
	public String getFormatVideoProgressive() {
		return null;
	}

	@Override
	public int getFormatAudioNumStreams() {
		return resource.getNrAudioChannels().intValue();
	}

	@Override
	public String getFormatAudioStreamNumProperty(int streamNum, String prop) {
		return null;
	}

	@Override
	public int FormatSubtitleNumStreams() {
		return 0;
	}

	@Override
	public String getFormatSubtitleStreamNumPropery() {
		return null;
	}

	@Override
	public String getFormatSubtitleCodec() {
		return null;
	}

	@Override
	public String getFormatContainer() {
		return resource.getProtocolInfo().getContentFormatMimeType().getType();
	}

	@Override
	public String getFormatVideoCodec() {
		return resource.getProtocolInfo().getContentFormatMimeType().getType();
	}

	@Override
	public String getFormatVideoResolution() {
		return resource.getResolution();
	}

	@Override
	public String getFormatVideoAspect() {
		return null;
	}

	@Override
	public String getFormatAudioCodec() {
		return null;
	}

	@Override
	public String FormatAudioChannels() {
		return String.valueOf(resource.getNrAudioChannels());
	}

	@Override
	public String getFormatAudioLanguage() {
		return null;
	}

	@Override
	public String getFormatAudioSampleRate() {
		return null;
	}

	@Override
	public String getFormatAudioBitsPerSample() {
		return null;
	}

	@Override
	public String getFormatAudioBitrateKbps() {
		return null;
	}

	@Override
	public String getFormatSubtitleLanguage() {
		return null;
	}

	@Override
	public String getFormatVideoBitrateMbps() {
		return null;
	}

	@Override
	public List<ICastMember> getActors() {
		List<ICastMember> cast = new ArrayList<ICastMember>();
		Property[] props = item.getProperties(DIDLObject.Property.UPNP.ACTOR.class);
		if (props != null) {
			for (Property p : props) {
				if (p.getValue() != null) {
					cast.add(new CastMember(p.getValue().toString(), "Actor"));
				}
			}
		}
		return cast;
	}

	@Override
	public List<ICastMember> getLeadActors() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getSupportingActors() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getActresses() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getLeadActresses() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getSupportingActresses() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getGuests() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getGuestStars() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getDirectors() {
		List<ICastMember> cast = new ArrayList<ICastMember>();
		Property[] props = item.getProperties(DIDLObject.Property.UPNP.DIRECTOR.class);
		if (props != null) {
			for (Property p : props) {
				if (p.getValue() != null) {
					cast.add(new CastMember(p.getValue().toString(), "Director"));
				}
			}
		}
		return cast;
	}

	@Override
	public List<ICastMember> getProducers() {
		List<ICastMember> cast = new ArrayList<ICastMember>();
		Property[] props = item.getProperties(DIDLObject.Property.UPNP.PRODUCER.class);
		if (props != null) {
			for (Property p : props) {
				if (p.getValue() != null) {
					cast.add(new CastMember(p.getValue().toString(), "Producer"));
				}
			}
		}
		return cast;
	}

	@Override
	public List<ICastMember> getWriters() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getChoreographers() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getSportsFigures() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getCoaches() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getHosts() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getExecutiveProducers() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getArtists() {
		List<ICastMember> cast = new ArrayList<ICastMember>();
		Property[] props = item.getProperties(DIDLObject.Property.UPNP.ARTIST.class);
		if (props != null) {
			for (Property p : props) {
				if (p.getValue() != null) {
					cast.add(new CastMember(p.getValue().toString(), "Artist"));
				}
			}
		}
		return cast;
	}

	@Override
	public List<ICastMember> getAlbumArtists() {
		return getArtists();
	}

	@Override
	public List<ICastMember> getComposers() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getJudges() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getNarrators() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getContestants() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<ICastMember> getCorrespondents() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getMediaTitle() {
		return item.getTitle();
	}

	@Override
	public void setMediaTitle(String title) {
	}

	@Override
	public String getMediaType() {
		String cl = item.getClazz().getValue();
		if (cl == null || cl.contains("video"))
			return MediaType.MOVIE.sageValue();
		if (cl.contains("audio") || cl.contains("music"))
			return MediaType.MUSIC.sageValue();
		return MediaType.MOVIE.sageValue();
	}

	@Override
	public void setMediaType(String type) {
	}

	@Override
	public int getSeasonNumber() {
		return 0;
	}

	@Override
	public void setSeasonNumber(int num) {
	}

	@Override
	public int getEpisodeNumber() {
		return 0;
	}

	@Override
	public void setEpisodeNumber(int num) {
	}

	@Override
	public String getIMDBID() {
		return null;
	}

	@Override
	public void setIMDBID(String id) {
	}

	@Override
	public int getDiscNumber() {
		return 0;
	}

	@Override
	public void setDiscNumber(int disc) {
	}

	@Override
	public String getMediaProviderID() {
		return null;
	}

	@Override
	public void setMediaProviderID(String id) {
	}

	@Override
	public String getMediaProviderDataID() {
		return null;
	}

	@Override
	public void setMediaProviderDataID(String id) {
	}

	@Override
	public int getUserRating() {
		return 0;
	}

	@Override
	public void setUserRating(int f) {
	}

	@Override
	public List<IMediaArt> getFanart() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getTrailerUrl() {
		return null;
	}

	@Override
	public void setTrailerUrl(String trailer) {
	}

	@Override
	public int getSeriesInfoID() {
		return 0;
	}

	@Override
	public void setSeriesInfoID(int id) {
	}

	@Override
	public String getDefaultPoster() {
		return null;
	}

	@Override
	public void setDefaultPoster(String poster) {
	}

	@Override
	public String getDefaultBanner() {
		return null;
	}

	@Override
	public void setDefaultBanner(String banner) {
	}

	@Override
	public String getDefaultBackground() {
		return null;
	}

	@Override
	public void setDefaultBackground(String background) {
	}

	@Override
	public String getScrapedBy() {
		return "upnp";
	}

	@Override
	public void setScrapedBy(String by) {
	}

	@Override
	public Date getScrapedDate() {
		return new Date(System.currentTimeMillis());
	}

	@Override
	public void setScrapedDate(Date date) {
	}

	@Override
	public String getMediaUrl() {
		return resource.getValue();
	}

	@Override
	public void setMediaUrl(String trailer) {
	}

	@Override
	public String getTrivia() {
		return null;
	}

	@Override
	public void setTrivia(String trivia) {
	}

	@Override
	public String getQuotes() {
		return null;
	}

	@Override
	public void setQuotes(String quotes) {
	}

	@Override
	public String getTagLine() {
		return null;
	}

	@Override
	public void setTagLine(String tagline) {
	}

	@Override
	public int getEpisodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setEpisodeCount(int count) {
		// TODO Auto-generated method stub
		
	}
}
