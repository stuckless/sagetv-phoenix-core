package sagex.phoenix.metadata;

import java.util.Map;

public interface IMetadataSearchResult {
	public Map<String, String> getExtra();

	public void setProviderId(String id);

	public String getProviderId();

	public MediaType getMediaType();

	public String getTitle();

	public int getYear();

	public float getScore();

	public String getId();

	public String getUrl();

}
