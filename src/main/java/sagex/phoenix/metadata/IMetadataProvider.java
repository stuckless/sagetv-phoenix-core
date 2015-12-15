package sagex.phoenix.metadata;

import sagex.phoenix.metadata.search.SearchQuery;

import java.util.List;

public interface IMetadataProvider {
    public IMetadataProviderInfo getInfo();

    public IMetadata getMetaData(IMetadataSearchResult result) throws MetadataException;

    public List<IMetadataSearchResult> search(SearchQuery query) throws MetadataException;
}
