package sagex.phoenix.skins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;

import sagex.UIContext;
import sagex.api.Global;
import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.skins.Skin.State;
import sagex.phoenix.util.PropertiesUtils;

public class SkinManager extends SystemConfigurationFileManager implements SystemConfigurationFileManager.ConfigurationFileVisitor {
	private static final Logger log = Logger.getLogger(SkinManager.class);

	private List<Skin> skins = new ArrayList<Skin>();

	public SkinManager(File systemDir, File userDir) {
		super(systemDir, userDir, FileFilterUtils.makeSVNAware(DirectoryFileFilter.INSTANCE));
	}

	private void activatePlugins() {
		for (ISkin p : skins) {
			if (!activatePlugin(p)) {
				log.error("Failed to activate Plugin: " + p.getId() + "; Status: " + p.getStatus());
			} else {
				log.info("Plugin Activated: " + p.getId());
			}
		}
	}

	public boolean activatePlugin(ISkin p) {
		if (p.getState() == State.STARTING) {
			log.error("Attempting to activate a plugin that is already starting.  This is probably a cyclic dependency for: "
					+ p.getId());
			p.setState(State.FAILED);
			p.setStatus("Can't Activate.  Possible cyclical dependencies.");
			return false;
		}

		if (p.getState() == State.ACTIVE)
			return true;
		if (p.getState() == State.FAILED)
			return false;
		if (p.getState() != State.RESOLVED)
			return false;

		p.setState(State.STARTING);

		// activate dependencies first.
		for (String dep : p.getDependencies()) {
			if (!activatePlugin(findPlugin(dep))) {
				p.setState(State.FAILED);
				p.setStatus("Failed to Activate Dependency Plugin: " + dep);
				return false;
			}
		}

		p.setState(State.ACTIVE);

		return true;
	}

	private void resolvePlugins() {
		for (ISkin p : skins) {
			resolvePlugin(p);
		}
	}

	private boolean resolvePlugin(ISkin p) {
		if (p == null)
			return false;
		if (p.getState() == State.ACTIVE)
			return true;
		if (p.getState() == State.FAILED)
			return false;
		if (p.getState() == State.RESOLVED)
			return true;

		if (p.getDependencies() == null || p.getDependencies().length == 0) {
			p.setState(State.RESOLVED);
			return true;
		}

		for (String dep : p.getDependencies()) {
			if (!resolvePlugin(findPlugin(dep))) {
				p.setState(State.FAILED);
				p.setStatus("Failed to resolve dependency: " + dep);
				return false;
			}
		}

		p.setState(State.RESOLVED);
		return true;
	}

	private void installPlugins() {
		for (ISkin p : skins) {
			if (p.getState() == State.UNINSTALLED) {
				// TODO: Actually Install the plugin, for new we are assuming
				// that it is already installed
				p.setState(State.INSTALLED);
			}
		}
	}

	public ISkin findPlugin(String id) {
		if (id == null)
			return null;
		for (ISkin p : skins) {
			if (id.equals(p.getId()))
				return p;
		}
		return null;
	}

	public ISkin[] getPlugins() {
		return skins.toArray(new Skin[skins.size()]);
	}

	public void stopPlugins() {
		for (ISkin p : skins) {
			stopPlugin(p);
		}
	}

	public void stopPlugin(ISkin p) {
		p.setState(State.STOPPING);
		p.setState(State.RESOLVED);
	}

	public ISkin createPlugin(String id, String name, ISkin dep) {
		ISkin skin = findPlugin(id);
		if (skin != null) {
			log.warn("Plugin already exists: " + id);
			return null;
		}

		File pluginDir = new File(getUserFiles().getDir(), id);
		if (pluginDir.exists()) {
			log.error("Can't create plugin, plugin dir already exists: " + pluginDir.getAbsolutePath());
			return null;
		}

		sagex.phoenix.util.FileUtils.mkdirsQuietly(pluginDir);

		File f = new File(pluginDir, "skin.xml");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.printf("<plugin id=\"%s\" name=\"%s\" version=\"1.0\">\n", id, name);
			pw.printf("   <description>New Plugin - Change Description</description>\n");
			if (dep != null) {
				pw.printf("   <depend id=\"%s\"/>\n", dep.getId());
			}
			pw.printf("</plugin>\n");
			pw.flush();
			pw.close();
		} catch (IOException e) {
			log.error("Failed to create a new plugin!", e);
		}

		f = new File(pluginDir, "README");
		try {
			f.createNewFile();
		} catch (IOException e) {
		}

		f = new File(pluginDir, "ScreenShots");
		sagex.phoenix.util.FileUtils.mkdirQuietly(f);

		// load the new plugin
		visitConfigurationFile(ConfigurationType.User, pluginDir);

		// return the new plugin
		skin = findPlugin(id);
		skin.setState(State.ACTIVE);
		return skin;
	}

	public void deletePlugin(ISkin plugin) {
		stopPlugin(plugin);
		try {
			FileUtils.deleteDirectory(plugin.getDirectory());
		} catch (IOException e) {
			log.error("Failed to fully delete plugin: " + plugin);
		}
	}

	public void loadSkin(String uiContext, ISkin skin) {
		log.info("Loading Skin: " + skin + " for context: " + uiContext);
		String deps[] = skin.getDependencies();
		if (deps != null && deps.length > 0) {
			for (String dep : deps) {
				loadSkin(uiContext, findPlugin(dep));
			}
		}

		Properties images = new Properties();
		File propFile = skin.getResource("images.properties");
		if (propFile == null || !propFile.exists()) {
			log.warn("Missing images.properties for skin: " + skin);
			return;
		}

		try {
			log.info("Loading skin properties: " + propFile);
			PropertiesUtils.load(images, propFile);
		} catch (IOException e) {
			log.error("Failed to load sking properties: " + propFile.getAbsolutePath() + " for plugin " + skin);
			return;
		}

		UIContext ctx = new UIContext(uiContext);

		Pattern insetsPattern = Pattern.compile("[0-9]+\\s*,[0-9]+\\s*,[0-9]+\\s*,[0-9]+\\s*");
		for (Enumeration e = images.propertyNames(); e.hasMoreElements();) {
			String var = (String) e.nextElement();
			String file = images.getProperty(var);
			if (var != null)
				var = var.trim();
			if (file != null)
				file = file.trim();
			Matcher m = insetsPattern.matcher(file);
			if (m.find()) {
				// handle insets
				String insets[] = file.split("\\s*,\\s*");
				log.info("Adding Global Context Skin Insets Variable: " + var + " with Value: " + file + "; in Skin: " + skin);
				Global.AddGlobalContext(ctx, var, insets);
				continue;
			}

			File imgFile = resolveFile(skin, file);
			if (imgFile == null) {
				log.info("Adding Global Context Skin Variable: " + var + " with Value: " + file + "; in Skin: " + skin);
				Global.AddGlobalContext(ctx, var, file);
			} else {
				log.info("Adding Global Skin Variable; UI: " + uiContext + "; Variable: " + var + "; Image: "
						+ imgFile.getAbsolutePath());
				Global.AddGlobalContext(ctx, var, imgFile.getAbsolutePath());
			}
		}
	}

	public File resolveFile(ISkin skin, String file) {
		File f = skin.getResource(file);
		if (f != null && f.exists()) {
			return f;
		}

		String deps[] = skin.getDependencies();
		if (deps != null && deps.length > 0) {
			for (String dep : deps) {
				skin = findPlugin(dep);
				if (skin != null) {
					return resolveFile(skin, file);
				}
			}
		}

		return null;
	}

	@Override
	public void loadConfigurations() {
		log.info("Begin Loading Skins");
		skins.clear();
		accept(this);
		installPlugins();
		resolvePlugins();
		activatePlugins();
		log.info("End Loading Skins");
	}

	@Override
	public void visitConfigurationFile(ConfigurationType type, File file) {
		log.info("Loading " + type + " Skin: " + file);
		Skin plugin = null;
		try {
			plugin = SkinBuilder.buildPlugin(file);
		} catch (Throwable t) {
			log.error("Failed to load plugin: " + file.getAbsolutePath(), t);
			plugin.setState(State.FAILED);
			plugin.setStatus(t.getMessage());
		}

		plugin.setManager(this);

		// only load the plugin, if it doesn not already exist
		ISkin p = findPlugin(plugin.getId());
		if (p == null) {
			// don't add plugins that don't have a skin.xml
			File xml = plugin.getResource("skin.xml");
			if (xml == null || !xml.exists()) {
				log.warn("Skipping Skin: " + plugin.getId() + " since it is not a valid skin");
				return;
			}

			if (plugin.getState() == State.FAILED) {
				log.warn("Skipping Skin: " + plugin.getId() + " since it failed to load");
				return;
			}

			log.info("Adding Phoenix Skin: " + plugin.getId());
			skins.add(plugin);
		} else {
			log.warn("Skippin Phoenix Skin in dir: " + file);
		}
	}
}
