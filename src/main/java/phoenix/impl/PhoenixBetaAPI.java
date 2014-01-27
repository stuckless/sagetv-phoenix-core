package phoenix.impl;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import sagex.UIContext;
import sagex.api.Global;
import sagex.api.PluginAPI;
import sagex.phoenix.stv.DownloadUtil;
import sagex.phoenix.stv.ProgressDialog;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.TimerUtil;

@API(group = "beta")
public class PhoenixBetaAPI {
	public boolean UpdatePhoenix() {
		final UIContext ctx = UIContext.getCurrentContext();
		Loggers.LOG.info("Checking for phoenix updates on client "
				+ ctx.getName());

		TimerUtil.runOnce(0, new TimerTask() {
			@Override
			public void run() {
				final File toFile = new File("SageTVPluginsDev.xml");
				final String url = "http://sagephoenix.googlecode.com/files/SageTVPluginsDev-2.3.314.xml";
				final ProgressDialog dlg = new ProgressDialog(ctx.getName(), "Checking for updates...");
				dlg.setHandler(new ProgressDialog.CancelHandler() {
					@Override
					public void onCancel() {
						Loggers.LOG.info("Phoenix Check for updates Cancelled.");
					}
				});
				dlg.show();

				try {
					int progress = 10;
					dlg.update("Downloading " + url, progress);
					if (!Global.StartFileDownload(ctx, url, null, toFile)) {
						dlg.update("Failed to download SageTVPluginsDev.xml", 100);
						return;
					}

					boolean error = false;
					while (true) {
						Object status = Global.GetFileDownloadStatus(ctx);
						if (DownloadUtil.isDownloadComplete(status)) {
							dlg.update("Downloaded.", 100);
							break;
						}

						if (DownloadUtil.isDownloadError(status)) {
							dlg.update((String) status, 100);
							error = true;
							break;
						}

						dlg.update((String) status, progress++);
						TimerUtil.sleep(300);
					}

					if (error)
						return;
					Loggers.LOG
							.info("Downloaded new manifest... checking for updates...");
					PluginAPI.RefreshAvailablePlugins(ctx);

					dlg.update("Checking for updated Phoenix plugin...");

					final Object phoenix = PluginAPI.GetAvailablePluginForID("phoenix");
					if (phoenix == null) {
						dlg.update("Error: Missing Phoenix Plugin");
						return;
					}

					if (PluginAPI.IsPluginInstalledSameVersion(ctx, phoenix)) {
						Loggers.LOG.info("No phoenix update required.");
						dlg.update("No Update Required.");
					} else {
						dlg.update("Installing Updated Phoenix Plugin...");
						Timer timer = TimerUtil.scheduleRepeating(500, 300,
								new TimerTask() {
									@Override
									public void run() {
										String msg = PluginAPI.GetPluginProgress(ctx);
										if (!StringUtils.isEmpty(msg)) {
											dlg.update(msg);
										}
									}
								});

						// this better block
						String msg = PluginAPI.InstallPlugin(ctx, phoenix);
						dlg.update("Done Installing Updated Phoenix Plugin: " + msg);
						timer.cancel();
						dlg.update(msg);
					}
				} finally {
					Loggers.LOG.info("Phoenix update complete.");
					//TimerUtil.sleep(1000);
					//dlg.close();
				}
			}
		});

		return true;
	}
}
