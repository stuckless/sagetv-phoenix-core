package sagex.phoenix.metadata.proxy;

import java.util.HashMap;
import java.util.Map;

import sagex.phoenix.metadata.IMetadata;

/**
 * Metadata Proxy that stores metadata in a String Map
 * 
 * @author seans
 */
public class MetadataProxy extends AbstractMetadataProxy {
	private Map<String, String> map;

	/**
	 * use newInstance()
	 */
	public MetadataProxy() {
		this(new HashMap<String, String>());
	}

	/**
	 * use newInstance(map)
	 */
	public MetadataProxy(Map<String, String> map) {
		this.map = map;
	}

	@Override
	public String get(SageProperty key) {
		return map.get(key.value());
	}

	@Override
	public boolean isSet(SageProperty key) {
		return map.containsKey(key.value());
	}

	@Override
	public void set(SageProperty key, String value) {
		map.put(key.value(), value);
	}

	@Override
	public void clear(SageProperty key) {
		map.remove(key.value());
	}

	protected Map<String, String> getMap() {
		return map;
	}

	public static IMetadata newInstance() {
		return (IMetadata) java.lang.reflect.Proxy.newProxyInstance(MetadataProxy.class.getClassLoader(),
				new Class[] { IMetadata.class }, new MetadataProxy());
	}

	public static <T> T newInstance(Class<T> metadata) {
		return (T) java.lang.reflect.Proxy.newProxyInstance(MetadataProxy.class.getClassLoader(), new Class[] { metadata },
				new MetadataProxy());
	}

	public static IMetadata newInstance(Map<String, String> map) {
		return (IMetadata) java.lang.reflect.Proxy.newProxyInstance(MetadataProxy.class.getClassLoader(),
				new Class[] { IMetadata.class }, new MetadataProxy(map));
	}

	public static <T> T newInstance(Map<String, String> map, Class<T> metadata) {
		return (T) java.lang.reflect.Proxy.newProxyInstance(MetadataProxy.class.getClassLoader(), new Class[] { metadata },
				new MetadataProxy(map));
	}
}
