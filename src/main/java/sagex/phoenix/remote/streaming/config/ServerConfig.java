package sagex.phoenix.remote.streaming.config;

import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label="Server Options", path = "phoenix/streaming/server", description = "Streaming Server Options")
public class ServerConfig extends GroupProxy {
    @AField(label="Temporary Files Location", description = "Temporary Directory where on demand streamed files are created, and later cleaned up.  There should be lots of room for files.")
    private FieldProxy<String> tempdir = new FieldProxy<String>("userdata/Phoenix/streaming/");

    @AField(label="Wait Timeout (Seconds)", description = "How many seconds the Streamer will wait for a file to be created, before giving up.")
    private FieldProxy<Integer> waitTimeout = new FieldProxy<Integer>(10);
    
	public ServerConfig() {
        super();
        init();
	}

	public String getTempDir() {
		return tempdir.get();
	}

	public void setTempDir(String dir) {
		this.tempdir.set(dir);
	}

	public int getWaitTimeout() {
		return waitTimeout.get();
	}

	public void setWaitTimeout(int waitTimeout) {
		this.waitTimeout.set(waitTimeout);
	}
}
