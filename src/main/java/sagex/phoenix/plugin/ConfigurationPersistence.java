package sagex.phoenix.plugin;

import sagex.phoenix.Phoenix;
import sagex.plugin.IPropertyPersistence;

/**
 * {@link IPropertyPersistence} that uses Phoenix configuration system
 * 
 * @author sean
 * 
 */
public class ConfigurationPersistence implements IPropertyPersistence {
	@Override
	public String get(String key, String defValue) {
		// get property based on scope
		return Phoenix.getInstance().getConfigurationManager().getProperty(key, defValue);
	}

	@Override
	public void set(String key, String value) {
		// sets property based on scope
		Phoenix.getInstance().getConfigurationManager().setProperty(key, value);
	}
}
