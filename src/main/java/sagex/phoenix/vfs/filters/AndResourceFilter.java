package sagex.phoenix.vfs.filters;

import sagex.phoenix.vfs.IMediaResource;

/**
 * Only returns true if ALL filters accept the resource
 *
 * @author seans
 */
public class AndResourceFilter extends AbstractResourceFilterContainer {
    public AndResourceFilter() {
        super();
    }

    public AndResourceFilter(IResourceFilter filter) {
        super(filter);
    }

    public boolean accept(IMediaResource resource) {
        if (getFilterCount() == 0)
            return true;

        for (IResourceFilter f : filters) {
            if (!f.accept(resource)) {
                return false;
            }
        }

        return true;
    }
}
