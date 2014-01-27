package sagex.phoenix.homecontrol.themostat.nest;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label="Nest Configuration", path = "phoenix/homecontrol/nest", description = "Nest Thermostat Configuration")
public class NestConfiguration extends GroupProxy {
    @AField(label="Username", description = "Nest username", scope=ConfigScope.SERVER)
    private FieldProxy<String> username = new FieldProxy<String>("");

    @AField(label="Password", description = "Nest Password", scope=ConfigScope.SERVER, type=ConfigType.PASSWORD)
    private FieldProxy<String> password = new FieldProxy<String>("");

    public NestConfiguration() {
        super();
        init();
    }

	public String getUsername() {
		return username.get();
	}

	public void setUsername(String username) {
		this.username.set(username);
	}

	public String getPassword() {
		return password.get();
	}

	public void setPassword(String password) {
		this.password.set(password);
	}
}
