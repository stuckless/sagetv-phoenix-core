package sagex.phoenix.util;

import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "Tools Configuration", path = "phoenix/tools", description = "Configuration for various tools, such as mplayer location")
public class ToolsConfiguration extends GroupProxy {
    @AField(label = "mplayer location", description = "Full path to MPlayer", type = ConfigType.FILE)
    private FieldProxy<String> mplayerLocation = new FieldProxy<String>("/usr/bin/mplayer");

    public void setMplayerLocation(String mplayerLocation) {
        this.mplayerLocation.set(mplayerLocation);
    }

    public String getMplayerLocation() {
        return mplayerLocation.get();
    }
}
