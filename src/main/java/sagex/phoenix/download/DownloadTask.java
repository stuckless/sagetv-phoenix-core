package sagex.phoenix.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;

import sagex.phoenix.download.DownloadItem.State;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.url.UrlUtil;

public class DownloadTask extends TimerTask {
	private DownloadManager mgr = null;
	private DownloadItem item = null;

	public DownloadTask(DownloadManager manager, DownloadItem item) {
		this.mgr = manager;
		this.item = item;
	}

	@Override
	public void run() {
		InputStream is = null;
		OutputStream os = null;
		try {
			mgr.log.info("Download Start: " + item);

			if (item.getHandler() != null) {
				item.getHandler().onStart(item);
			}

			item.setState(State.DOWNLOADING);
			URLConnection conn = UrlUtil.openUrlConnection(item.getRemoteURL(), item.getUserAgent(), item.getReferrer(),
					item.getTimeout(), true);
			is = conn.getInputStream();

			File out = item.getLocalFile();
			if (out.getParent() != null) {
				if (!out.getParentFile().exists()) {
					FileUtils.mkdirsQuietly(out.getParentFile());
				}

				if (!out.getParentFile().exists()) {
					throw new Exception("Could not create destination directory: " + out.getParentFile());
				}
			}

			os = new FileOutputStream(out);
			IOUtils.copyLarge(is, os);
			os.flush();
			item.setBytesDownloaded(out.length());
			if (item.getLocalFile().length() == 0) {
				mgr.log.warn("Download Completed by the file was emtpy for " + item);
				throw new IOException("Downloaded Empty File for " + item);
			} else {
				mgr.log.info("Download Complete: " + item);
				mgr.completed(item);
			}
		} catch (IOException e) {
			FileUtils.deleteQuietly(item.getLocalFile());
			mgr.log.warn("Failed to download item: " + item + " but it may retry later", e);
			mgr.reschedule(item);
		} catch (Throwable t) {
			mgr.log.warn("Failed to download item: " + item + " for some unknown reason", t);
			FileUtils.deleteQuietly(item.getLocalFile());
			mgr.fail(item, t);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}
}
