package sagex.phoenix.vfs.groups;

import org.apache.log4j.Logger;
import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.ConfigList;
import sagex.phoenix.vfs.util.HasOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups based on the value of a Sage Metadata Field on a MediaFile
 *
 * @author sean
 */
public class MetadataFieldGrouper implements IGrouper, HasOptions {
    private Logger log = Logger.getLogger(MetadataFieldGrouper.class);

    /**
     * {@value}
     */
    public static final String OPT_METADATA_FIELD = "field";

    private List<ConfigurableOption> options = new ArrayList<ConfigurableOption>();
    private SageProperty metadataKey = null;

    public MetadataFieldGrouper() {
        options.add(new ConfigurableOption(OPT_METADATA_FIELD, "Metadata Field Name", null, DataType.string, true,
                ListSelection.single, ConfigList.metadataList()));
    }

    @Override
    public List<ConfigurableOption> getOptions() {
        return options;
    }

    @Override
    public void onUpdate(BaseConfigurable parent) {
        String key = parent.getOption(OPT_METADATA_FIELD).getString(null);
        if (key != null) {
            metadataKey = MetadataUtil.getSageProperty(key);
            if (metadataKey == null) {
                log.warn("Invalid Metadata Field Name: " + key);
            }
        }
    }

    @Override
    public String getGroupName(IMediaResource res) {
        if (metadataKey != null && res instanceof IMediaFile) {
            return ((IMediaFile) res).getMetadata().get(metadataKey);
        }
        return null;
    }
}
