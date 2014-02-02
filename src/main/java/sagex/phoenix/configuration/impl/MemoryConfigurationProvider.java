package sagex.phoenix.configuration.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.IConfigurationProvider;
import sagex.phoenix.util.IterableEnumeration;

/**
 * Simple, non-peristed, Configuration Provider.
 * 
 * @author sean
 */
public class MemoryConfigurationProvider implements IConfigurationProvider {
	private Map<ConfigScope, Properties> props = new HashMap<ConfigScope, Properties>();

	public MemoryConfigurationProvider() {
		props.put(ConfigScope.CLIENT, new Properties());
		props.put(ConfigScope.USER, new Properties());
		props.put(ConfigScope.SERVER, new Properties());
	}

	public String getProperty(ConfigScope scope, String key) {
		return props.get(scope).getProperty(key);
	}

	public void setProperty(ConfigScope scope, String key, String val) {
		props.get(scope).setProperty(key, val);
	}

	@Override
	public Iterator<String> keys(ConfigScope scope) {
		return new IterableEnumeration<String>((Enumeration<String>) props.get(scope).propertyNames());
	}
}
