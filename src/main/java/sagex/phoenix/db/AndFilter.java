package sagex.phoenix.db;

import sagex.phoenix.util.Pair;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.filters.IResourceFilter;

public class AndFilter extends Pair<IResourceFilter, IResourceFilter> implements IResourceFilter {
	public AndFilter() {
		super();
	}

	public AndFilter(IResourceFilter first) {
		super(first, null);
	}

	@Override
	public boolean accept(IMediaResource res) {
		return first().accept(res) && second().accept(res);
	}
	
	public String toString() {
		return "("+first()+" AND "+ second()+")";
	}
}
