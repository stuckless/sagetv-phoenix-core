package sagex.phoenix.vfs.visitors;

import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;
import sagex.phoenix.vfs.filters.IResourceFilter;

/**
 * MediaResourceVisitor decorator that will only call the visitor on items that match the filter.
 * 
 * @author seans
 */
public class FilteredResourceVisitor implements IMediaResourceVisitor {
    private IMediaResourceVisitor visitor;
    private IResourceFilter filter;
    
    public FilteredResourceVisitor(IResourceFilter filter, IMediaResourceVisitor visitor) {
        this.filter=filter;
        this.visitor=visitor;
    }
    
    public boolean visit(IMediaResource resource, IProgressMonitor mon) {
        if (filter.accept(resource)) {
            return visitor.visit(resource, mon);
        }
        return true;
    }

    public IMediaResourceVisitor getVisitor() {
        return visitor;
    }

    public IResourceFilter getFilter() {
        return filter;
    }

    public void setVisitor(IMediaResourceVisitor visitor) {
        this.visitor = visitor;
    }

    public void setFilter(IResourceFilter filter) {
        this.filter = filter;
    }
}
