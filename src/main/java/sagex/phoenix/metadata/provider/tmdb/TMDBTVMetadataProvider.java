package sagex.phoenix.metadata.provider.tmdb;

import com.omertron.themoviedbapi.MovieDbException;

import com.omertron.themoviedbapi.TheMovieDbApi;

import org.apache.commons.lang.StringUtils;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;

import java.util.List;

/**
 * Created by jusjoken on 9/19/2021.
 */
public class TMDBTVMetadataProvider extends MetadataProvider implements ITVMetadataProvider {
    public static final String ID = "tmdb";

    TheMovieDbApi tmdb = null;
    TMDBConfiguration config = null;

    public TMDBTVMetadataProvider(IMetadataProviderInfo info) {
        super(info);
        try {
            tmdb = new TheMovieDbApi(getApiKey());
        } catch (MovieDbException e) {
            e.printStackTrace();
        }
        config = GroupProxy.get(TMDBConfiguration.class);
    }

    TheMovieDbApi getTVApi() {
        return tmdb;
    }

    TMDBConfiguration getConfiguration() {
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

        return new TMDBTVItemParser(this, result).getMetadata();

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
                List<IMetadataSearchResult> results = new TMDBTVSearchParser(this, query).getResults();
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
        String title = query.get(SearchQuery.Field.QUERY);
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
                query.set(SearchQuery.Field.QUERY, newTitle);
                // do the search again
                return new TMDBTVSearchParser(this, query).getResults();
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
        String key = System.getProperty("themoviedb.api_key");
        if (key == null)
            key = "d4ad46ee51d364386b6cf3b580fb5d8c";
        return key;
    }

    @Override
    public ISeriesInfo getSeriesInfo(String seriesId) throws MetadataException {
        TMDBTVSeriesParser parser = new TMDBTVSeriesParser(this, seriesId);
        return parser.getSeriesInfo();
    }
}
