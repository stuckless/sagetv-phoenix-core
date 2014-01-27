package sagex.phoenix.metadata;

import sagex.phoenix.tools.annotation.API;

/**
 * Just a PlaceHolder class so that the Phoenix API can auto-generate the API
 * for all Metadata
 * 
 * @author seans
 */
@API(group="metadata", proxy=true, prefix="Metadata", resolver="phoenix.media.GetMetadata")
public interface IMetadata extends ISageMetadataALL {
	/**
	 * Reserved Property Key to identify that an item is watched
	 * {@value}
	 */
	public static final String XWatched = "X-Watched";
	
	/**
	 * Reserved Property Key to identify that an item is a LibraryFile
	 * {@value}
	 */
	public static final String XLibraryFile = "X-LibraryFile";
}
