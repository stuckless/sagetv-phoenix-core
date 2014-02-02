package sagex.phoenix.metadata;

import java.util.List;

public interface IMetadataProviderInfo {
	public String getId();

	public String getName();

	public String getDescription();

	public String getIconUrl();

	public List<MediaType> getSupportedSearchTypes();

	public void setFanartProviderId(String fanartProvider);

	public String getFanartProviderId();
}
