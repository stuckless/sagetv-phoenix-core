package sagex.phoenix.vfs.visitors;

import java.util.ArrayList;
import java.util.List;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;
import sagex.phoenix.vfs.MediaResourceType;

/**
 * Collects resources
 * 
 * @author sean
 * 
 */
public class CollectorResourceVisitor implements IMediaResourceVisitor {
	private List<IMediaResource> collected = new ArrayList<IMediaResource>();
	private int collectedSize = 0;

	private int max = Integer.MAX_VALUE;
	private MediaResourceType mediaResourceType = null;

	/**
	 * Creates collector that will collect all files of the given media resource
	 * type
	 * 
	 * @param type
	 */
	public CollectorResourceVisitor(MediaResourceType type) {
		this.mediaResourceType = type;
	}

	/**
	 * Creates collector that collects a max number of items of any type (files,
	 * folders, etc)
	 * 
	 * @param max
	 */
	public CollectorResourceVisitor(int max) {
		this.max = max;
	}

	/**
	 * Creates a collector that collects a max number of items for a given type
	 * 
	 * @param max
	 * @param type
	 */
	public CollectorResourceVisitor(int max, MediaResourceType type) {
		this.max = max;
		this.mediaResourceType = type;
	}

	public boolean visit(IMediaResource resource, IProgressMonitor mon) {
		if (collectedSize >= max)
			return false;

		if (mediaResourceType != null) {
			if (resource.isType(mediaResourceType.value())) {
				collected.add(resource);
				collectedSize++;
			}
		} else {
			collected.add(resource);
			collectedSize++;
		}

		if (collectedSize >= max) {
			if (mon != null) {
				mon.setCancelled(true);
			}
			return false;
		}
		return true;
	}

	public List<IMediaResource> getCollection() {
		return collected;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public MediaResourceType getMediaResourceType() {
		return mediaResourceType;
	}

	public void setMediaResourceType(MediaResourceType mediaResourceType) {
		this.mediaResourceType = mediaResourceType;
	}

}
