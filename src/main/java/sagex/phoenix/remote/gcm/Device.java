package sagex.phoenix.remote.gcm;

public class Device {
	public Device() {}
	public String id;
	public Device(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	public String name;
	public long lastAccessed = System.currentTimeMillis();
}