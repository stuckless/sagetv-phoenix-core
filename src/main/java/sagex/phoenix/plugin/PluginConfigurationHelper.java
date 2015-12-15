package sagex.phoenix.plugin;

import java.util.ArrayList;
import java.util.List;

import sagex.phoenix.configuration.Config;
import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.Field;
import sagex.phoenix.configuration.Group;
import sagex.phoenix.configuration.IConfigurationElement;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.NamedValue;
import sagex.plugin.AbstractPlugin;
import sagex.plugin.PluginProperty;

public class PluginConfigurationHelper {
    /**
     * Given a Configuration {@link Group}, add it's children as Plugin
     * configuration.
     *
     * @param plugin {@link AbstractPlugin} implementation
     * @param el     {@link Group} instance
     */
    public static void addConfiguration(AbstractPlugin plugin, Group el) {
        if (el == null) {
            Loggers.LOG.warn("Null Element", new Exception("Null Element for configuration!"));
            return;
        }

        for (IConfigurationElement e : el.getChildren()) {
            if (e instanceof Group) {
                addConfiguration(plugin, (Group) e);
            } else if (e instanceof Field) {
                addConfiguation(plugin, (Field) e);
            }
        }
    }

    private static void addConfiguation(AbstractPlugin plugin, Field f) {
        String options[] = null;
        if (f.getType() == ConfigType.MULTICHOICE || f.getType() == ConfigType.CHOICE) {
            if (f.getOptions() != null && f.getOptions().size() > 0) {
                List<String> keys = new ArrayList<String>();
                for (NamedValue v : f.getOptions()) {
                    keys.add(v.getValue());
                }
                options = keys.toArray(new String[]{});
            }
        }

        // Configuration Persistence knows about scope
        PluginProperty prop = plugin.addProperty(f.getType().sageId(), f.getId(), f.getDefaultValue(), f.getLabel(),
                f.getDescription(), options, f.getListSeparator()).setVisibility(new ConfigurationVisibility(f));
        prop.setPersistence(new ConfigurationPersistence());
        if (f.getHints().getBooleanValue(Config.Hint.REGEX, false)) {
            prop.setValidator(new RegexPropertyValidator());
        }
    }
}
