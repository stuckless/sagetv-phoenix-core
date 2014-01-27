package sagex.phoenix.vfs.filters;

import java.util.Map;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaResource;

public class RatingsFilter extends Filter {
    public RatingsFilter() {
    	super();
    	addOption(new ConfigurableOption(OPT_VALUE, "Rated", null, DataType.string, true, ListSelection.single, (String)null));
    }
    
    public boolean canAccept(IMediaResource res) {
    	// TODO: Implement Rating Filter
    	Loggers.LOG.warn("RatingsFilter: Not Implemented.");
        return true;
    }

	@Override
	public Map<String, String> getOptionList(String id) {
		// TODO: Create Rating Option Map
		return super.getOptionList(id);
	}
}
