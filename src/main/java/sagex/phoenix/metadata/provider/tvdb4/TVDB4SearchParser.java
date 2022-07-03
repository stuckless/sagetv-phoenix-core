package sagex.phoenix.metadata.provider.tvdb4;

import org.apache.log4j.Logger;
import sagex.phoenix.metadata.IMetadataSearchResult;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.search.SearchQuery;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class TVDB4SearchParser {
    private static final Logger log = Logger.getLogger(TVDB4SearchParser.class);

    private SearchQuery query = null;
    private List<IMetadataSearchResult> results = new LinkedList<IMetadataSearchResult>();
    private String searchTitle;
    private TVDB4MetadataProvider provider;

    private Comparator<IMetadataSearchResult> sorter = new Comparator<IMetadataSearchResult>() {
        public int compare(IMetadataSearchResult o1, IMetadataSearchResult o2) {
            if (o1.getScore() > o2.getScore())
                return -1;
            if (o1.getScore() < o2.getScore())
                return 1;
            return 0;
        }
    };


    public TVDB4SearchParser(TVDB4MetadataProvider prov, SearchQuery query) {
        this.provider = prov;
        this.query = query;

        searchTitle = query.get(SearchQuery.Field.QUERY);
    }

    public List<IMetadataSearchResult> getResults() throws MetadataException {
        // already parsed
        if (results.size() > 0)
            return results;

        // parse
        try {
            TVDB4JsonHandler jsonHandler = new TVDB4JsonHandler();
            if(jsonHandler.validConfig()){
                results = jsonHandler.search(searchTitle, query, provider.getLanguage());
            }else{
                log.warn("getResults: TVDB4 configuration is not valid.  Check PIN.");
                return null;
            }

            if (results==null || results.size()==0) {
                log.warn("getResults: Could not find any results for: " + searchTitle);
                return results;
            }
            Collections.sort(results, sorter);
        } catch (Exception e) {
            // we got a parse exception, let's try to log the response
            log.debug("getResults: Search Failed using URL " + searchTitle);

            throw new MetadataException("getResults: Failed to get/parse search query", query, e);
        }
        return results;
    }

}
