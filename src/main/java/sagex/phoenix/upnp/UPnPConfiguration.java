package sagex.phoenix.upnp;

import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label="UPnP Options", path = "phoenix/upnp", description = "Phoenix UPnP configuration options")
public class UPnPConfiguration extends GroupProxy {
    @AField(label="Enable UPnP Server", description="Exposes Phoenix Views over UPnP")
    private FieldProxy<Boolean> serverEnabled = new FieldProxy<Boolean>(false);
    
    @AField(label="Enable UPnP Client", description="Creates VFS Views for UPnP Media Server's on the Network")
    private FieldProxy<Boolean> clientEnabled = new FieldProxy<Boolean>(true);

    public UPnPConfiguration() {
        super();
        init();
    }

	public boolean getServerEnabled() {
		return serverEnabled.get();
	}

	public void setServerEnabled(boolean serverEnabled) {
		this.serverEnabled.set(serverEnabled);
	}

	public boolean getClientEnabled() {
		return clientEnabled.get();
	}

	public void setClientEnabled(boolean clientEnabled) {
		this.clientEnabled.set(clientEnabled);
	}
}
