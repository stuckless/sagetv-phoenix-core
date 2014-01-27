package sagex.phoenix.metadata.persistence;

import java.io.File;

import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataPersistence;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.util.PathUtils;

/**
 * Save a Sage7 thumbanil file for a given mediafile
 *  
 * @author seans
 *
 */
public class Sage7ThumbnailPersistence implements IMetadataPersistence {
	public Sage7ThumbnailPersistence() {
	}

	@Override
	public void storeMetadata(IMediaFile file, IMetadata md, Hints options) throws MetadataException {
		try {
			File f = PathUtils.getFirstFile(file);
			if (f!=null && f.exists()) {
				MediaType mt = MediaType.MOVIE;
				if (file.isType(MediaResourceType.MUSIC.value())) {
					mt = MediaType.MUSIC;
				} else if (file.isType(MediaResourceType.TV.value())) {
					mt = MediaType.TV;
				} else if (file.isType(MediaResourceType.PICTURE.value())) {
					return;
				}
				File thumbfile = FanartUtil.getLocalFanartForFile(f, mt, MediaArtifactType.POSTER, false);
				if (thumbfile==null) return;
				if (thumbfile.exists()) {
					// do nothing, since it exists
					return;
				}
				
				String poster = phoenix.fanart.GetFanartPoster(file);
				if (poster==null) return;
				
				File posterFile = new File(poster);
				if (!posterFile.exists()) return;
				
				PersistenceUtil.writeImageFromUrl(posterFile.toURI().toString(), thumbfile, 150);
			}
		} catch (Exception e) {
			throw new MetadataException("Failed to write Sage7 thumbnail file", file, md, e);
		}
	}
}
