package sagex.phoenix.vfs.visitors;

import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.persistence.PropertiesPersistence;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;

public class MetadataPropertiesExportVisitor extends FileVisitor {

	public MetadataPropertiesExportVisitor() {
	}

	@Override
	public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
		if (res.isType(MediaResourceType.ANY_VIDEO.value())) {
			monitor.setTaskName(res.getTitle());

			PropertiesPersistence p = new PropertiesPersistence();
			try {
				p.storeMetadata(res, res.getMetadata(), null);
			} catch (MetadataException e) {
				monitor.setTaskName("Failed: " + res.getTitle());
			}

			monitor.worked(1);
		}
		return true;
	}

}
