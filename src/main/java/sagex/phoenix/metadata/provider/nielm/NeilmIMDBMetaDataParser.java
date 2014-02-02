package sagex.phoenix.metadata.provider.nielm;

import java.util.Vector;

import net.sf.sageplugins.sageimdb.DbTitleObject;
import net.sf.sageplugins.sageimdb.ImdbWebBackend;
import net.sf.sageplugins.sageimdb.Role;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProvider;
import sagex.phoenix.metadata.MediaArt;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.provider.imdb.IMDBUtils;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.Loggers;

public class NeilmIMDBMetaDataParser {
	private ImdbWebBackend db = null;
	private DbTitleObject data = null;

	private IMetadata metadata = null;
	private IMetadataProvider provider;

	private MetadataConfiguration mdConfig = GroupProxy.get(MetadataConfiguration.class);

	public NeilmIMDBMetaDataParser(IMetadataProvider prov, ImdbWebBackend db, DbTitleObject data) {
		this.data = data;
		this.db = db;
		this.provider = prov;

	}

	public IMetadata getMetaData() {
		if (metadata == null) {
			metadata = MetadataProxy.newInstance();
			metadata.setMediaProviderID(provider.getInfo().getId());
			metadata.setMediaProviderDataID(IMDBUtils.parseIMDBID(data.getImdbUrl()));
			metadata.setIMDBID(IMDBUtils.parseIMDBID(data.getImdbUrl()));
			metadata.setMediaType(MediaType.MOVIE.sageValue());

			updateCastMembers();

			if (data.getGenres() != null) {
				for (String s : data.getGenres()) {
					metadata.getGenres().add(s);
				}
			}

			metadata.setRated(Phoenix.getInstance().getRatingsManager()
					.getRating(MediaType.MOVIE, MetadataSearchUtil.parseMPAARating(data.getMPAArating())));
			metadata.setExtendedRatings(data.getMPAArating());
			metadata.setDescription(data.getSummaries());
			metadata.setOriginalAirDate(DateUtils.parseDate(data.getAiringDate()));
			try {
				metadata.setRunningTime(data.getDuration() * 60 * 1000);
			} catch (Throwable t) {
				Loggers.METADATA.warn("Failed to get Runtime information from IMDB", t);
			}

			if (data.getImageURL() != null) {
				MediaArt ma = new MediaArt();
				ma.setType(MediaArtifactType.POSTER);
				ma.setDownloadUrl(data.getImageURL().toExternalForm());
				metadata.getFanart().add(ma);
			}

			metadata.setMediaTitle(data.getName());
			metadata.setEpisodeName(data.getName());
			metadata.setUserRating(MetadataSearchUtil.parseUserRating(data.getRating()));
			metadata.setYear(NumberUtils.toInt(data.getYear()));

			if (mdConfig.getFetchQuotesAndTrivia()) {
				metadata.setTrivia(data.getTrivia());
				metadata.setQuotes(data.getQuotes());
			}

			metadata.setTagLine(data.getTagLines());
		}
		return metadata;
	}

	public void updateCastMembers() {
		Vector<Role> cast = data.getCast();
		if (cast != null && cast.size() > 0) {
			for (Role role : cast) {
				metadata.getActors().add(new CastMember(role.getName().getName(), role.getPart()));
			}
		}

		cast = data.getOriginators();
		if (cast != null && cast.size() > 0) {
			for (Role role : cast) {
				String part = role.getPart();
				if (part == null)
					continue;
				part = part.trim();
				if ("(Directed by)".equalsIgnoreCase(part) || "directed".equalsIgnoreCase(part)) {
					metadata.getDirectors().add(new CastMember(role.getName().getName(), null));
				} else if ("(Writing credits)".equalsIgnoreCase(part) || "Writer".equalsIgnoreCase(part)
						|| "Writing".equalsIgnoreCase(part)) {
					metadata.getWriters().add(new CastMember(role.getName().getName(), null));
				} else if ("producer".equalsIgnoreCase(part)) {
					metadata.getProducers().add(new CastMember(role.getName().getName(), null));
				} else if ("choreographer".equalsIgnoreCase(part)) {
					metadata.getChoreographers().add(new CastMember(role.getName().getName(), null));
				}
			}
		}

	}
}
