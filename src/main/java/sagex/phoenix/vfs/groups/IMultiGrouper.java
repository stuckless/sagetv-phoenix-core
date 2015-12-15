package sagex.phoenix.vfs.groups;

import sagex.phoenix.vfs.IMediaResource;

import java.util.List;

public interface IMultiGrouper {
    public List<String> getGroupNames(IMediaResource res);
}
