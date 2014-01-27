package sagex.phoenix.vfs.filters;

import java.util.Map;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.DecoratedMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.sage.SageMediaFile;

public class UserCategoryFilter extends Filter {
    private String value = null;
    
    public UserCategoryFilter() {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "UserCategory", null, DataType.string, true, ListSelection.single, (String)null));
    }
    
    public boolean canAccept(IMediaResource res) {
        String usercategory = value;
        if (usercategory==null) return false;
        
        if (res instanceof IMediaFolder){
            return true;
        }
        if (res instanceof SageMediaFile) {
            return ((SageMediaFile)res).hasUserCategory(usercategory);
        }else if(res instanceof DecoratedMediaFile){
            return canAccept(((DecoratedMediaFile) res).getDecoratedItem());
        } else {
            return false;
        }
        
    }

	@Override
	public Map<String, String> getOptionList(String id) {
            return super.getOptionList(id);
	}

	@Override
	protected void onUpdate() {
            value = getOption(OPT_VALUE).getString(null);
	}
}
