package sagex.phoenix.vfs;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.Hints;

/**
 * Abstract Media Resource. Most VFS items should descend from this class just
 * to take advantage of some of the boilerplate implementation.
 * 
 * @author seans
 * 
 */
public abstract class AbstractMediaResource implements IMediaResource {
	protected Logger log = Logger.getLogger(this.getClass());
	protected String id;
	private Object resource;
	private IMediaFolder parent;
	private String title;
	private long lastModified = System.currentTimeMillis();
	private Object thumbnail;
	private long startTime, endTime;

	protected enum Flag {
		Watched, Library, DontLike, Favorite, ManualRecord, Archived
	}

	private Map<Flag, Boolean> flags = new EnumMap<Flag, Boolean>(Flag.class);

	/**
	 * Creates a new unnamed media resource with the given parent and unique id.
	 * 
	 * @param parent
	 *            Optional parent that owns this resource.
	 * @param id
	 *            unique id for the resource. Cannot be null.
	 */
	public AbstractMediaResource(IMediaFolder parent, String id) {
		this(parent, id, null, null);
	}

	/**
	 * Creates a fully resolved media resource that includes a valid id and
	 * title. If id is null, then you must ensure that createId(resource) return
	 * a unique id for this resource. Ids do not need to be unique across the
	 * system, but they should be unique for a given parent. ie, if the id or 2
	 * objects for the same parent are equal, then the resources are considered
	 * to be the same resource.
	 * 
	 * @param parent
	 *            Optional parent for this resource.
	 * @param id
	 *            unique id for this resource.
	 * @param resource
	 * @param title
	 */
	public AbstractMediaResource(IMediaFolder parent, String id, Object resource, String title) {
		this.parent = parent;
		this.id = id;
		this.resource = resource;
		this.title = title;

		// ID and RESOURCE can never be null. If they are, then we
		// need to create tmp values for them.
		if (this.id == null) {
			this.id = createId(resource);
		}

		if (this.id == null) {
			log.warn("Creating TMP id for resource: " + resource + "; consider adding fixed ID.");
			this.id = createTemporayId();
		}

		if (this.id != null) {
			// fix ids, they cannot how / in them
			this.id = this.id.replace('/', '\\');
		}

		if (resource == null)
			this.resource = this.id;
		if (title == null)
			this.title = this.id;
		setTitle(title);
	}

	protected String createTemporayId() {
		if (resource != null) {
			return "TMP" + resource.hashCode();
		} else {
			return "TMP" + hashCode();
		}
	}

	protected boolean getFlag(Flag flag) {
		Boolean b = flags.get(flag);
		if (b == null) {
			return false;
		}
		return b;
	}

	public void setFlag(Flag flag, boolean val) {
		flags.put(flag, val);
	}

	public boolean isFlagSet(Flag flag) {
		return flags.containsKey(flag);
	}

	public void setTitle(String title) {
		if (title == null)
			title = id;
		this.title = title;
	}

	/**
	 * sub classes can override this method to create ids based on the backing
	 * resource
	 * 
	 * @param resource
	 * @return
	 */
	protected String createId(Object resource) {
		return null;
	}

	@Override
	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	@Override
	public Object getMediaObject() {
		return resource;
	}

	protected void setMediaObject(Object resource) {
		this.resource = resource;
	}

	@Override
	public IMediaFolder getParent() {
		return parent;
	}

	@Override
	public void accept(IMediaResourceVisitor visitor, IProgressMonitor monitor, int deep) {
		if (monitor != null && monitor.isCancelled())
			return;
		if (deep >= 0) {
			visitor.visit(this, monitor);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + getClass().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMediaResource other = (AbstractMediaResource) obj;

		if (id != null) {
			return id.equals(other.id);
		}

		return false;
	}

	@Override
	public int compareTo(IMediaResource o) {
		if (this.equals(o))
			return 0;
		return id.compareTo(o.getId());
	}

	public Object getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(Object thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String toString() {
		return getClass().getName() + " [Id: " + id + ", Title: " + title + "]";
	}

	@Override
	public boolean delete(Hints hints) {
		log.info("delete() not implemented for " + this);
		return false;
	}

	@Override
	public boolean exists() {
		// assume a resource exists if it has a valid id
		return !id.startsWith("TMP");
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean isDontLike() {
		return getFlag(Flag.DontLike);
	}

	@Override
	public boolean isFavorite() {
		return getFlag(Flag.Favorite);
	}

	@Override
	public boolean isLibraryFile() {
		return getFlag(Flag.Library);
	}

	public void setLibraryFile(boolean library) {
		setFlag(Flag.Library, library);
	}

	@Override
	public boolean isType(int type) {
		return false;
	}

	@Override
	public boolean isWatched() {
		return getFlag(Flag.Watched);
	}

	@Override
	public long lastModified() {
		return lastModified;
	}

	@Override
	public void setDontLike(boolean like) {
		setFlag(Flag.DontLike, like);
	}

	@Override
	public void setManualRecord(boolean manual) {
		setFlag(Flag.ManualRecord, manual);
	}

	@Override
	public boolean isManualRecord() {
		return getFlag(Flag.ManualRecord);
	}

	@Override
	public void setWatched(boolean watched) {
		setFlag(Flag.Watched, watched);
	}

	@Override
	public void touch(long time) {
		this.lastModified = time;
	}

	public boolean isArchived() {
		return getFlag(Flag.Archived);
	}

	public void setArchived(boolean arch) {
		setFlag(Flag.Archived, arch);
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getPath() {
		return (getParent() == null ? "" : getParent().getPath()) + "/" + getTitle();
	}

}
