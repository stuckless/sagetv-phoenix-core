package sagex.phoenix.cache;

import java.util.HashMap;
import java.util.Map;

public class MapCache<T> extends AbstractCache<T> {
	public MapCache() {
		this(-1);
	}

	public MapCache(long expiry) {
		super(expiry);
	}

	@Override
	protected Map<Object, CachedObject<T>> createCacheMap() {
		return new HashMap<Object, CachedObject<T>>();
	}
}
