package sagex.phoenix.vfs.filters;

import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.util.ConfigList;

/**
 * Filters media based on the Types listed in the {@link MediaResourceType}
 * class
 * 
 * the value can either be number of a string, ie, "1" or "FILE", or "file", or
 * "File".
 * 
 * @author seans
 * 
 */
public class MediaTypeFilter extends Filter {
	private int type = -1;

	public MediaTypeFilter() {
		super();
		addOption(new ConfigurableOption(OPT_VALUE, "Media Type", null, DataType.string, true, ListSelection.single,
				ConfigList.mediaTypeList()));
	}

	public MediaTypeFilter(MediaResourceType type) {
		this();
		setValue(type.name());
	}

	public boolean canAccept(IMediaResource res) {
		if (res instanceof IMediaFolder)
			return true;
		return res.isType(type);
	}

	/**
	 * Sets the media type value for this filter instance. Filter values can be
	 * a number the represents the value of a {@link MediaResourceType} value,
	 * such as "1" or "2", etc. Value can also be a String the represents the
	 * String name of a {@link MediaResourceType} value, such as, "file", or
	 * "HD". Case does not matter.
	 */
	@Override
	public void onUpdate() {
		String value = getOption(OPT_VALUE).getString(null);
		type = NumberUtils.toInt(value, -1);

		if (type == -1) {
			MediaResourceType rt = MediaResourceType.toMediaResourceType(value);
			if (rt != null) {
				type = rt.value();
			}
		}
	}

	@Override
	public Map<String, String> getOptionList(String id) {
		// TODO: return map of mediatype options
		return super.getOptionList(id);
	}
}
