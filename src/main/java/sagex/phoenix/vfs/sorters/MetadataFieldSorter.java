package sagex.phoenix.vfs.sorters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.filters.MetadataFieldFilter;
import sagex.phoenix.vfs.groups.MetadataFieldGrouper;
import sagex.phoenix.vfs.util.ConfigList;
import sagex.phoenix.vfs.util.HasOptions;

public class MetadataFieldSorter implements Comparator<IMediaResource>, Serializable, HasOptions {
    private static final long serialVersionUID = 1L;

    /**
     * {@value}
     */
    public static final String OPT_METADATA_FIELD = MetadataFieldGrouper.OPT_METADATA_FIELD;

    /**
     * {@value}
     */
    private static final String OPT_COMPARE_AS_NUMBER = MetadataFieldFilter.OPT_COMPARE_AS_NUMBER;

    private List<ConfigurableOption> options = new ArrayList<ConfigurableOption>();
    private transient SageProperty field = null;
    private boolean numericCompare = false;

    public MetadataFieldSorter() {
        options.add(new ConfigurableOption(OPT_METADATA_FIELD, "Metadata Field Name", null, DataType.string, true,
                ListSelection.single, ConfigList.metadataList()));
        options.add(new ConfigurableOption(OPT_COMPARE_AS_NUMBER, "Compare as Numbers", "false", DataType.bool, true,
                ListSelection.single, "true:Yes,no:No"));
    }

    public int compare(IMediaResource o1, IMediaResource o2) {
        if (o1 == null || o2 == null) {
            return -1;
        }
        if (!(o1 instanceof IMediaFile)) {
            return -1;
        }
        if (!(o2 instanceof IMediaFile)) {
            return -1;
        }

        String v1 = ((IMediaFile) o1).getMetadata().get(field);
        String v2 = ((IMediaFile) o2).getMetadata().get(field);
        if (numericCompare) {
            int i1 = NumberUtils.toInt(v1, 0), i2 = NumberUtils.toInt(v2, 0);
            int val = 0;
            if (i1 == i2)
                val = 0;
            if (i1 < i2)
                val = -1;
            if (i1 > i2)
                val = 1;
            return val;
        }

        if (v1 == null && v2 == null)
            return 0;
        if (v1 == null)
            return -1;
        return v1.compareToIgnoreCase(v2);
    }

    @Override
    public List<ConfigurableOption> getOptions() {
        return options;
    }

    @Override
    public void onUpdate(BaseConfigurable parent) {
        numericCompare = parent.getOption(OPT_COMPARE_AS_NUMBER).getBoolean(false);
        String key = parent.getOption(OPT_METADATA_FIELD).getString(null);
        field = MetadataUtil.getSageProperty(key);
        if (field == null) {
            Loggers.LOG.warn("MetadataValueSorter: Invalid SageTV Metadata Field Name " + key);
        }
    }
}