package sagex.phoenix.configuration;

import sagex.phoenix.util.NamedValue;

import java.util.List;

/**
 * Simple Option Factory for creating options
 *
 * @author sean
 */
public interface IOptionFactory {
    public List<NamedValue> getOptions(String key);
}
