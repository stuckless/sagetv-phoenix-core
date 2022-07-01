package sagex.phoenix.metadata.provider.tvdb4;

import org.apache.commons.lang.StringUtils;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;

import java.util.List;

public class TVDB4MetadataProvider  extends MetadataProvider implements ITVMetadataProvider {
    public static final String ID = "tvdb4";
    TVDB4Configuration config = null;

    public TVDB4MetadataProvider(IMetadataProviderInfo info) {
        super(info);
        config = GroupProxy.get(TVDB4Configuration.class);
    }

    TVDB4Configuration getConfiguration() {
        return config;
    }

    public String getLanguage() {
        String lang = config.getLanguage();
        if (lang==null||lang.isEmpty()) {
            lang="eng";
        }
        return lang;
    }

    @Override
    public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
        if (MetadataSearchUtil.hasMetadata(result)) {
            return MetadataSearchUtil.getMetadata(result);
        }

        return new TVDB4ItemParser(this, result).getMetadata();
    }

    @Override
    public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
        // search by ID, if the ID is present
        if (!StringUtils.isEmpty(query.get(SearchQuery.Field.ID))) {
            log.debug("search: by ID:" + SearchQuery.Field.ID);
            List<IMetadataSearchResult> res = MetadataSearchUtil.searchById(this, query, query.get(SearchQuery.Field.ID));
            if (res != null) {
                return res;
            }
        }

        // carry on normal search
        if (query.getMediaType() == MediaType.TV) {
            try {
                log.debug("search: by info in query - no ID available:" + query);
                List<IMetadataSearchResult> results = new TVDB4SearchParser(this, query).getResults();
                if (results!=null && results.size() == 0) {
                    log.debug("search: by info FAILED so use MODIFIED Title:");
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
                log.debug("searchUsingModifiedTitle: Searching again with 'and' replaced by '&' for " + newTitle);
                query.set(SearchQuery.Field.QUERY, newTitle);
                // do the search again
                return new TVDB4SearchParser(this, query).getResults();
            }
        }
        // re-throw the parent exception, since nothing changed here
        throw parent;
    }

    @Override
    public ISeriesInfo getSeriesInfo(String seriesId) throws MetadataException {
        TVDB4SeriesParser parser = new TVDB4SeriesParser(this, seriesId);
        return parser.getSeriesInfo();
    }

}
