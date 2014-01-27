package sagex.phoenix.vfs.filters;

import org.apache.log4j.Logger;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.sage.SageMediaFile;

public class FirstRunFilter extends Filter {
    static private final Logger LOG = Logger.getLogger(FirstRunFilter.class);
    public FirstRunFilter(boolean firstrun) {
    	super();
    	addOption(new ConfigurableOption(OPT_VALUE, "FirstRun", "true", DataType.string, true, ListSelection.single, "true:Yes,false:No"));
    	setValue(String.valueOf(firstrun));
    }
    
    public FirstRunFilter() {
    	this(true);
    }
    
    public boolean canAccept(IMediaResource res) {
        boolean firstrun = getOption(OPT_VALUE).getBoolean(true); 
        if (res instanceof IMediaFolder){
            //LOG.info("canAccept: IMediaFolder found so returning TRUE - OPT_VALUE = '" + firstrun + "'");
            return true;
        }
        if (res instanceof SageMediaFile) {
            //LOG.info("canAccept: SageMediaFile found so returning '" + ((SageMediaFile)res).isShowFirstRun() == firstrun + "' - OPT_VALUE = '" + firstrun + "'");
            return ((SageMediaFile)res).isShowFirstRun() == firstrun;
        }else if(res instanceof DecoratedMediaFile){
            //LOG.info("canAccept: DecoratedMediaFile found - OPT_VALUE = '" + firstrun + "' res '" + res + "'");
            return canAccept(((DecoratedMediaFile) res).getDecoratedItem());
        } else {
            //LOG.info("canAccept: default returning FALSE - OPT_VALUE = '" + firstrun + "' res '" + res + "'");
            return false;
        }
    }
}
