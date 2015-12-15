package sagex.phoenix.metadata.proxy;

import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MetadataUtil;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * A COW (Copy On Write) Metadata proxy will proxy all requests to the Parent
 * but it will NOT commit any writes to the parent. This allows you to proxy the
 * metadata for an object without worrying that it will physically update the
 * parent. If you later decide that you want the parent to reflect the changes
 * in the COW instance, you can call commit() on the object.
 * <p/>
 * The {@link IMetadata} instance MUST be a metadata proxy instance that
 * subclasses from {@link AbstractMetadataProxy}
 *
 * @author seans
 */
public class COWMetadataProxy extends MetadataProxy {
    private AbstractMetadataProxy parent = null;
    private Map<String, SageProperty> keys = null;

    public COWMetadataProxy(IMetadata parent) {
        super();
        this.parent = (AbstractMetadataProxy) Proxy.getInvocationHandler(parent);

        // store a copy of th key map for faster lookups
        keys = MetadataUtil.getProperties(IMetadata.class);
    }

    @Override
    public String get(SageProperty key) {
        // Works because this objects data is stored in a map
        // all sets are put into this map and not the parent, so
        // when we 'get' a value we look first in our map, and then
        // in the parent.
        String val = super.get(key);
        if (val != null)
            return val;
        return parent.get(key);
    }

    /**
     * Takes all sets from the COW map and applies them against the parent.
     */
    public synchronized void commit() {
        Map<String, String> map = getMap();
        for (Map.Entry<String, String> me : map.entrySet()) {
            if (keys == null) {
                log.warn("No Keys... unable to commit()");
                continue;
            }
            // need to set the keys using SageProperty
            SageProperty key = keys.get(me.getKey());
            if (key == null) {
                log.warn("No key for: " + me.getKey() + "; Skipping Commit for field.");
                continue;
            }
            parent.set(key, me.getValue());
        }
        map.clear();
    }

    public static IMetadata newInstance(IMetadata parent) {
        return (IMetadata) java.lang.reflect.Proxy.newProxyInstance(COWMetadataProxy.class.getClassLoader(),
                new Class[]{IMetadata.class}, new COWMetadataProxy(parent));
    }
}
