package phoenix.impl;

import java.util.List;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.ISageMetadata;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.vfs.IMediaFile;

@API(group="metadata")
public class MetadataExtAPI {
	/**
	 * returns the first genre
	 * 
	 * @param md
	 * @return
	 */
	public String getCategory(IMetadata md) {
		return getGenre(md, 0);
	}

	/**
	 * can accept 
	 * @param mediaFile
	 * @return
	 */
	public String getCategory(IMediaFile mediaFile) {
		if (mediaFile==null) return null;
		return getGenre(mediaFile.getMetadata(), 0);
	}

	/**
	 * return the second genre
	 * 
	 * @param md
	 * @return
	 */
	public String getSubCategory(IMetadata md) {
		return getGenre(md, 1);
	}

	public String getSubCategory(IMediaFile mediaFile) {
		if (mediaFile==null) return null;
		return getGenre(mediaFile.getMetadata(), 1);
	}
	
	/**
	 * returns a genre at a given position or null
	 * 
	 * @param md
	 * @param pos
	 * @return
	 */
	public String getGenre(IMetadata md, int pos) {
		if (md==null) return null;
		List<String> genres = md.getGenres();
		if (genres.size()>=pos) return genres.get(pos);
		return null;
	}
	
	/**
	 * returns true if the named sage metadata field has been set to a non empty value
	 * 
	 * @param isagemetadata
	 * @param key
	 * @return
	 */
   public static boolean IsSet(ISageMetadata isagemetadata, String key) {
	   SageProperty skey = MetadataUtil.getSageProperty(key);
	   if (skey!=null) {
		   return isagemetadata.isSet(skey);
	   }
	   return false;
   }
}
