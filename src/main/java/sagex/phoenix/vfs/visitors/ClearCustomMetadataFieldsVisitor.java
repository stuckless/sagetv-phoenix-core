package sagex.phoenix.vfs.visitors;

import sagex.phoenix.metadata.ISageCustomMetadataRW;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Clears/Resets custom metadta fields for a Resource
 * 
 * @author seans
 *
 */
public class ClearCustomMetadataFieldsVisitor extends FileVisitor {
	public ClearCustomMetadataFieldsVisitor() {
	}

	@Override
	public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
		clearMetadata((IMediaFile) res);
		incrementAffected();
		return true;
	}
	
	/**
	 * Given a {@link IMediaFile} it will clear the custom metadata (ie fanart metadata)
	 * associated with it.
	 * 
	 * @param mf
	 */
	public static void clearMetadata(IMediaFile mf) {
		if (mf==null) return;
		ISageCustomMetadataRW md = mf.getMetadata();
		md.setDiscNumber(0);
		md.setEpisodeNumber(0);
		md.setIMDBID("");
		md.setMediaProviderDataID("");
		md.setMediaProviderID("");
		md.setMediaTitle("");
		md.setMediaType("");
		md.setSeasonNumber(0);
		md.setUserRating(0);
	}
}
