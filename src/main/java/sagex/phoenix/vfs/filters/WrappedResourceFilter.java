package sagex.phoenix.vfs.filters;

import sagex.phoenix.vfs.IMediaResource;

/**
 * Wraps a standard {@link IResourceFilter} to make it compatible with a View Filter
 * 
 * @author seans
 *
 */
public class WrappedResourceFilter extends Filter {
	private IResourceFilter filter = null;
	public WrappedResourceFilter(IResourceFilter filter) {
		this.filter=filter;
	}

	@Override
	protected boolean canAccept(IMediaResource res) {
		return filter.accept(res); 
	}
	
	public IResourceFilter getResourceFilter() {
		return filter;
	}
}
