package sagex.phoenix.download;

import java.io.File;

/**
 * FileMonitor is thread that monitors the size of a file until it reaches a
 * certain size OR a period of time elapses. An Handler is passed that is
 * updated on the File's size. The handler is also notified when the monitor
 * starts, updates, and completes.
 * <p/>
 * FileMonitor can be used to monitor a File Download and act on the file after
 * a certain amount of time has passed or if the File's size has reached a
 * certain size.
 *
 * @author sean
 */
public class FileMonitor implements Runnable {
    public interface Handler {
        public void onMonitorBegin(File file, long curSizeBytes);

        public void onUpdate(File file, long curSizeBytes);

        public void onMonitorComplete(File file, long size);
    }

    private File file = null;
    private long checkInterval = 200;
    private long monitorUntilSize = 1024 * 1024 * 5;
    private long monitorUntilTime;
    private Handler handler;
    private boolean abort = false;

    public FileMonitor(File file, long checkInterval, long untilSize, long untilTime, Handler handler) {
        this.file = file;
        this.checkInterval = checkInterval;
        this.monitorUntilSize = untilSize;
        this.monitorUntilTime = System.currentTimeMillis() + untilTime;
        this.handler = handler;
    }

    @Override
    public void run() {
        handler.onMonitorBegin(file, file.length());
        while (!abort && file.length() < monitorUntilSize && System.currentTimeMillis() < monitorUntilTime) {
            handler.onUpdate(file, file.length());
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                abort();
                // notify other threads
                Thread.currentThread().interrupt();
            }
        }
        handler.onMonitorComplete(file, file.length());
    }

    public void abort() {
        abort = true;
    }

    public boolean isAborted() {
        return abort;
    }
}
