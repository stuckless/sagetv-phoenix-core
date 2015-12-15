package sagex.phoenix.metadata.provider.tmdb;

import org.apache.commons.lang.StringUtils;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.search.HasFindByIMDBID;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.url.UrlUtil;

import java.util.List;

public class TheMovieDBMetadataProvider extends MetadataProvider implements HasFindByIMDBID {
    private static final String SEARCH_URL = "http://api.themoviedb.org/2.1/Movie.search/%s/xml/%s/%s";
    public static final String ITEM_URL = "http://api.themoviedb.org/2.1/Movie.getInfo/%s/xml/%s/%s";
    public static final String IMDB_ITEM_URL = "http://api.themoviedb.org/2.1/Movie.imdbLookup/%s/xml/%s/%s";

    private String locale = "en";

    public TheMovieDBMetadataProvider(IMetadataProviderInfo info) {
        super(info);
    }

    public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
        if (MetadataSearchUtil.hasMetadata(result))
            return MetadataSearchUtil.getMetadata(result);

        String url = result.getUrl();
        if (url == null && result.getId() != null) {
            url = getItemUrl(result.getId());
        }

        return new TheMovieDBItemParser(url, this).getMetadata();
    }

    public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
        // search by ID, if the ID is present
        if (!StringUtils.isEmpty(query.get(SearchQuery.Field.ID))) {
            List<IMetadataSearchResult> res = MetadataSearchUtil.searchById(this, query, query.get(SearchQuery.Field.ID));
            if (res != null) {
                if (res.size() == 1) {
                    // need to update the url, since the search util just sets
                    // it to an id
                    IMetadataSearchResult md = res.get(0);
                    if (md instanceof MediaSearchResult) {
                        ((MediaSearchResult) md).setUrl(getItemUrl(md.getId()));
                    }
                }
                return res;
            }
        }

        // carry on normal search
        if (query.getMediaType() == MediaType.MOVIE) {
            return new TheMovieDBSearchParser(query, this).getResults();
        }

        throw new MetadataException("Unsupported Search Type: " + query.getMediaType(), query);
    }

    /**
     * This is the api key provided for the Batch Metadata Tools. Other projects
     * MUST NOT use this key. If you are including these tools in your project,
     * be sure to set the following System property, to set your own key. <code>
     * themoviedb.api_key=YOUR_KEY
     * </code>
     */
    public static Object getApiKey() {
        String key = System.getProperty("themoviedb.api_key");
        if (key == null)
            key = "d4ad46ee51d364386b6cf3b580fb5d8c";
        return key;
    }

    public IMetadata getMetadataForIMDBId(String imdbid) {
        MediaSearchResult sr = new MediaSearchResult();
        sr.setUrl(getIMDBItemUrl(imdbid));
        try {
            return getMetaData(sr);
        } catch (Exception e) {
            log.warn("Failed to find result for imdb: " + imdbid, e);
        }
        return null;
    }

    public String getSearchUrl(String query) {
        return String.format(SEARCH_URL, locale, TheMovieDBMetadataProvider.getApiKey(), UrlUtil.encode(query));
    }

    public String getItemUrl(String tmdbId) {
        return String.format(ITEM_URL, locale, TheMovieDBMetadataProvider.getApiKey(), UrlUtil.encode(tmdbId));
    }

    public String getIMDBItemUrl(String imdbId) {
        return String.format(IMDB_ITEM_URL, locale, TheMovieDBMetadataProvider.getApiKey(), UrlUtil.encode(imdbId));
    }
}
