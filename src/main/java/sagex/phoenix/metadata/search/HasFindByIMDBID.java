package sagex.phoenix.metadata.search;

import sagex.phoenix.metadata.IMetadata;

public interface HasFindByIMDBID {
	public IMetadata getMetadataForIMDBId(String imdbid);
}
