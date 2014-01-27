package sagex.phoenix.metadata.persistence;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataPersistence;
import sagex.phoenix.metadata.ISageCustomMetadataRW;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataHints;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.fixes.FixParentalRatingsVisitor;
import sagex.phoenix.metadata.fixes.FixTVYearVisitor;
import sagex.phoenix.util.Hints;
import sagex.phoenix.util.LogUtil;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * Save the metadata to the Sage Object of the given MediaFile
 *  
 * @author seans
 *
 */
public class Sage7Persistence implements IMetadataPersistence {
	private Logger log = Logger.getLogger(this.getClass());
	private MetadataConfiguration config = GroupProxy.get(MetadataConfiguration.class);
	
	public Sage7Persistence() {
	}

	@Override
	public void storeMetadata(IMediaFile file, IMetadata md, Hints options) throws MetadataException {
		try {
			if (file.isType(MediaResourceType.MUSIC.value())) {
				log.info("Not updating metadata for music file " + file.getTitle());
				return;
			}
			
			// don't even try to save metadata for airings
			if (file.isType(MediaResourceType.EPG_AIRING.value())) {
				log.info("Can't save info for Airings; Title was " + file.getTitle());
				// force the fanart title to be the title in the airing, since we can't change it.
				md.setMediaTitle(file.getTitle());
				return;
			}
			
			// check if we are a TV update, and if so, we have a valid episode
			MediaType mt = MediaType.toMediaType(md.getMediaType());
			if (mt == MediaType.TV) {
				// for TV to update, we need AT LEAST an episode # or a disc #
				if (md.getEpisodeNumber()<=0 && md.getDiscNumber()<= 0) {
					// force the fanart title to be the title in the airing, since we can't change it.
					md.setMediaTitle(file.getTitle());
					log.info("Skipping Metadata Update for TV File: " + file + ", because it doesn't have an episode or disc #");
					return;
				}
			}
			
			// carry on and update
			boolean preserve = options.getBooleanValue(MetadataHints.PRESERVE_ORIGINAL_METADATA, config.getPreserverRecordingMetadata());
			if (file.isType(MediaResourceType.RECORDING.value()) 
					&& !MetadataUtil.isImportedRecording(file) 
					&& preserve) {
				updateCustomMetadataFieldsOnly(file, md, options);
				if (config.getFillInMissingRecordingMetadata()) {
					MetadataUtil.fillMetadata(md, file.getMetadata());
					try {
						FixTVYearVisitor.fixTVYear(file, file.getMetadata());
						FixParentalRatingsVisitor.fixParentalRating(file, file.getMetadata());
					} catch (Throwable t) {
						log.warn("Failed while fixing metadata fields.  Will carry on.");
					}
				}
			} else if (options.getBooleanValue(MetadataHints.UPDATE_FANART, true) && !options.getBooleanValue(MetadataHints.UPDATE_METADATA, true)) {
				updateCustomMetadataFieldsOnly(file, md, options);
			} else {
				log.info("Storing updated metadata for item " + file);
				// now save the metadata
				IMetadata newMD = file.getMetadata();
				// copy the modified metadata parts from the source to the dest
				MetadataUtil.copyModifiedMetadata(md, newMD);
				
				// check to see if we are importing this as a recording
				if (!file.isType(MediaResourceType.RECORDING.value()) && file.isType(MediaResourceType.TV.value()) && options.getBooleanValue(MetadataHints.IMPORT_TV_AS_RECORDING, false)) {
					Phoenix.getInstance().getMetadataManager().importMediaFileAsRecording(file);
					if (!config.getArchiveRecordings()) {
						file.setLibraryFile(false);
					}
				}
				
				// for recordings, the "Title" field should just be the title
				// but for non-recordings, it should be the relative path of the import
				if (file.isType(MediaResourceType.RECORDING.value())) {
					if (!StringUtils.isEmpty(md.getRelativePathWithTitle())) {
						newMD.setRelativePathWithTitle(md.getRelativePathWithTitle());
					} else {
						newMD.setRelativePathWithTitle(md.getMediaTitle());
					}
				} else {
					if (config.getPrefixVideosWithRelativePath()) {
						newMD.setRelativePathWithTitle(MetadataUtil.getRelativePathWithTitle(file, md));
					} else {
						newMD.setRelativePathWithTitle(md.getMediaTitle());
					}
				}
				
				// fix the metadata to be saved...
				try {
					FixTVYearVisitor.fixTVYear(file, newMD);
					FixParentalRatingsVisitor.fixParentalRating(file, newMD);
				} catch (Throwable t) {
					log.warn("Failed while fixing metadata fields.  Will carry on.");
				}
			}
			
			try {
				if (file.isType(MediaResourceType.TV.value())) {
					if (!TVSeriesUtil.updateTVSeriesInfoForFile(file)) {
						log.warn("Failed to update tv series info for: " + file.getTitle());
					}
				}
			} catch (Exception e) {
				log.warn("Failed to update the Series Info for " + file, e);
			}
			
			// store the fact that "Phoenix" updated the file
			file.getMetadata().setScrapedBy("Phoenix");
			file.getMetadata().setScrapedDate(new Date(System.currentTimeMillis()));
			LogUtil.logMetadataUpdated(file);
		} catch (Exception e) {
			LogUtil.logMetadataUpdatedError(file, e);
			throw new MetadataException("Failed to store metadata", file, md, e);
		}
	}

	private void updateCustomMetadataFieldsOnly(IMediaFile file, IMetadata md,	Hints options) {
		log.warn("Only custom metadata fields are being updated for file " + file + "; Hints: " + options);
		// only update the custom metadata fields
		ISageCustomMetadataRW sage = file.getMetadata();
		sage.setDiscNumber(md.getDiscNumber());
		sage.setEpisodeNumber(md.getEpisodeNumber());
		sage.setIMDBID(md.getIMDBID());
		sage.setMediaProviderDataID(md.getMediaProviderDataID());
		sage.setMediaProviderID(md.getMediaProviderID());
		sage.setMediaTitle(md.getMediaTitle());
		sage.setMediaType(md.getMediaType());
		sage.setSeasonNumber(md.getSeasonNumber());
		sage.setUserRating(md.getUserRating());
		sage.getFanart().clear();
		sage.getFanart().addAll(md.getFanart());
		sage.setTagLine(md.getTagLine());
		if (config.getFetchQuotesAndTrivia()) {
			sage.setQuotes(md.getQuotes());
			sage.setTrivia(md.getTrivia());
		}
		sage.setTrailerUrl(md.getTrailerUrl());
	}
}
