package sagex.phoenix.homecontrol.themostat.nest;

import sagex.phoenix.homecontrol.themostat.IDevice;

public class NestDevice implements IDevice {
	protected String id, name;

	public NestDevice(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
}
