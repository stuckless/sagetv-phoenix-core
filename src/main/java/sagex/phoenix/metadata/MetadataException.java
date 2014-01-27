package sagex.phoenix.metadata;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;

import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.vfs.IMediaFile;

/**
 * Exception to represent something went wrong with Metadata, either searching
 * ,updating, persisting, etc.
 * 
 * if the event that the {@link SearchQuery}, {@link IMediaFile}, and
 * {@link IMetadata} are known, then the exception should contain those objects.
 * 
 * @author seans
 */
public class MetadataException extends Exception {
	private static final long serialVersionUID = 1L;

	private SearchQuery query = null;

	// these are transient so that gwt doesn't try to an serialize them.
	private transient IMediaFile file = null;
	private transient IMetadata metadata = null;
	private transient IMetadataSearchResult result = null;

	private boolean canRetry = false;

	public MetadataException(String msg) {
		super(msg);
	}

	public MetadataException(Throwable ex) {
		super(ex);
		updateRetry(ex);
	}

	public MetadataException(String msg, Throwable ex) {
		super(msg, ex);
		updateRetry(ex);
	}

	public MetadataException(String msg, SearchQuery query, IMediaFile file,
			IMetadata md, Throwable ex) {
		super(msg, ex);
		this.query = query;
		this.file = file;
		this.metadata = md;
		updateRetry(ex);
	}

	public MetadataException(String msg, SearchQuery query, IMediaFile file,
			Throwable ex) {
		super(msg, ex);
		this.query = query;
		this.file = file;
		updateRetry(ex);
	}

	public MetadataException(String msg, IMediaFile file, IMetadata md) {
		super(msg);
		this.file = file;
		this.metadata = md;
	}

	public MetadataException(String msg, SearchQuery query) {
		super(msg);
		this.query = query;
	}

	public MetadataException(String msg, IMetadataSearchResult result) {
		super(msg);
		this.result = result;
	}

	public MetadataException(String string, IMediaFile file2, IMetadata md,
			Throwable e) {
		super(string, e);
		this.file = file2;
		this.metadata = md;
		updateRetry(e);
	}

	public MetadataException(String string, IMetadataSearchResult result2,
			Throwable e) {
		super(string, e);
		this.result = result2;
		updateRetry(e);
	}

	public MetadataException(String string, SearchQuery query2, Throwable e) {
		super(string, e);
		this.query = query2;
		updateRetry(e);
	}

	private void updateRetry(Throwable e) {
		// retry if connection exception or socket exception or
		// if it's a metadata exception, and we can retry
		if (e != null
				&& (e instanceof ConnectException
					||  e instanceof SocketTimeoutException 
					|| (e instanceof MetadataException 
						&& ((MetadataException) e).canRetry()))
					|| (ExceptionUtils.indexOfType(e, ConnectException.class)!=-1)
					|| (ExceptionUtils.indexOfType(e, SocketTimeoutException.class)!=-1)
						) {
			canRetry = true;
		}
	}

	public SearchQuery getQuery() {
		return query;
	}

	public IMediaFile getFile() {
		return file;
	}

	public IMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetadataException [");
		if (file != null) {
			builder.append("file=");
			builder.append(file);
			builder.append(", ");
		}
		if (metadata != null) {
			builder.append("metadata=");
			builder.append(metadata);
			builder.append(", ");
		}
		if (query != null) {
			builder.append("query=");
			builder.append(query);
			builder.append(", ");
		}
		if (result != null) {
			builder.append("result=");
			builder.append(result);
		}
		builder.append("]");
		return builder.toString();
	}

	public boolean canRetry() {
		return canRetry;
	}
}
