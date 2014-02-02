package sagex.phoenix.configuration;

import java.util.Iterator;

/**
 * Simple abstraction to Text based configuration systems, like Java Properties,
 * Sage Properties, etc.
 * 
 * @author sean
 */
public interface IConfigurationProvider {
	public String getProperty(ConfigScope scope, String key);

	public void setProperty(ConfigScope scope, String key, String val);

	public Iterator<String> keys(ConfigScope scope);
}