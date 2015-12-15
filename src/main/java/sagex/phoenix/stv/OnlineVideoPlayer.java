package sagex.phoenix.stv;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import sagex.UIContext;
import sagex.api.Global;
import sagex.api.MediaFileAPI;
import sagex.api.MediaPlayerAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.vfs.HasPlayableUrl;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.ov.IUrlResolver;
import sagex.util.WaitFor;

/**
 * Class for managing the playback of Online Videos
 *
 * @author sean
 */
public class OnlineVideoPlayer implements ProgressDialog.CancelHandler {
    private Logger log = Logger.getLogger(this.getClass());
    private UIContext ctx = null;
    private IMediaFile file;
    private ProgressDialog dialog = null;
    private String url = null;
    private File destFile = null;
    private boolean aborted = false;

    private long bufferedWaitTime = 5000;

    private PlaybackMonitor playbackMonitor = null;

    public OnlineVideoPlayer(UIContext ctx, IMediaFile file) {
        this.ctx = ctx;
        this.file = file;
        if (!(file instanceof HasPlayableUrl)) {
            Toaster.toast("Can only play online videos", 1000);
            return;
        }
        url = ((HasPlayableUrl) file).getUrl();

        // NOTE: we need the absolute file... SageTV will fail without it
        this.destFile = Phoenix.getInstance().getUserCacheEntry("onlinevideos", url);
        try {
            this.destFile = this.destFile.getCanonicalFile();
        } catch (IOException e) {
            this.destFile = this.destFile.getAbsoluteFile();
        }

        log.info("Cached file: " + destFile + " for url " + url);
    }

    private boolean watchAndWait() {
        Object watching = MediaPlayerAPI.Watch(ctx, destFile);
        log.info("Is Watching " + file.getTitle() + "? " + watching);

        WaitFor curFileWait = new WaitFor() {
            @Override
            public boolean isDoneWaiting() {
                log.info("Waiting for file to watch...");
                return MediaPlayerAPI.GetCurrentMediaFile(ctx) != null;
            }
        };
        curFileWait.waitFor(20000, 500);
        return MediaPlayerAPI.GetCurrentMediaFile(ctx) != null;
    }

    public boolean play() {
        // register this player
        Phoenix.getInstance().getOnlineVideoPlaybackManager().addPlayer(this);

        dialog = new ProgressDialog(ctx.getName(), "Waiting for Video " + file.getTitle());

        try {
            // show the buffering dialog
            dialog.setHandler(this);
            dialog.show();

            // attempt to resolve the real url, if necessary
            if (Phoenix.getInstance().getOnlineVideosUrlResolverManager().getResolvers().size() > 0) {
                String newUrl = null;
                for (IUrlResolver r : Phoenix.getInstance().getOnlineVideosUrlResolverManager().getResolvers()) {
                    try {
                        if (r.canAccept(url)) {
                            newUrl = r.getUrl(url);
                            if (newUrl != null) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed while processin url resolver: " + r, e);
                    }
                }

                if (newUrl != null) {
                    log.info("Resolved url: " + url + " to " + newUrl);
                    url = newUrl;
                }
            }

            log.info("Starting filedownload for " + url + " to " + destFile);
            if (destFile.exists()) {
                // if we already have the file, the maybe we can just play it.
                if (destFile.length() == file.getMetadata().getDuration()) {
                    return watchAndWait();
                } else {
                    // TODO: check file size and play
                    log.warn("Removing old file (partially downloaded): " + destFile);
                    destFile.delete();
                }
            }

            // in the event that their is a download already, then cancel it.
            Global.CancelFileDownload(ctx);

            // start the download
            if (Global.StartFileDownload(ctx, url, null, destFile)) {
                long waitTimeout = 20000;
                log.info("File is downloading...");
                WaitFor wait = new WaitFor() {
                    @Override
                    public boolean isDoneWaiting() {
                        if (aborted)
                            return true;
                        long time = Global.GetFileDownloadStreamTime(ctx);
                        if (time > 0) {
                            if (time > bufferedWaitTime) {
                                dialog.update(100);
                            } else {
                                dialog.update("Buffering Video " + file.getTitle(),
                                        (int) (((float) time / (float) bufferedWaitTime) * 100));
                            }
                        }
                        log.info("Stream Time: " + Global.GetFileDownloadStreamTime(ctx));
                        return Global.GetFileDownloadStreamTime(ctx) > bufferedWaitTime;
                    }
                };
                wait.waitFor(waitTimeout, 500);

                // check if we aborted
                if (aborted) {
                    destroy();
                    dialog.close();
                    Toaster.toast("Playback aborted for " + file.getTitle(), 1000);
                    return false;
                }

                // check that we have a stream
                long time = Global.GetFileDownloadStreamTime(ctx);
                if (time == 0) {
                    destroy();
                    dialog.close();
                    Toaster.toast("Failed to get Online Video for " + file.getTitle() + " in " + waitTimeout + "ms", 1500);
                    return false;
                }

                dialog.update("Playing Video " + file.getTitle(), 100);
                // calls watch and waits for current media file to start
                if (!watchAndWait()) {
                    destroy();
                    return false;
                }

                log.info("Watching file... Adding Show...");
                Object mf = MediaPlayerAPI.GetCurrentMediaFile(ctx);
                long duration = file.getMetadata().getDuration();
                // <Action
                // Name="NewShow = AddShow(TitleText, false, TitleText, ShowDesc, NewDur, null, null, null, null, null, null, null, null, null, &quot;TMP&quot; + Time(), null, 0)"
                // Sym="BASE-82840">
                Object show = ShowAPI
                        .AddShow(ctx, file.getTitle(), false, file.getTitle(), file.getMetadata().getDescription(), duration, null,
                                null, null, null, null, null, null, null, null, "TMP" + System.currentTimeMillis(), null, 0);
                MediaFileAPI.SetMediaFileShow(ctx, mf, show);
                log.info("AddShow() called with duration " + duration + " for " + mf);

                // create the playback monitor
                if (DownloadUtil.isDownloading(Global.GetFileDownloadStatus(ctx))) {
                    playbackMonitor = new PlaybackMonitor(ctx, file);
                    playbackMonitor.start();
                }

                return true;
            } else {
                log.info("Failed to download file...");
            }
        } finally {
            dialog.close();
        }
        return false;
    }

    @Override
    public void onCancel() {
        destroy();
    }

    public String getUIContext() {
        return ctx.getName();
    }

    public void destroy() {
        aborted = true;

        // cancel any downloads
        if (DownloadUtil.isDownloading(Global.GetFileDownloadStatus(ctx))) {
            log.info("Cancelling file download of url " + url + " to " + destFile);
            Global.CancelFileDownload(ctx);
        }

        // cancel the background monitoring
        if (playbackMonitor != null) {
            playbackMonitor.abort();
        }

        // remove ourself fromt online players manager
        Phoenix.getInstance().getOnlineVideoPlaybackManager().removePlayer(this);
    }

    public IMediaFile getMediaFile() {
        return file;
    }

    @Override
    public String toString() {
        return "OnlineVideoPlayer [" + (ctx != null ? "ctx=" + ctx.getName() + ", " : "")
                + (file != null ? "file=" + file.getTitle() + ", " : "") + (url != null ? "url=" + url : "") + "]";
    }
}
