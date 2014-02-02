package sagex.phoenix.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import sagex.api.Configuration;
import sagex.api.MediaFileAPI;
import sagex.phoenix.fanart.SageFanartUtil;
import sagex.phoenix.fanart.SimpleMediaFile;
import sagex.phoenix.metadata.MediaType;

public class PhoenixDiagnostics {
	public static class Timer {
		private long start = 0;

		public Timer() {
			this.start = System.currentTimeMillis();
		}

		public long timeDiff() {
			return System.currentTimeMillis() - start;
		}

		public void reset() {
			start = System.currentTimeMillis();
		}
	}

	private static final int WRAP = 75;
	private List<String> summary = new LinkedList<String>();
	private boolean missingTVMetadata = false;
	private boolean missingMovieMetadata = false;
	private Thread reportThread = null;

	public PhoenixDiagnostics() {
	}

	private void line(PrintWriter pw, String label, Object data) {
		pw.println(String.format("%30s: %s", label, data));
	}

	private void line(PrintWriter pw, String text) {
		pw.println(WordUtils.wrap(text, WRAP));
	}

	private void linefnw(PrintWriter pw, String text, Object... args) {
		pw.println(String.format(text, args));
	}

	private void linef(PrintWriter pw, String text, Object... args) {
		line(pw, String.format(text, args));
	}

	private void h1(PrintWriter pw, String title) {
		line(pw, StringUtils.center(title, WRAP));
		pw.println();
	}

	private void h2(PrintWriter pw, String title) {
		pw.println();
		line(pw, StringUtils.center("=== " + title + " ===", WRAP));
	}

	private void h3(PrintWriter pw, String title) {
		pw.println();
		line(pw, StringUtils.center("--- " + title + " ---", WRAP));
	}

	private void nl(PrintWriter pw) {
		pw.println("\n");
	}

	public synchronized String report(String reportType) {
		if (reportThread != null && reportThread.isAlive()) {
			return "Report already running.";
		}

		if ("quick".equals(reportType)) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			printReport(pw, true);
			pw.flush();
			return sw.toString();
		}

		if ("detailed".equals(reportType)) {
			try {
				File f = new File("phoenix-diagnostics.txt");
				final PrintWriter pw = new PrintWriter(new FileWriter(f));
				reportThread = new Thread() {
					@Override
					public void run() {
						printReport(pw, false);
					}
				};
				reportThread.start();
				return "Report is now running.  It may take some time to complete.  Your report is located at "
						+ f.getAbsolutePath();
			} catch (Exception e) {
				return "Unable to create report.";
			}
		}

		Object mf = MediaFileAPI.GetMediaFileForFilePath(new File(reportType));
		if (mf == null) {
			return "Invalid report type/mediafile: " + reportType;
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		printSimpleMediaFileReport(pw, mf);
		pw.flush();
		return sw.toString();
	}

	private void printSimpleMediaFileReport(PrintWriter pw, Object mf) {
		printCommonReport(pw);
		printReportSingleMediaFile(pw, mf);
	}

	public void printReport(PrintWriter pw, boolean quick) {
		try {
			Timer time = new Timer();
			printCommonReport(pw);

			h2(pw, "Java Environment Settings");
			for (Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				// don't show the sage user key
				if ("USERKEY".equals(key))
					continue;

				line(pw, key, System.getProperty(key));
			}

			if (!quick) {
				reportMediaGroup(pw, "Recorded TV", MediaFileAPI.GetMediaFiles("T"));
				nl(pw);
				reportMediaGroup(pw, "Videos", MediaFileAPI.GetMediaFiles("V"));
				nl(pw);
				reportMediaGroup(pw, "DVD/Blu-ray", MediaFileAPI.GetMediaFiles("BD"));
				nl(pw);
			}

			nl(pw);

			h2(pw, "Summary");
			for (String s : summary) {
				line(pw, s);
			}
			nl(pw);

			line(pw, "Report Complete");
			linef(pw, "Total Report Time (ms): %s", time.timeDiff());
			pw.flush();
		} finally {
			// we are intentionally closing the writer, since we could have been
			// in a thread
			pw.flush();
			pw.close();
		}
	}

	private void printCommonReport(PrintWriter pw) {
		h1(pw,
				String.format("Phoenix Diagnostic Report - Date: %s",
						DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())));

		h2(pw, "Version Checks");
		line(pw, "Phoenix Version", phoenix.api.GetVersion());
		line(pw, "Sagex.api Version", sagex.api.Version.GetVersion());

		// check sagex api version
		if (!phoenix.api.IsAtLeastVersion(sagex.api.Version.GetVersion(), phoenix.api.GetRequiredSagexApiVersion())) {
			linef(pw, "ERROR: The required sagex.api.jar version should be %s and yours is %s",
					phoenix.api.GetRequiredSagexApiVersion(), sagex.api.Version.GetVersion());
			linef(pw, "Using an incorrect sagex.api.jar version may prevent prevent Phoenix from using the custom metadata fields.");
			nl(pw);
			summary.add("Not Running Correct sagex.api.jar version.");
		}

		// check sage version
		line(pw, "Sage Version", SageTV.getSageVersion());
		if (!phoenix.api.IsAtLeastVersion(SageTV.getSageVersion(), phoenix.api.GetRequiredSageVersion())) {
			linef(pw, "ERROR: The required SageTV version should be %s and yours is %s", phoenix.api.GetRequiredSageVersion(),
					SageTV.getSageVersion());
			linef(pw, "Fanart may not work correctly with this SageTV version, since it does not support GetMediaFileMetadata().");
			summary.add("Not Running Correct SageTV server version.");
			nl(pw);
		}

		h2(pw, "Metadata/Fanart Configuration");
		// test if extra fields are installed, especially MediaTitle,
		// MediaType, SeasonNumber
		String fields = Configuration.GetServerProperty("custom_metadata_properties", null);
		line(pw, "Custom Metadata Fields", ((fields == null) ? "Not Installed" : fields));
		if (fields == null) {
			line(pw, "ERROR: Missing Custom Metadata Fields.");
			line(pw,
					"Phoenix requires the custom_metadata_properties to be set on the Sage Server so that imported videos/tv will retain important metadata fields, such as Media Title, Season Number and Media Type.  These metadata fields are essential in order for Phoenix to locate the propper metadata for a media item.");
			line(pw, "Sage Server requires the following property to be set");
			line(pw,
					"custom_metadata_properties=MediaProviderDataID;MediaTitle;MediaType;OriginalAirDate;EpisodeTitle;EpisodeNumber;SeasonNumber;DiscNumber;UserRating");
			summary.add("Missing custom_metadata_properties");
			nl(pw);
		}

		// test if metadata plugin installed on server
		String plugins = Configuration.GetServerProperty("mediafile_metadata_parser_plugins", null);
		line(pw, "MediaFile Metadata Plugins", plugins);
		boolean isMDPluginEnabled = plugins != null && plugins.indexOf("org.jdna.sage.MetadataUpdaterPlugin") != -1;
		if (!isMDPluginEnabled) {
			line(pw, "WARN: Automatic Metadata Updating is not installed.");
			line(pw,
					"If you want to have metadata fetched when you import new media into your library, then add the following line to your Server Sage.properties.");
			line(pw, "mediafile_metadata_parser_plugins=org.jdna.sage.MetadataUpdaterPlugin");
			nl(pw);
		}

		// test if metadata tools installed
		// line(pw, "Manual Metadata Lookups Enabled",
		// phoenix.api.IsMetadataProviderSupportEnabled());

		if (SageFanartUtil.isParseFileTitleForTVSeriesEnabled()) {
			line(pw, "WARN: phoenix/mediametadata/parseMediaFileForTVSeries is enabled");
			line(pw,
					"Typically this is only needed if your imported media does not have MediaTitle and MediaType.  If everything is configured correctly, and you've rescanned your media, then there should be no reason to enable this.");
			line(pw,
					"Enabling this can also cause a minor performance issue, since it has to run a regular expression on every media file.");
			nl(pw);
		}

		// pheonix settings
		h2(pw, "Phoenix Metadata/Fanart Settings");
		String settings[] = new String[] { "phoenix/mediametadata/fanartEnabled", "phoenix/mediametadata/fanartCentralFolder",
				"phoenix/mediametadata/parseMediaFileForTVSeries", "phoenix/mediametadata/tvSeriesRegex",
				"phoenix/mediametadata/tvSeasonRegex", "phoenix/mediametadata/fanartSupportClass",
				"phoenix/mediametadata/mediaMetadataSupportClass" };
		for (String s : settings) {
			line(pw, s, Configuration.GetProperty(s, null));
		}
		nl(pw);
	}

	private void reportMediaGroup(PrintWriter pw, String title, Object[] mediaFiles) {
		h2(pw, title);
		if (mediaFiles == null || mediaFiles.length == 0) {
			line(pw, "NO MEDIA FILES");
			return;
		}

		line(pw,
				"This is a complete list of the media files in the particular category.  All Fanart about about each media item is listed.\n");
		for (Object item : mediaFiles) {
			printReportSingleMediaFile(pw, item);
		}
	}

	private void printReportSingleMediaFile(PrintWriter pw, Object item) {
		File f = MediaFileAPI.GetFileForSegment(item, 0);
		if (f == null)
			return;

		linefnw(pw, "BEGIN MediaFile %s", f.getAbsolutePath());
		line(pw, "Display Title", MediaFileAPI.GetMediaTitle(item));
		h3(pw, "RAW Metadata Fields");
		line(pw, "MediaTitle", SageFanartUtil.GetMediaFileMetadata(item, "MediaTitle"));
		line(pw, "MediaType", SageFanartUtil.GetMediaFileMetadata(item, "MediaType"));
		line(pw, "SeasonNumber", SageFanartUtil.GetMediaFileMetadata(item, "SeasonNumber"));

		SimpleMediaFile smf = SageFanartUtil.GetSimpleMediaFile(item);
		h3(pw, "Calculated Metadata Fields");
		line(pw, "MediaTitle", smf.getTitle());
		line(pw, "MediaType", smf.getMediaType());
		line(pw, "SeasonNumber", smf.getSeason());

		if (smf.getMediaType() == MediaType.TV) {
			if (StringUtils.isEmpty(SageFanartUtil.GetMediaFileMetadata(item, "MediaTitle"))
					|| StringUtils.isEmpty(SageFanartUtil.GetMediaFileMetadata(item, "MediaType"))
					|| StringUtils.isEmpty(SageFanartUtil.GetMediaFileMetadata(item, "SeasonNumber"))) {
				if (!missingTVMetadata) {
					missingTVMetadata = true;
					summary.add("Some of your TV media items are missing MediaTitle, MediaType, or SeasonNumber");
				}
			}
		}

		if (smf.getMediaType() == MediaType.MOVIE) {
			if (StringUtils.isEmpty(SageFanartUtil.GetMediaFileMetadata(item, "MediaTitle"))
					|| StringUtils.isEmpty(SageFanartUtil.GetMediaFileMetadata(item, "MediaType"))) {
				if (!missingMovieMetadata) {
					missingMovieMetadata = true;
					summary.add("Some of your Movie media items are missing MediaTitle or MediaType");
				}
			}
		}

		h3(pw, "Fanart Paths");
		String posterPath = phoenix.api.GetFanartPosterPath(item);
		line(pw, "Background Path", phoenix.api.GetFanartBackgroundPath(item));
		line(pw, "Banner Path", phoenix.api.GetFanartBannerPath(item));
		line(pw, "Poster Path", posterPath);

		h3(pw, "Fanart Single Files and Times");
		Timer t = new Timer();
		t.reset();
		String back = phoenix.api.GetFanartBackground(item);
		long backTime = t.timeDiff();

		t.reset();
		String banner = phoenix.api.GetFanartBanner(item);
		long bannerTime = t.timeDiff();

		t.reset();
		String poster = phoenix.api.GetFanartPoster(item);
		long posterTime = t.timeDiff();

		line(pw, "Background", back);
		line(pw, "Background Time (ms)", backTime);
		line(pw, "Banner", banner);
		line(pw, "Banner Time (ms)", bannerTime);
		line(pw, "Poster", poster);
		line(pw, "Poster Time (ms)", posterTime);

		linefnw(pw, "END MediaFile %s", f.getAbsolutePath());
		pw.println();

	}
}
