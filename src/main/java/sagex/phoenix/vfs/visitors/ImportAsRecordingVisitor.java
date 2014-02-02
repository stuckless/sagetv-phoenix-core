package sagex.phoenix.vfs.visitors;

import sagex.phoenix.Phoenix;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Will Import each visited file as a Recording (or remove it from the
 * recordings)
 * 
 * @author seans
 * 
 */
public class ImportAsRecordingVisitor extends FileVisitor {
	private boolean importAsRecording = false;

	public ImportAsRecordingVisitor(Boolean importAsRecording) {
		this.importAsRecording = importAsRecording;
	}

	@Override
	public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
		log.info("Visiting File: " + res.getTitle() + "; Importing: " + importAsRecording);
		if (importAsRecording) {
			if (Phoenix.getInstance().getMetadataManager().importMediaFileAsRecording(res)) {
				incrementAffected();
			} else {
				log.warn("Failed to Import " + res.getTitle() + " as recording.");
			}
		} else {
			if (Phoenix.getInstance().getMetadataManager().unimportMediaFileAsRecording(res) != null) {
				incrementAffected();
			} else {
				log.warn("Failed to Un-Import " + res.getTitle() + " as recording.");
			}
		}
		return true;
	}

}
