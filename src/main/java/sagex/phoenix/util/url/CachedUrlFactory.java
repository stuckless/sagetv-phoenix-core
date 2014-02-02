package sagex.phoenix.util.url;

import java.io.IOException;

import org.apache.log4j.Logger;

public class CachedUrlFactory implements IUrlFactory {
	private static final Logger log = Logger.getLogger(CachedUrlFactory.class);

	public CachedUrlFactory() {
		log.info("Caching URL Factory in use.");
	}

	public IUrl createUrl(String url) throws IOException {
		return new CachedUrl(url);
	}

}
