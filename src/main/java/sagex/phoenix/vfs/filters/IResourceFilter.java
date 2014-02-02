package sagex.phoenix.vfs.filters;

import sagex.phoenix.vfs.IMediaResource;

public interface IResourceFilter {
	public boolean accept(IMediaResource res);
}
