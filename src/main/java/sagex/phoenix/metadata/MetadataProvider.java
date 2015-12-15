package sagex.phoenix.metadata;

import org.apache.log4j.Logger;

/**
 * Basic stub provider implementation
 *
 * @author seans
 */
public abstract class MetadataProvider implements IMetadataProvider {
    protected Logger log = Logger.getLogger(this.getClass());

    private IMetadataProviderInfo info;

    public MetadataProvider() {
    }

    public MetadataProvider(IMetadataProviderInfo info) {
        setProviderInfo(info);
    }

    public void setProviderInfo(IMetadataProviderInfo info) {
        this.info = info;
    }

    @Override
    public IMetadataProviderInfo getInfo() {
        return info;
    }

    protected void setInfo(IMetadataProviderInfo info) {
        this.info = info;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + info + "]";
    }
}
