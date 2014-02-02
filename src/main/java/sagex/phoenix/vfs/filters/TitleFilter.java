package sagex.phoenix.vfs.filters;

import sagex.phoenix.vfs.IMediaResource;

public class TitleFilter extends BaseRegexFilter {
	public TitleFilter() {
		super("Title");
	}

	public boolean canAccept(IMediaResource res) {
		return match(res.getTitle());
	}
}
