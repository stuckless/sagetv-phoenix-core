package sagex.phoenix.vfs.visitors;

import java.util.List;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * If a given media file's title matches the search, then it is added to the
 * list.
 * 
 * @author sls
 * 
 */
public class TitleSearchVisitor extends FileVisitor {
	private String titleContains = null;
	private List<IMediaResource> addTo;

	public TitleSearchVisitor(String titleContains, List<IMediaResource> addTo) {
		if (titleContains == null) {
			titleContains = "";
		}

		this.titleContains = titleContains.toLowerCase();
		this.addTo = addTo;
	}

	@Override
	public boolean visitFile(IMediaFile res, IProgressMonitor monitor) {
		String name = res.getTitle();
		if (name != null) {
			name = name.toLowerCase();
			if (name.contains(titleContains)) {
				addTo.add(res);
				return true;
			}
		}

		if (res.isType(MediaResourceType.TV.value())) {
			// check episode name
			IMetadata md = res.getMetadata();
			if (md == null)
				return false;
			name = md.getEpisodeName();
			if (name != null) {
				name = name.toLowerCase();
				if (name.contains(titleContains)) {
					addTo.add(res);
					return true;
				}
			}
		}

		return false;
	}
}
