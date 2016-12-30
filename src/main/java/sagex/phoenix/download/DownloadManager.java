package sagex.phoenix.download;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.url.UrlUtil;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DownloadManager is used to schedule and download remote items. Each download
 * item can specify how many attempts to use before it will give it. The
 * download manager is a better alternative to just manually downloading the url
 * contents, since it can detect errors and then reschedule the download for
 * later.
 *
 * @author seans
 */
public class DownloadManager {
    Logger log = Logger.getLogger(this.getClass());
    private DownloadConfiguration config = GroupProxy.get(DownloadConfiguration.class);

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(config.getMaxDownloadThreads());

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
        reschedule(item);
    }

    void fail(DownloadItem item, Throwable t) {
        log.warn("Failed to download item: " + item, t);
        item.setError(t);
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

        long delay = 0;
        if (retries > 1) {
            // delay retries by 1 second
            delay = 1000;
        }
        scheduler.schedule(new DownloadTask(this, item), delay, TimeUnit.MILLISECONDS);
        log.info("Scheduled Download: " + item + " with delay " + delay);
    }

    void completed(DownloadItem item) {
        item.setState(DownloadItem.State.COMPLETE);
        if (item.getHandler() != null) {
            item.getHandler().onComplete(item);
        }
    }

    /**
     * Downloads the Item and doesn't return util the item is complete. This
     * does not use a background thread, and it doesn't not retry if the download
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
}
