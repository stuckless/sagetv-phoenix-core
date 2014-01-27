package sagex.phoenix.vfs.filters;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple container for Filters
 * 
 * @author seans
 *
 */
public abstract class AbstractResourceFilterContainer implements IResourceFilter, Iterable<IResourceFilter> {
    protected List<IResourceFilter> filters = new LinkedList<IResourceFilter>();
    
    public AbstractResourceFilterContainer() {
    }

    public AbstractResourceFilterContainer(IResourceFilter filter) {
        addFilter(filter);
    }
    
    public void addFilter(IResourceFilter filter) {
        filters.add(filter);
    }
    
    public int getFilterCount() {
        return filters.size();
    }
    
    public void clear() {
        filters.clear();
    }

	@Override
	public Iterator<IResourceFilter> iterator() {
		return filters.iterator();
	}
}
