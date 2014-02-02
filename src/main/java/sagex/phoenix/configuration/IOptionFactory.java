package sagex.phoenix.configuration;

import java.util.List;

import sagex.phoenix.util.NamedValue;

/**
 * Simple Option Factory for creating options
 * 
 * @author sean
 */
public interface IOptionFactory {
	public List<NamedValue> getOptions(String key);
}
