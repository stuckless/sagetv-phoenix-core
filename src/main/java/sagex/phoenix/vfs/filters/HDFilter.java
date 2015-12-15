package sagex.phoenix.vfs.filters;

import sagex.api.AiringAPI;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;

public class HDFilter extends Filter {
    public HDFilter() {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "HD", "true", DataType.string, true, ListSelection.single, "true:Yes,false:No"));
    }

    public boolean canAccept(IMediaResource res) {
        if (res instanceof IMediaFolder)
            return true;
        if (res instanceof IMediaFile) {
            boolean hd = getOption(OPT_VALUE).getBoolean(true);
            return (AiringAPI.IsAiringHDTV(res.getMediaObject()) == hd);
        } else {
            return false;
        }
    }
}
