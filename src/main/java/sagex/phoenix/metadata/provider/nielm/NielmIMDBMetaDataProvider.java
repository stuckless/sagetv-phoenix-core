package sagex.phoenix.metadata.provider.nielm;

import net.sf.sageplugins.sageimdb.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.provider.imdb.IMDBUtils;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.Pair;
import sagex.phoenix.util.Similarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class NielmIMDBMetaDataProvider extends MetadataProvider {
    private static final Logger log = Logger.getLogger(NielmIMDBMetaDataProvider.class);

    private ImdbWebBackend db = null;

    public NielmIMDBMetaDataProvider(IMetadataProviderInfo info) {
        super(info);
        db = new ImdbWebBackend();
    }

    public IMetadata getMetaDataByUrl(String url) throws MetadataException {
        ImdbWebObjectRef objRef = new ImdbWebObjectRef(DbObjectRef.DB_TYPE_TITLE, "IMDB Url", url);
        DbTitleObject title;
        try {
            title = (DbTitleObject) objRef.getDbObject(db);
            return new NeilmIMDBMetaDataParser(this, db, title).getMetaData();
        } catch (Exception e) {
            log.error("IMDB Lookup Failed:" + url, e);
            throw new MetadataException("IMDB Search failed for " + url, e);
        }
    }

    private void updateTitleAndYear(MediaSearchResult vsr, Role r) {
        String buf = r.getName().getName();
        Pair<String, String> parts = IMDBUtils.parseTitle(buf);
        vsr.setTitle(parts.first());
        vsr.setYear(NumberUtils.toInt(parts.second()));
    }

    @Override
    public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException {
        if (MetadataSearchUtil.hasMetadata(result))
            return MetadataSearchUtil.getMetadata(result);

        String url = result.getUrl();
        if (StringUtils.isEmpty(url)) {
            url = IMDBUtils.createDetailUrl(result.getId());
        }

        return getMetaDataByUrl(url);
    }

    @Override
    public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException {
        log.debug("Search Query: " + query);

        // search by ID, if the ID is present
        if (!StringUtils.isEmpty(query.get(SearchQuery.Field.ID))) {
            List<IMetadataSearchResult> res = MetadataSearchUtil.searchById(this, query, query.get(SearchQuery.Field.ID));
            if (res != null) {
                IMetadataSearchResult r = res.get(0);
                if (r instanceof MediaSearchResult) {
                    ((MediaSearchResult) r).setIMDBId(IMDBUtils.parseIMDBID(r.getUrl()));
                }
                return res;
            }
        }

        // carry on normal search
        String arg = query.get(SearchQuery.Field.QUERY);
        if (arg == null) {
            log.warn("The QUERY field was not set in the SearchQuery for: " + query
                    + ";  This is most likey a programmer oversight.");
            return null;
        }

        List<IMetadataSearchResult> results = new ArrayList<IMetadataSearchResult>();

        try {
            Vector<Role> list = db.searchTitle(arg);
            for (Role r : list) {
                MediaSearchResult vsr = new MediaSearchResult();
                String title = r.getName().getName();
                if (title == null)
                    continue;
                title = title.toLowerCase();
                if (title.contains("(tv series"))
                    continue;
                if (title.contains("(tv episode"))
                    continue;
                if (title.contains("(video game)"))
                    continue;

                // System.out.println(r.getName().getName());
                updateTitleAndYear(vsr, r);
                vsr.setScore(Similarity.getInstance().compareStrings(arg, vsr.getTitle()));
                vsr.setProviderId(getInfo().getId());
                DbObjectRef objRef = r.getName();
                if (objRef instanceof ImdbWebObjectRef) {
                    // set the imdb url as the ID for this result.
                    // that will enable us to find it later
                    String url = ((ImdbWebObjectRef) objRef).getImdbRef();
                    if (url == null)
                        continue;
                    if (!url.startsWith("http")) {
                        url = getIMDBPrefixUrl() + url;
                    }
                    vsr.setUrl(url);
                    vsr.setId(IMDBUtils.parseIMDBID(((ImdbWebObjectRef) objRef).getImdbRef()));
                    vsr.setIMDBId(IMDBUtils.parseIMDBID(((ImdbWebObjectRef) objRef).getImdbRef()));
                    vsr.setProviderId(getInfo().getId());
                    vsr.setMediaType(query.getMediaType());
                } else {
                    log.error("Imdb Search result was incorrect type: " + objRef.getClass().getName());
                }
                results.add(vsr);
            }
        } catch (Exception e) {
            throw new MetadataException("Nielm IMDB Failed", query, e);
        }

        return results;
    }

    private String getIMDBPrefixUrl() {
        return "http://www.imdb.com";
    }
}
