package sagex.phoenix.vfs.groups;

import java.util.List;

import sagex.phoenix.vfs.IMediaResource;

public interface IMultiGrouper {
	public List<String> getGroupNames(IMediaResource res);
}
