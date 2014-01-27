package sagex.phoenix.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MetadataProviderInfo implements IMetadataProviderInfo, Serializable {
    private static final long serialVersionUID = 1L;
    private String            id, name, description, iconUrl;
	private List<MediaType> mediaTypes = new ArrayList<MediaType>();
	private String fanartId;

    public MetadataProviderInfo() {
    }

    public MetadataProviderInfo(String id, String name, String description, String iconUrl, List<MediaType> types) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        if (types!=null) {
        	this.mediaTypes.addAll(types);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

	@Override
	public List<MediaType> getSupportedSearchTypes() {
		return mediaTypes;
	}

	@Override
	public void setFanartProviderId(String fanartProvider) {
		this.fanartId = fanartProvider;
	}
	
	public String getFanartProviderId() {
		return fanartId;
	}

	@Override
	public String toString() {
		return "MetadataProviderInfo [id=" + id + ", fanartId=" + fanartId
				+ ", mediaTypes=" + mediaTypes + ", name=" + name + "]";
	}
}
