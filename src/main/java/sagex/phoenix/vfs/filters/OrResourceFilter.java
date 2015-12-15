package sagex.phoenix.vfs.filters;

import sagex.phoenix.vfs.IMediaResource;

/**
 * returns true if Any filters accept the resource
 *
 * @author seans
 */
public class OrResourceFilter extends AbstractResourceFilterContainer {
    public OrResourceFilter() {
        super();
    }

    public OrResourceFilter(IResourceFilter filter) {
        super(filter);
    }

    public boolean accept(IMediaResource resource) {
        if (getFilterCount() == 0)
            return true;

        for (IResourceFilter f : filters) {
            if (f.accept(resource)) {
                return true;
            }
        }

        return false;
    }
}
