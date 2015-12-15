package phoenix.impl;

import org.apache.log4j.Logger;
import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.impl.ConfigurationUtils;
import sagex.phoenix.skins.ISkin;
import sagex.phoenix.skins.Skin;
import sagex.phoenix.skins.Skin.State;
import sagex.phoenix.tools.annotation.API;

import java.io.File;
import java.util.List;

/**
 * Skin Operations
 *
 * @author seans
 */
@API(group = "skin")
public class SkinAPI {
    private Logger log = Logger.getLogger(this.getClass());

    public SkinAPI() {
    }

    /**
     * Creates a new Skin in the Skins Directory, and activates it.
     *
     * @param id   skin id, something like, "glassyicons", or "sls.skin1", etc
     * @param name friendly name
     * @param dep  skin dependency
     * @return null if the skin cannel be created, or if it already exists.
     */
    public ISkin CreateSkin(String id, String name, Object dep) {
        if (dep != null) {
            return Phoenix.getInstance().getSkinManager().createPlugin(id, name, getSkin(dep));
        } else {
            return Phoenix.getInstance().getSkinManager().createPlugin(id, name, null);
        }
    }

    /**
     * Completely removes a skin from the filesystem. Use with care. There is no
     * undelete.
     *
     * @param skin
     * @return
     */
    public void DeleteSkin(Object skin) {
        Phoenix.getInstance().getSkinManager().deletePlugin(getSkin(skin));
    }

    /**
     * Returns the available skins
     *
     * @return
     */
    public ISkin[] GetSkins() {
        return Phoenix.getInstance().getSkinManager().getPlugins();
    }

    /**
     * Starts/Activates a skin. NOTE, this is NOT loading the skin into the
     * current UI context, this simply resolves the skin and activates it, so
     * that it can be loaded into the current UI context, using the LoadSkin()
     * call.
     *
     * @param plugin
     * @return if the skin can be activated
     */
    public boolean StartSkin(ISkin plugin) {
        return Phoenix.getInstance().getSkinManager().activatePlugin(plugin);
    }

    /**
     * Stops a skin. ie, prevents it from being loaded using the LoadSkin()
     * call.
     *
     * @param plugin
     * @return
     */
    public boolean StopSkin(ISkin plugin) {
        Phoenix.getInstance().getSkinManager().stopPlugin(plugin);
        return true;
    }

    /**
     * Return a skin resource by looking for it the plugin's path or by
     * searching it's dependencies, if it does not exist.
     *
     * @param plugin
     * @param path
     * @return
     */
    public File GetSkinResource(Object skin, String path) {
        if (skin == null) {
            return null;
        }

        ISkin plugin = null;
        if (skin instanceof String) {
            plugin = Phoenix.getInstance().getSkinManager().findPlugin((String) skin);
        } else if (skin instanceof Skin) {
            plugin = (ISkin) skin;
        } else {
            throw new RuntimeException("Invalid Skin: " + skin);
        }

        return Phoenix.getInstance().getSkinManager().resolveFile(plugin, path);
    }

    /**
     * Finds a skin by Id
     *
     * @param skin
     * @return {@link Skin} skin instance
     */
    public ISkin FindSkin(Object skin) {
        return getSkin(skin);
    }

    /**
     * accepts a {@link ISkin} instance or a Skin Id
     * <p/>
     * throws a runtime exception if you give it an invalid skin
     *
     * @param skin
     * @return
     */
    public ISkin getSkin(Object skin) {
        ISkin plugin = null;
        if (skin instanceof String) {
            plugin = Phoenix.getInstance().getSkinManager().findPlugin((String) skin);
        } else if (skin instanceof Skin) {
            plugin = (ISkin) skin;
        } else {
            throw new RuntimeException("Invalid Skin: " + skin);
        }
        return plugin;
    }

    /**
     * Load a skin into the current context. This will process the skin and it's
     * image register and setup the global context for the given skin. If the
     * Skin is not found or is not activated, then the default skin will be
     * loaded.
     *
     * @param uiContext ui context for the skin
     * @param skin      skin id or {@link Skin} skin instance
     */
    public void LoadSkin(String uiContext, Object skin) {
        log.info("LoadSkin(): Skin: " + skin + "; context " + uiContext);

        if (skin == null) {
            log.info("LoadSkin(): Skin was null, using default skin " + skin + "; context " + uiContext);
            skin = "skin.default";
            return;
        }

        ISkin plugin = null;
        if (skin instanceof String) {
            plugin = Phoenix.getInstance().getSkinManager().findPlugin((String) skin);
        } else if (skin instanceof Skin) {
            plugin = (ISkin) skin;
        } else {
            log.warn("Unabled to load skin: " + skin + "; Using default.");
            plugin = Phoenix.getInstance().getSkinManager().findPlugin("skin.default");
        }

        if (plugin.getState() != State.ACTIVE) {
            log.warn("The skin is not active: " + plugin.getId() + "; Using default skin.");
            plugin = Phoenix.getInstance().getSkinManager().findPlugin("skin.default");
        }

        if (plugin == null) {
            throw new RuntimeException("Unable to load a valid Phoenix Skin.  Phoenix will not work.");
        }

        Phoenix.getInstance().getSkinManager().loadSkin(uiContext, plugin);
    }

    /**
     * Get a Skin Configuration Property
     *
     * @param key
     * @return
     */
    public String GetSkinProperty(String key, Object defValue) {
        String o = phoenix.config.GetUserProperty("skin/default/" + key, defValue);
        if (o == null)
            return (defValue == null) ? null : String.valueOf(defValue);
        return o;
    }

    /**
     * Sets a skin property
     *
     * @param key
     * @param value
     */
    public void SetSkinProperty(String key, String value) {
        phoenix.config.SetUserProperty("skin/default/" + key, value);
    }

    /**
     * Returns a list of the currently configured skin property keys
     *
     * @return List<String> of the skin configuration keys
     */
    public List GetSkinProperties() {
        return ConfigurationUtils.filterContainsKeys("/skin/default/", Phoenix.getInstance().getConfigurationManager()
                .getProperties(ConfigScope.USER));
    }

    /**
     * Returns a list of the currently configured skin property keys
     *
     * @return List<String> of the skin configuration keys
     */
    public List GetSkinProperties(String startsWithFilter) {
        return ConfigurationUtils.filterContainsKeys("/skin/default/" + startsWithFilter, Phoenix.getInstance()
                .getConfigurationManager().getProperties(ConfigScope.USER));
    }
}
