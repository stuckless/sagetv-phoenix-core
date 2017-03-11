package sagex.phoenix.metadata.provider.tvdb;

import com.omertron.thetvdbapi.TheTVDBApi;
import org.apache.commons.lang.StringUtils;
import sagex.phoenix.configuration.proxy.GroupProxy;
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

import java.util.List;

public class TVDBMetadataProvider extends MetadataProvider implements ITVMetadataProvider {
    public static final String ID = "tvdb";

    TheTVDBApi tvdbApi = null;
    TVDBConfiguration config = null;

    public TVDBMetadataProvider(IMetadataProviderInfo info) {
        super(info);
        tvdbApi = new TheTVDBApi(getApiKey());
        config = GroupProxy.get(TVDBConfiguration.class);
    }

    TheTVDBApi getTVDBApi() {
        return tvdbApi;
    }

    TVDBConfiguration getConfiguration() {
        return config;
    }

    public String getLanguage() {
        String lang = config.getLanguage();
        if (lang==null||lang.isEmpty()) {
            lang="en";
        }
        return lang;
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
            if (res != null) {
                return res;
            }
        }

        // carry on normal search
        if (query.getMediaType() == MediaType.TV) {
            try {
                List<IMetadataSearchResult> results = new TVDBSearchParser(this, query).getResults();
                if (results.size() == 0) {
                    return searchUsingModifiedTitle(query, new MetadataException("Search Failed for " + query.getRawTitle()));
                } else {
                    return results;
                }
            } catch (Exception e) {
                log.warn("Search Failed for " + query, e);
                return searchUsingModifiedTitle(query, new MetadataException("Search Failed for " + query.getRawTitle(), e));
            }
        }

        throw new MetadataException("Unsupported Search Type: " + query.getMediaType(), query);
    }

    private List<IMetadataSearchResult> searchUsingModifiedTitle(SearchQuery query, MetadataException parent) throws MetadataException {
        String title = query.get(Field.QUERY);
        String parts[] = title.split("\\s+");
        if (parts != null && parts.length > 1) {
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("and")) {
                    parts[i] = "&";
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
        // re-throw the parent exception, since nothing changed here
        throw parent;
    }

    /**
     * This is the api key provided for the Batch Metadata Tools. Other projects
     * MUST NOT use this key. If you are including these tools in your project,
     * be sure to set the following System property, to set your own key. <code>
     * themoviedb.api_key=YOUR_KEY
     * </code>
     */
    public static String getApiKey() {
        String key = System.getProperty("thetvdb.api_key");
        if (key == null)
            key = "5645B594A3F32D27";
        return key;
    }

    @Override
    public ISeriesInfo getSeriesInfo(String seriesId) throws MetadataException {
        TVDBSeriesParser parser = new TVDBSeriesParser(this, seriesId);
        return parser.getSeriesInfo();
    }
}
