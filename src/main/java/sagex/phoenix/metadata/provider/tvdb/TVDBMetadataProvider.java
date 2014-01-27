package sagex.phoenix.metadata.provider.tvdb;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataProviderInfo;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.ITVMetadataProvider;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataProvider;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.metadata.search.SearchQuery.Field;

public class TVDBMetadataProvider extends MetadataProvider implements ITVMetadataProvider {
	public static final String ID = "tvdb";
	public static final String     FANART_URL          = "http://www.thetvdb.com/banners/{0}";
	
	public TVDBMetadataProvider(IMetadataProviderInfo info) {
		super(info);
	}
	
    public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
        if (MetadataSearchUtil.hasMetadata(result)) {
            log.info("Returning Metadata stored in the Result.");
            return MetadataSearchUtil.getMetadata(result);
        }

        return new TVDBItemParser(this, result).getMetadata();
    }

    public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
        // search by ID, if the ID is present
        if (!StringUtils.isEmpty(query.get(SearchQuery.Field.ID))) {
            List<IMetadataSearchResult> res = MetadataSearchUtil.searchById(this, query, query.get(SearchQuery.Field.ID));
            if (res!=null) {
                return res;
            }
        }
        
        // carry on normal search
        if (query.getMediaType() ==  MediaType.TV) {
        	try {
	    		List<IMetadataSearchResult> results = new TVDBSearchParser(this, query).getResults();
	    		if (results.size()==0) {
	    			return searchUsingModifiedTitle(query);
	    		} else {
	    			return results;
	    		}
        	} catch (Exception e) {
    			return searchUsingModifiedTitle(query);
        	}
        }
        
        throw new MetadataException("Unsupported Search Type: " + query.getMediaType(), query);
    }

    private List<IMetadataSearchResult> searchUsingModifiedTitle(SearchQuery query) throws MetadataException {
    	String title = query.get(Field.QUERY);
    	String parts[] = title.split("\\s+");
    	if (parts!=null && parts.length>1) {
	    	for (int i=0;i<parts.length;i++) {
	    		if (parts[i].equalsIgnoreCase("and")) {
	    			parts[i]="&";
	    			break;
	    		}
	    	}
	    	String newTitle = StringUtils.join(parts, " ");
	    	if (!newTitle.equalsIgnoreCase(title)) {
	    		log.debug("Searching again with 'and' replaced by '&' for " + newTitle);
	    		query.set(Field.QUERY, newTitle);
	    		// do the search again
	    		return new TVDBSearchParser(this, query).getResults();
	    	}
    	}
		return null;
	}

	/**
     * This is the api key provided for the Batch Metadata Tools. Other projects
     * MUST NOT use this key. If you are including these tools in your project,
     * be sure to set the following System property, to set your own key. <code>
     * themoviedb.api_key=YOUR_KEY
     * </code>
     */
    public static Object getApiKey() {
        String key = System.getProperty("thetvdb.api_key");
        if (key == null) key = "5645B594A3F32D27";
        return key;
    }
    
    /**
     * Returns a complete fanart path for the given path or null if the path is empty
     * 
     * @param path
     * @return
     */
    public static String getFanartURL(String path) {
    	if (!StringUtils.isEmpty(path)) {
    		return MessageFormat.format(FANART_URL, path);
    	}
    	return null;
    }

	@Override
	public ISeriesInfo getSeriesInfo(String seriesId) throws MetadataException {
		TVDBSeriesParser parser = new TVDBSeriesParser(seriesId);
		return parser.getSeriesInfo();
	}
}
