package sagex.phoenix.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.download.DownloadItem.State;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.url.UrlUtil;
import sagex.util.WaitFor;

/**
 * DownloadManager is used to schedule and download remote items. Each download
 * item can specify how many attempts to use before it will give it. The
 * download manager is a better alternative to just manually downloading the url
 * contents, since it can detect errors and then reschedule the download for
 * later.
 * 
 * @author seans
 * 
 */
public class DownloadManager {
	public static class Status {
		public int threads = 0;
		public int waiting = 0;

		@Override
		public String toString() {
			return "Status [threads=" + threads + ", waiting=" + waiting + "]";
		}
	}

	Logger log = Logger.getLogger(this.getClass());
	private DownloadConfiguration config = GroupProxy.get(DownloadConfiguration.class);
	private AtomicInteger currentThread = new AtomicInteger(0);
	private List<Timer> threads = new ArrayList<Timer>();
	private Queue<DownloadItem> items = new LinkedList<DownloadItem>();

	public DownloadManager() {
	}

	/**
	 * Schedule a download for immediate download. Downloads are placed in a
	 * queue and will be distributed to one of the many download threads that
	 * are available.
	 * 
	 * @param item
	 */
	public void download(DownloadItem item) {
		item.setRetries(0);
		if (item.getMaxReties() == 0) {
			// set default # of retries
			item.setMaxReties(config.getMaxRetries());
		}

		items.add(item);
		reschedule(item);
	}

	void fail(DownloadItem item, Throwable t) {
		log.warn("Failed to download item: " + item, t);
		item.setError(t);
		items.remove(item);
		if (item.getHandler() != null) {
			item.getHandler().onError(item);
		}

		// remove the local file, because it didn't work
		FileUtils.deleteQuietly(item.getLocalFile());
	}

	void reschedule(DownloadItem item) {
		if (item.getLocalFile() != null && item.getLocalFile().exists() && item.getLocalFile().length() > 0) {
			log.warn("Skipping " + item.getLocalFile() + " since it already was downloaded before.");
			completed(item);
			return;
		}

		if (item.getLocalFile() != null && item.getLocalFile().exists()) {
			// clean up empty files
			item.getLocalFile().delete();
		}

		int retries = item.incrementRetries();
		if (retries > item.getMaxReties()) {
			fail(item, new Exception("Max Retry has been exceeded: " + retries));
			return;
		}

		Timer timer = getNextTimer();
		long delay = 0;
		if (retries > 1) {
			// delay retries by 1 second
			delay = 1000;
		}
		timer.schedule(new DownloadTask(this, item), delay);
		log.info("Scheduled Download: " + item + " with delay " + delay);
	}

	void completed(DownloadItem item) {
		items.remove(item);
		item.setState(DownloadItem.State.COMPLETE);
		if (item.getHandler() != null) {
			item.getHandler().onComplete(item);
		}
	}

	private synchronized Timer getNextTimer() {
		int pos = currentThread.getAndAdd(1) % config.getMaxDownloadThreads();
		if (pos < threads.size()) {
			return threads.get(pos);
		} else {
			Timer timer = new Timer("Downloader-" + pos, true);
			threads.add(timer);
			return timer;
		}
	}

	/**
	 * returns the current status of the download manager. ie, the number of
	 * running threads, waiting items, etc.
	 * 
	 * @return
	 */
	public Status getStatus() {
		Status stats = new Status();
		stats.threads = threads.size();
		stats.waiting = items.size();
		return stats;
	}

	/**
	 * Downloads the Item and doesn't return util the item is complete. This
	 * does not use a background thread, and it doesn not retry if the download
	 * fails.
	 * 
	 * @param item
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public void downloadAndWait(DownloadItem item) throws IOException {
		try {
			log.info("Downloading: " + item);

			InputStream is = null;
			OutputStream os = null;

			URLConnection conn = UrlUtil.openUrlConnection(item.getRemoteURL(), item.getUserAgent(), item.getReferrer(), 0, true);
			is = conn.getInputStream();

			File out = item.getLocalFile();
			if (out.getParentFile() != null) {
				if (!out.getParentFile().exists()) {
					FileUtils.mkdirsQuietly(out.getParentFile());
				}

				if (!out.getParentFile().exists()) {
					throw new IOException("Could not create destination directory: " + out.getParentFile());
				}

				if (!out.getParentFile().isDirectory()) {
					throw new IOException("Parent directory " + out.getParentFile() + " is not a directory!");
				}
			}

			os = new FileOutputStream(out);
			IOUtils.copyLarge(is, os);
			os.flush();
			IOUtils.closeQuietly(os);

			if (item.getLocalFile().length() == 0) {
				throw new IOException("Download Failed no data for " + item);
			}
		} catch (Throwable t) {
			FileUtils.deleteQuietly(item.getLocalFile());
			throw new IOException("Download Failed for item " + item, t);
		}
	}

	public void downloadAndWait(final DownloadItem item, int expireMS) throws IOException {
		// queue the download
		download(item);

		// wait for failure or completion
		WaitFor w = new WaitFor() {
			@Override
			public boolean isDoneWaiting() {
				return item.getState() == DownloadItem.State.COMPLETE || item.getState() == State.ERROR;
			}
		};
		log.info("Waiting for file to download...");
		w.waitFor(expireMS, 100);
		log.info("Done waiting");
		if (item.getState() != DownloadItem.State.COMPLETE) {
			throw new IOException("Failed to download item " + item + " in " + expireMS + "ms");
		}
	}
}
