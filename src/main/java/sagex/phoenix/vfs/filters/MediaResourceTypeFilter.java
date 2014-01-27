package sagex.phoenix.vfs.filters;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * Filters media based on the Types listed in the {@link MediaResourceType}
 * class
 * 
 * the value can either be number of a string, ie, "1" or "FILE", or "file", or
 * "File". Multiple values can be specified a comma separated list. It's behaves
 * as an OR if more than 1 values is passed.
 * 
 * @author seans
 * 
 */
public class MediaResourceTypeFilter extends Filter {
	private OrResourceFilter filters = new OrResourceFilter();

	public MediaResourceTypeFilter() {
		super();
		addOption(new ConfigurableOption(OPT_VALUE, "Resource Type", null, DataType.string, true, ListSelection.multi, (String)null));
	}

	public MediaResourceTypeFilter(String types) {
		this();
		setValue(types);
	}

	public boolean canAccept(IMediaResource res) {
		if (res instanceof IMediaFolder)
			return true;
		return filters.accept(res);
	}

	@Override
	protected void onUpdate() {
		filters.clear();
		String value = getOption(OPT_VALUE).getString(null);
		for (String s : value.split("\\s*,\\s*")) {
			MediaResourceType type = MediaResourceType.toMediaResourceType(s);
			if (type != null) {
				filters.addFilter(new MediaTypeFilter(type));
			} else {
				log.warn("Invalid MediaType: " + s + " specified in Filter: " + value);
			}
		}
	}
}
