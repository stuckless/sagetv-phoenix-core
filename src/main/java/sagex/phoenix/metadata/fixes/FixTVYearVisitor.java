package sagex.phoenix.metadata.fixes;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.ISeriesInfo;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.visitors.FileVisitor;

/**
 * Clears the Year from TV Show Metadata, since the Year should come from the
 * {@link ISeriesInfo}.
 * 
 * @author sean
 */
public class FixTVYearVisitor extends FileVisitor {
	private static SageProperty year = MetadataUtil.getSageProperty("Year");

	public FixTVYearVisitor() {
	}

	@Override
	public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
		if (res.isType(MediaResourceType.TV.value())) {
			// Remove the Year metadata on TV shows
			if (!StringUtils.isEmpty(res.getMetadata().get(year))) {
				monitor.setTaskName("Fixing Year on for " + res.getTitle() + " " + res.getMetadata().getEpisodeName());
				res.getMetadata().clear(year);
				incrementAffected();
			}
		}
		return true;
	}

	public static boolean fixTVYear(IMediaFile res, IMetadata md) {
		boolean updated = false;
		if (res.isType(MediaResourceType.TV.value())) {
			// Remove the Year metadata on TV shows
			if (!StringUtils.isEmpty(res.getMetadata().get(year))) {
				res.getMetadata().clear(year);
				updated = true;
			}
		}
		return updated;
	}
}
