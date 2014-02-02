package sagex.phoenix.cache;

import java.util.Map;
import java.util.WeakHashMap;

public class SimpleWeakMapCache<T> extends AbstractCache<T> {
	public SimpleWeakMapCache() {
		this(30 * 1000);
	}

	public SimpleWeakMapCache(long expiry) {
		super(expiry);
	}

	@Override
	protected Map<Object, CachedObject<T>> createCacheMap() {
		return new WeakHashMap<Object, CachedObject<T>>();
	}
}
