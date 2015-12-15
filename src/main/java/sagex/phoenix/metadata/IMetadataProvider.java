package sagex.phoenix.metadata;

import java.util.List;

import sagex.phoenix.metadata.search.SearchQuery;

public interface IMetadataProvider {
    public IMetadataProviderInfo getInfo();

    public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException;

    public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException;
}
