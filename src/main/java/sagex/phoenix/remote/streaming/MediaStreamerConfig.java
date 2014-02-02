package sagex.phoenix.remote.streaming;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.remote.streaming.config.ScriptConfig;
import sagex.phoenix.remote.streaming.config.ServerConfig;
import sagex.phoenix.remote.streaming.config.VLCConfig;

public class MediaStreamerConfig {
	private VLCConfig vlcConfig = null;
	private ScriptConfig scriptConfig = null;
	private ServerConfig serverConfig = null;

	public VLCConfig getVLCConfig() {
		if (vlcConfig == null) {
			vlcConfig = GroupProxy.get(VLCConfig.class);
		}
		return vlcConfig;
	}

	public ScriptConfig getScriptConfig() {
		if (scriptConfig == null) {
			scriptConfig = GroupProxy.get(ScriptConfig.class);
		}
		return scriptConfig;
	}

	public ServerConfig getServerConfig() {
		if (serverConfig == null) {
			serverConfig = GroupProxy.get(ServerConfig.class);
		}
		return serverConfig;
	}

}
