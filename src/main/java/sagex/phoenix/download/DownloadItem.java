package sagex.phoenix.download;

import java.io.File;
import java.net.URL;

public class DownloadItem {
    public enum State {
        WAITING, DOWNLOADING, COMPLETE, ERROR
    }

    private long id = System.nanoTime();
    private File localFile = null;
    private URL remoteURL = null;
    private long bytesDownloaded = 0;
    private long totalBytes = 0;
    private String referrer = null;
    private String userAgent = null;
    private State state = State.WAITING;
    private DownloadHandler handler;
    private boolean overwrite = false;

    private int retries = 0;
    private int maxReties = 1;
    private Throwable error;

    private Object userObject;
    private int timeout;

    public DownloadItem() {
    }

    public DownloadItem(URL source, File dest) {
        this.remoteURL = source;
        this.localFile = dest;
    }

    public long getId() {
        return id;
    }

    public File getLocalFile() {
        return localFile;
    }

    public void setLocalFile(File localFile) {
        this.localFile = localFile;
    }

    public URL getRemoteURL() {
        return remoteURL;
    }

    public void setRemoteURL(URL remoteURL) {
        this.remoteURL = remoteURL;
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    public void setBytesDownloaded(long bytesDownloaded) {
        this.bytesDownloaded = bytesDownloaded;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public DownloadHandler getHandler() {
        return handler;
    }

    public void setHandler(DownloadHandler handler) {
        this.handler = handler;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserAgent() {
        return userAgent;
    }

    @Override
    public String toString() {
        return "DownloadItem [bytesDownloaded=" + bytesDownloaded + ", " + (error != null ? "error=" + error + ", " : "")
                + (handler != null ? "handler=" + handler + ", " : "") + "id=" + id + ", "
                + (localFile != null ? "localFile=" + localFile + ", " : "") + "maxReties=" + maxReties + ", "
                + (referrer != null ? "referrer=" + referrer + ", " : "")
                + (remoteURL != null ? "remoteURL=" + remoteURL + ", " : "") + "retries=" + retries + ", "
                + (state != null ? "state=" + state + ", " : "") + "totalBytes=" + totalBytes + ", "
                + (userAgent != null ? "userAgent=" + userAgent : "") + "]";
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getMaxReties() {
        return maxReties;
    }

    public void setMaxReties(int maxReties) {
        this.maxReties = maxReties;
    }

    public void setError(Throwable error) {
        setState(State.ERROR);
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    public int incrementRetries() {
        return ++retries;
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    public Object getUserObject() {
        return userObject;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }
}
