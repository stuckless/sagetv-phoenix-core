package sagex.phoenix.cache;

import java.util.HashMap;
import java.util.Map;

public class AbstractCache<T> implements ICache<T> {
    protected static class CachedObject<T2> {
        private T2 entry;
        private long lastModified;

        public CachedObject(T2 o) {
            update(o);
        }

        public T2 update(T2 obj) {
            entry = obj;
            lastModified = System.currentTimeMillis();
            return entry;
        }

        public T2 getEntry() {
            return entry;
        }

        public long getLastModified() {
            return lastModified;
        }
    }

    private Map<Object, CachedObject<T>> objects = null;
    private int hits;
    private int misses;

    // default expiry is 30 seconds
    private long expiry = 30 * 1000;

    public AbstractCache(long expiry) {
        super();

        this.expiry = expiry;
        objects = createCacheMap();
    }

    protected Map<Object, CachedObject<T>> createCacheMap() {
        return new HashMap<Object, CachedObject<T>>();
    }

    public Map<Object, CachedObject<T>> getObjects() {
        return objects;
    }

    public void clear() {
        hits = 0;
        misses = 0;
        objects.clear();
    }

    public T get(Object id) {
        CachedObject<T> o = objects.get(id);
        if (o == null) {
            misses++;
            return null;
        } else {
            if (expiry == -1 || o.lastModified + expiry > System.currentTimeMillis()) {
                hits++;
                return o.update(o.entry);
            } else {
                misses++;
                remove(id);
                return null;
            }
        }
    }

    public void put(Object id, T obj) {
        if (obj == null)
            return;

        CachedObject<T> o = objects.get(id);
        if (o == null) {
            objects.put(id, new CachedObject<T>(obj));
        } else {
            o.update(obj);
        }
    }

    public int hits() {
        return hits;
    }

    public int misses() {
        return misses;
    }

    public void remove(Object id) {
        objects.remove(id);
    }

    public int size() {
        return objects.size();
    }

    public void ejectStaleItems() {
        clear();
    }
}