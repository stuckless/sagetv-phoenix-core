package sagex.phoenix.metadata;

import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Abstract way of storing metadata to an alternate location.
 * 
 * @author seans
 */
public interface IMetadataPersistence {
	/**
	 * stores the metadata from the given file.
	 * 
	 * @param file
	 *            {@link IMediaFile} that will be updated
	 * @param md
	 *            {@link IMetadata} current metadata that needs to be stored
	 * @param options
	 *            {@link Hints} options that may be used when storing
	 * @throws MetadataException
	 */
	public void storeMetadata(IMediaFile file, IMetadata md, Hints options) throws MetadataException;
}
