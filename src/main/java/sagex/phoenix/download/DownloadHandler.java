package sagex.phoenix.download;

public interface DownloadHandler {
	public void onStart(DownloadItem item);
	public void onComplete(DownloadItem item);
	public void onError(DownloadItem item);
}
