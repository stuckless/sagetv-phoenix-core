package sagex.phoenix.vfs.ov.youtube;

import java.io.IOException;
import java.util.List;

import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.ov.IUrlResolver;

public class YoutubeUrlResolver implements IUrlResolver {
	public YoutubeUrlResolver() {
	}

	@Override
	public boolean canAccept(String url) {
		return (url != null && url.toLowerCase().contains("youtube.com"));
	}

	@Override
	public String getUrl(String url) {
		List<String> urls;
		try {
			urls = YoutubeUtil.getYoutubeVideoURLs(url);
			if (urls != null && urls.size() > 0) {
				return urls.get(0);
			}
		} catch (IOException e) {
			Loggers.LOG.warn("Failed to get Youtube Url for " + url);
		}
		return null;
	}
}
