package sagex.phoenix.homecontrol.themostat;

import sagex.phoenix.tools.annotation.API;

@API(group = "thermostat", proxy = true)
public interface IDevice {
	public String getId();
	public String getName();
}
