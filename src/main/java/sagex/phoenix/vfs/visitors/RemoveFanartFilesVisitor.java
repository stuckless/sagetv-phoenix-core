package sagex.phoenix.vfs.visitors;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Physically remove Fanart Files that is associated with each given media file
 * 
 * @author seans
 */
public class RemoveFanartFilesVisitor extends FileVisitor {
	public RemoveFanartFilesVisitor() {
	}

	@Override
	public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
		String sfile = phoenix.fanart.GetFanartArtifactDir(res, (MediaType)null, null, (MediaArtifactType)null, null, null, false);
		if (sfile !=null) {
			File file = new File(sfile);
			try {
				Loggers.METADATA.info("REMOVE FANART: " + file);
				FileUtils.deleteDirectory(file);
				incrementAffected();
			} catch (IOException e) {
				Loggers.LOG.warn("Failed to remove directory: " + file);
			}
		}
		return true;
	}
}
