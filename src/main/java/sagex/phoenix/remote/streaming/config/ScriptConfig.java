package sagex.phoenix.remote.streaming.config;

import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "Script Options", path = "phoenix/streaming/script", description = "Script Streaming Options for Phoenix")
public class ScriptConfig extends GroupProxy {
	@AField(label = "Command", description = "Full Path to script executable that is used for creating streams")
	private FieldProxy<String> command = new FieldProxy<String>("");

	public ScriptConfig() {
		super();
		init();
	}

	public String getCommand() {
		return command.get();
	}

	public void setCommand(String command) {
		this.command.set(command);
	}
}
