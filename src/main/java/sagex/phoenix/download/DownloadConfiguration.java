package sagex.phoenix.download;

import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "Download Manager Options", path = "phoenix/downloads", description = "Configuration options for the Phoenix Download Manager")
public class DownloadConfiguration extends GroupProxy {
	@AField(label = "Maximum Retry Count", description = "Maximum number of times a download item will be retried, if an IO error happens during the download")
	private FieldProxy<Integer> maxRetries = new FieldProxy<Integer>(3);

	@AField(label = "Number of Download Threads", description = "Maximum number of concurrent downloads")
	private FieldProxy<Integer> maxDownloadThreads = new FieldProxy<Integer>(2);

	public DownloadConfiguration() {
		super();
		init();
	}

	public int getMaxRetries() {
		return maxRetries.get();
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries.set(maxRetries);
	}

	public int getMaxDownloadThreads() {
		return maxDownloadThreads.get();
	}

	public void setMaxDownloadThreads(int maxDownloadThreads) {
		this.maxDownloadThreads.set(maxDownloadThreads);
	}
}
