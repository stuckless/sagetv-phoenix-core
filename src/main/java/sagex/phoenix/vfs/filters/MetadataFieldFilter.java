package sagex.phoenix.vfs.filters;

import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.groups.MetadataFieldGrouper;
import sagex.phoenix.vfs.util.ConfigList;

/**
 * Filters based on the value of a metadata field
 * 
 * @author seans
 *
 */
public class MetadataFieldFilter extends Filter {
	/**
	 * {@value}
	 */
    public static final String OPT_COMPARE_AS_NUMBER = "compare-numeric";
    
	private SageProperty field;
    private String value;
    private boolean asNumber; 
    
    public MetadataFieldFilter() {
    	super();
    	addOption(new ConfigurableOption(MetadataFieldGrouper.OPT_METADATA_FIELD, "Metadata Field", null, DataType.string));
    	addOption(new ConfigurableOption(OPT_VALUE, "Field Value", null, DataType.string, true, ListSelection.single, ConfigList.metadataList()));
    	addOption(new ConfigurableOption(OPT_COMPARE_AS_NUMBER, "Compare as Numbers", "false", DataType.bool, true, ListSelection.single, "true:Yes,no:No"));
    }

    public MetadataFieldFilter(String field, String value) {
    	this();
        setValue(value);
        getOption(MetadataFieldGrouper.OPT_METADATA_FIELD).value().setValue(field);
        onUpdate();
    }

    public boolean canAccept(IMediaResource res) {
        if (res instanceof IMediaFolder) return true;
        if (field==null) return false;
        if (value==null) return false;
        String val = ((IMediaFile)res).getMetadata().get(field);
        if (val==null) return false;
        if (asNumber) {
        	int i1 = NumberUtils.toInt(val);
        	int i2 = NumberUtils.toInt(value);
        	return i1==i2;
        } else {
        	return value.equalsIgnoreCase(val);
        }
    }

    /**
     * Sets the media type value for this filter instance.  Filter values can be a number the represents the value
     * of a {@link MediaResourceType} value, such as "1" or "2", etc. Value can also be a String the represents
     * the String name of a {@link MediaResourceType} value, such as, "file", or "HD".  Case does not matter. 
     */
    @Override
    public void onUpdate() {
    	asNumber = getOption(OPT_COMPARE_AS_NUMBER).getBoolean(false);
    	value = getOption(OPT_VALUE).getString(null);
    	String key = getOption(MetadataFieldGrouper.OPT_METADATA_FIELD).getString(null);
    	
    	if (key!=null) {
    		field = sagex.phoenix.metadata.MetadataUtil.getSageProperty(key);
    		if (field==null) {
    			log.warn("Invalid SageTV Metadata Field: " + key);
    		}
        }
    }

	@Override
	public Map<String, String> getOptionList(String id) {
		// TODO: return map of mediatype options
		return super.getOptionList(id);
	}
}
