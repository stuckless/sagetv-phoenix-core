package sagex.phoenix.vfs.filters;

import sagex.api.AiringAPI;
import sagex.api.ChannelAPI;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class ChannelFilter extends Filter {
    private String channels[] = null;

    public ChannelFilter() {
        super();
        addOption(new ConfigurableOption(OPT_VALUE, "Channel", null, DataType.string));
    }

    @Override
    public boolean canAccept(IMediaResource res) {
        if (channels == null)
            return false;

        if (res instanceof IMediaFile) {
            String ch = ChannelAPI.GetChannelNumber(AiringAPI.GetChannel(res.getMediaObject()));
            if (ch != null) {
                for (String c : channels) {
                    if (ch.equals(c)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void onUpdate() {
        String value = getOption(OPT_VALUE).getString(null);
        if (value == null || value.length() == 0) {
            channels = null;
        } else {
            channels = value.split("\\s*,\\s*");
        }
    }
}
