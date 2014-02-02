package sagex.phoenix.upnp;

import java.io.File;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.util.FileUtils;
import sagex.phoenix.util.url.UrlUtil;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.util.PathUtils;
import sagex.phoenix.vfs.views.ViewFolder;

public class OnlineVideosLinkCreator {
	private boolean fetchMetadata = false;

	public OnlineVideosLinkCreator(boolean fetchMetadata) {
		this.fetchMetadata = fetchMetadata;
	}

	public void makeOfflineLinks(IMediaFolder folder, File destDir) {
		processFolder(folder, destDir);
	}

	private void processItem(IMediaResource r, File destDir) {
		if (r instanceof IMediaFolder) {
			processFolder((IMediaFolder) r, destDir);
		} else {
			processFile((IMediaFile) r, destDir);
		}
	}

	private void processFile(IMediaFile file, File destDir) {
		String phoenixUrl = "phoenix://";
		if (file.getParent() instanceof ViewFolder) {
			phoenixUrl += ((ViewFolder) file.getParent()).getViewFactory().getName();
		} else {
			phoenixUrl += "#";
		}
		phoenixUrl += PathUtils.getPathAsUrl(file);
		if (file.isType(MediaResourceType.ONLINE.value())) {
			phoenixUrl += ("?url=" + UrlUtil.encode(file.getMetadata().getMediaUrl()));
		}
		FileUtils.mkdirsQuietly(destDir);

		String filename = null;
		if (isTV(file)) {
			filename = createTVFilename(file);
		} else {
			filename = createMoviename(file);
		}

		File outFile = new File(destDir, createFilename(filename) + ".mp4");
		System.out.println("Creating: " + outFile.getAbsolutePath());
		try {
			if (outFile.exists())
				return;

			if (!outFile.createNewFile()) {
				throw new Exception("Can't create file");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createMoviename(IMediaFile file) {
		String title = null;
		IMetadata md = file.getMetadata();
		if (md.getYear() > 0) {
			title = String.format("%s (%s)", file.getTitle(), md.getYear());
		} else {
			title = file.getTitle();
		}
		return title;
	}

	private String createTVFilename(IMediaFile file) {
		String title = null;
		IMetadata md = file.getMetadata();
		if (md.getEpisodeNumber() > 0) {
			title = String.format("%s S%02dE%02d %s", file.getTitle(), md.getSeasonNumber(), md.getEpisodeNumber(),
					md.getEpisodeName());
		} else {
			title = String.format("%s -- %s", file.getTitle(), md.getEpisodeName());
		}
		return title;
	}

	private boolean isTV(IMediaFile file) {
		if (file.getMetadata().getEpisodeNumber() > 0) {
			return true;
		}

		if (file.getParent() != null) {
			return file.getParent().getTitle().toLowerCase().startsWith("season");
		}

		return false;
	}

	private void processFolder(IMediaFolder folder, File destDir) {
		destDir = new File(destDir, createFilename(folder.getTitle()));
		for (IMediaResource r : folder) {
			processItem(r, destDir);
		}
	}

	private String createFilename(String title) {
		// TODO: FS Escape
		return title;
	}
}
