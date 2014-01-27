package sagex.phoenix.metadata.search;

import sagex.phoenix.util.Hints;
import sagex.phoenix.vfs.IMediaFile;

public interface IFilenameScraper {
	public String getId();
	public int getPriority();
	public SearchQuery createSearchQuery(IMediaFile file, Hints hints);
}

