package sagex.phoenix.vfs.visitors;

import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.persistence.Sage7ThumbnailPersistence;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

public class ExportSageTV7ThumbnailVisitor extends FileVisitor {

	public ExportSageTV7ThumbnailVisitor() {
	}

	@Override
	public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
		if (res.isType(MediaResourceType.ANY_VIDEO.value())) {
			monitor.setTaskName(res.getTitle());
			
			Sage7ThumbnailPersistence p = new Sage7ThumbnailPersistence();
			try {
				p.storeMetadata(res, res.getMetadata(), null);
			} catch (MetadataException e) {
				log.warn("Failed to create Sage7 Thumbnail for " + res.getTitle(), e);
				monitor.setTaskName("Failed: " + res.getTitle());
			}
			
			monitor.worked(1);
		}
		return true;
	}

}
