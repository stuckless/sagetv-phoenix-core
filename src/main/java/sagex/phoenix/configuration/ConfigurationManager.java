package sagex.phoenix.configuration;

import org.apache.log4j.Logger;
import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.impl.ConfigurationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationManager {
    /**
     * event that is fired when a configuration element of type button is called
     * with SetProperty() * {@value}
     */
    public static final String BUTTON_EVENT = "configuration.button.event";

    /**
     * Configuration Button event argument that holder the event's property name
     * * {@value}
     */
    public static final String EVENT_PROPERTY = "property";

    /**
     * Configuration button event argument that holds the event's current value
     * * {@value}
     */
    public static final String EVENT_VALUE = "value";

    /**
     * event that is fired when a configuration value is set using SetProperty()
     * * {@value}
     */
    public static final String SET_EVENT = "configuration.set.event";

    private static final Logger log = Logger.getLogger(ConfigurationManager.class);
    private ConfigurationMetadataManager metadataManager;
    private IConfigurationProvider provider;
    private boolean strict = false;

    public ConfigurationManager(ConfigurationMetadataManager metadataManager, IConfigurationProvider configProvider) {
        this.metadataManager = metadataManager;
        this.provider = configProvider;
    }

    /**
     * Gets a property using the defined scope, if no scope, then use Client
     *
     * @param key
     * @param defValue
     */
    public String getProperty(String key, String defValue) {
        String val = null;
        IConfigurationElement ce = metadataManager.getConfigurationElement(key);
        if (ce != null && ce instanceof Field) {
            ConfigScope cs = ((Field) ce).getScope();
            if (cs == null)
                cs = ConfigScope.CLIENT;
            val = provider.getProperty(cs, key);
            if (val == null && defValue == null) {
                val = ((Field) ce).getDefaultValue();
            }
        } else {
            // no configuration metadata, just do a simple client lookup.
            val = provider.getProperty(ConfigScope.CLIENT, key);
        }
        return (val != null) ? val : defValue;
    }

    /**
     * Set's a property using the defined scope. If not scope, then client is
     * used.
     *
     * @param key
     * @param value
     * @return
     */
    public void setProperty(String key, String value) {
        IConfigurationElement ce = metadataManager.getConfigurationElement(key);
        if (ce != null && ce instanceof Field) {
            ConfigScope cs = ((Field) ce).getScope();
            if (cs == null)
                cs = ConfigScope.CLIENT;
            provider.setProperty(cs, key, value);
            if (((Field) ce).getType() == ConfigType.BUTTON) {
                // fire config button event
                Map args = new HashMap();
                args.put(EVENT_PROPERTY, key);
                args.put(EVENT_VALUE, value);
                Phoenix.getInstance().getEventBus().fireEvent(BUTTON_EVENT, args, false);
            }
        } else {
            provider.setProperty(ConfigScope.CLIENT, key, value);
        }

        // Fire an event to let listeners know that a value has been set
        // Map args = new HashMap();
        // args.put(EVENT_PROPERTY, key);
        // args.put(EVENT_VALUE, value);
        // Phoenix.getInstance().getEventBus().fireEvent(SET_EVENT, args,
        // false);
    }

    public void setServerProperty(String key, String value) {
        provider.setProperty(ConfigScope.SERVER, key, value);
    }

    public void setClientProperty(String key, String value) {
        provider.setProperty(ConfigScope.CLIENT, key, value);
    }

    public void setUserProperty(String key, String value) {
        provider.setProperty(ConfigScope.USER, key, value);
    }

    protected IConfigurationElement getConfigurationElement(String key) {
        if (key == null) {
            log.warn("Passed a Null Key!", new Exception("Passed a null key"));
            return null;
        }

        IConfigurationElement el = metadataManager.getConfigurationElement(key);
        if (el == null) {
            if (isStrictConfigurationEnabled()) {
                throw new RuntimeException("No metadata for configuration element: " + key);
            }
        }
        return el;
    }

    public String getServerProperty(String key, String defValue) {
        String val = provider.getProperty(ConfigScope.SERVER, key);
        if (val == null && defValue == null) {
            val = getDefaultValue(key);
        }
        return (val != null) ? val : defValue;
    }

    public String getClientProperty(String key, String defValue) {
        String val = provider.getProperty(ConfigScope.CLIENT, key);
        if (val == null && defValue == null) {
            val = getDefaultValue(key);
        }
        return (val != null) ? val : defValue;
    }

    public String getDefaultValue(String key) {
        IConfigurationElement ce = metadataManager.getConfigurationElement(key);
        if (ce != null && ce instanceof Field) {
            return ((Field) ce).getDefaultValue();
        }
        return null;
    }

    public String getUserProperty(String key, String defValue) {
        String val = provider.getProperty(ConfigScope.USER, key);
        if (val == null && defValue == null) {
            val = getDefaultValue(key);
        }
        return (val != null) ? val : defValue;
    }

    private boolean isStrictConfigurationEnabled() {
        return strict;
    }

    public void setStrictConfiguration(boolean b) {
        this.strict = false;
    }

    public void reload() {
        // fire an event that configuration providers can listen for, to
        // "reload".
        Phoenix.getInstance().getEventBus().fireEvent(ConfigurationManager.class.getName() + ".reload", null, false);
    }

    public void save() {
        // fire an event that configuration providers can listen for, to "save".
        Phoenix.getInstance().getEventBus().fireEvent(ConfigurationManager.class.getName() + ".save", null, false);
    }

    /**
     * Returns a list of the property keys for the given scope
     *
     * @param scope
     * @return
     */
    public List<String> getProperties(ConfigScope scope) {
        return ConfigurationUtils.toList(provider.keys(scope));
    }
}
