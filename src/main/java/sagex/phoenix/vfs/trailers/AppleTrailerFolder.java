package sagex.phoenix.vfs.trailers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

public class AppleTrailerFolder extends VirtualMediaFolder {
	private String url = null;

	public AppleTrailerFolder(String title, String url) {
		super(title);
		this.url = url;
	}

	public AppleTrailerFolder(IMediaFolder parent, String title, String url) {
		super(parent, title);
		this.url = url;
	}

	@Override
	protected void populateChildren(List<IMediaResource> children2) {
		InputStream is = null;
		try {
			is = new URL(url).openStream();
			JSONArray ja = new JSONArray(IOUtils.toString(is, "UTF-8"));
			int len = ja.length();
			for (int i = 0; i < len; i++) {
				JSONObject jo = ja.getJSONObject(i);
				if (!StringUtils.isEmpty(jo.optString("title"))) {
					children2.add(new AppleTrailerItem(this, jo));
				} else {
					log.warn("Skipping Trailer Item.  No Title.  JSON: " + jo);
				}
			}
		} catch (MalformedURLException e) {
			log.warn("Invalid URL: " + url, e);
		} catch (IOException e) {
			log.warn("Failed to read URL: " + url, e);
		} catch (JSONException e) {
			log.warn("Failed to parse json for url: " + url, e);
		} catch (Throwable t) {
			log.warn("Programmer Error while processing url: " + url, t);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
