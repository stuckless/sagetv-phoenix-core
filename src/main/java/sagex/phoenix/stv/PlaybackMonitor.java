package sagex.phoenix.stv;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.UIContext;
import sagex.api.AiringAPI;
import sagex.api.Configuration;
import sagex.api.Global;
import sagex.api.MediaFileAPI;
import sagex.api.MediaPlayerAPI;
import sagex.phoenix.vfs.IMediaFile;
import sagex.util.WaitFor;

public class PlaybackMonitor extends Thread {
	private Logger log = Logger.getLogger(this.getClass());

	private UIContext ctx = null;
	private boolean abort = false;
	private IMediaFile file = null;
	private long MinUnderflowWarningTimePlayback;

	public PlaybackMonitor(UIContext ctx, IMediaFile file) {
		this.ctx = ctx;
		this.file = file;
		this.MinUnderflowWarningTimePlayback = NumberUtils.toLong(Configuration.GetProperty(ctx,
				"online_video/min_underflow_warning_playback_near_end", String.valueOf(5 * 1000)));
		setDaemon(true);
	}

	protected boolean isUnderrunning(Object mf) {
		int segments = MediaFileAPI.GetNumberOfSegments(ctx, mf);
		long endTime = MediaFileAPI.GetEndForSegment(ctx, mf, segments - 1) - AiringAPI.GetAiringStartTime(mf);
		long curPlaybackTime = MediaPlayerAPI.GetMediaTime(ctx) - AiringAPI.GetAiringStartTime(mf);
		return (endTime - curPlaybackTime) < MinUnderflowWarningTimePlayback;
	}

	@Override
	public void run() {
		log.info("Playback Monitor Started");
		while (!abort && DownloadUtil.isDownloading(Global.GetFileDownloadStatus(ctx))) {
			try {
				final Object mf = MediaPlayerAPI.GetCurrentMediaFile(ctx);

				// check the download to see that we are keeping pace
				if (MediaPlayerAPI.IsPlaying(ctx)) {
					if (isUnderrunning(mf)) {
						log.info("We are going to run out buffer :(");
						MediaPlayerAPI.Pause(ctx);
						final ProgressDialog dialog = new ProgressDialog(ctx.getName(), "Buffering...");
						dialog.show();
						final long waitTime = 30000;
						final long curTime = System.currentTimeMillis();
						WaitFor wait = new WaitFor() {
							@Override
							public boolean isDoneWaiting() {
								dialog.update((int) (((float) (System.currentTimeMillis() - curTime) / (float) waitTime) * 100));
								return isUnderrunning(mf);
							}
						};
						wait.waitFor(waitTime, 200);

						// just wait a little more just to buffer more
						Thread.sleep(500);

						dialog.close();
						if (!MediaPlayerAPI.IsPlaying()) {
							MediaPlayerAPI.Play(ctx);
						}
					}
				}
				// check every half second
				Thread.sleep(500);
			} catch (Throwable e) {
				log.warn("Error during Playback Monitor!", e);
			}
		}

		log.info("Playback Monitor Finished: Aborted?: " + abort + "; Downloading?: "
				+ DownloadUtil.isDownloading(Global.GetFileDownloadStatus(ctx)));
	}

	public void abort() {
		abort = true;
	}
}
