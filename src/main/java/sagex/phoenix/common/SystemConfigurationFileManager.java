package sagex.phoenix.common;

import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;
import sagex.api.Configuration;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages sets of configuration files from 3 primary sources; System, Plugins,
 * and User Contributed. Configuration files are loaded in that order to ensure
 * that plugins can override the system, and the users can override the system
 * or plugin.
 * <p/>
 * Subclasses can inherit from this class to inherit the ability to have 3
 * classes of configuration files, that are loaded in a particular order.
 * <p/>
 * if the configuration property, 'phoenix/safeMode' is true, then ONLY system
 * configurations are loaded.
 *
 * @author sean
 */
public abstract class SystemConfigurationFileManager implements HasFileConfigurations {
    public enum ConfigurationType {
        System, User, Plugin
    }

    ;

    public interface ConfigurationFileVisitor {
        public void visitConfigurationFile(ConfigurationType type, File file);
    }

    protected Logger log = Logger.getLogger(this.getClass());

    private ManagedDirectory systemFiles;
    private List<ManagedDirectory> pluginFiles = new ArrayList<ManagedDirectory>();
    private ManagedDirectory userFiles;

    public ManagedDirectory getSystemFiles() {
        return systemFiles;
    }

    public List<ManagedDirectory> getPluginFiles() {
        return pluginFiles;
    }

    public ManagedDirectory getUserFiles() {
        return userFiles;
    }

    public FileFilter getConfigurationFilter() {
        return configurationFilter;
    }

    private FileFilter configurationFilter;

    public SystemConfigurationFileManager(File systemDir, File userDir, FileFilter configurationFilter) {
        systemFiles = new ManagedDirectory(systemDir, configurationFilter);
        userFiles = new ManagedDirectory(userDir, configurationFilter);
        this.configurationFilter = configurationFilter;
    }

    public boolean addPluginConfiguration(File pluginConfigDir) {
        ManagedDirectory dir = new ManagedDirectory(pluginConfigDir, configurationFilter);
        if (!pluginFiles.contains(dir)) {
            pluginFiles.add(dir);
            return true;
        }
        return false;
    }

    public void accept(ConfigurationFileVisitor visitor) {
        if (systemFiles != null) {
            log.debug("Visiting System Files");
            visitFiles(systemFiles.getFiles(), ConfigurationType.System, visitor);
        }

        boolean safeMode = BooleanUtils.toBoolean(Configuration.GetProperty("phoenix/safeMode", "false"));
        if (!safeMode) {
            for (ManagedDirectory d : pluginFiles) {
                log.debug("Visiting Plugin Files");
                visitFiles(d.getFiles(), ConfigurationType.Plugin, visitor);
            }
        } else {
            log.info("Safe Mode Enabled, plugin configurations ignored.");
        }

        if (!safeMode) {
            if (userFiles != null) {
                log.debug("Visiting User Files");
                visitFiles(userFiles.getFiles(), ConfigurationType.User, visitor);
            }
        } else {
            log.info("Safe Mode Enabled, user configurations ignored.");
        }
    }

    private void visitFiles(File[] files, ConfigurationType type, ConfigurationFileVisitor visitor) {
        if (files != null) {
            for (File f : files) {
                log.debug("Visiting configuration; type: " + type + "; file: " + f);
                visitor.visitConfigurationFile(type, f);
            }
        }
    }

    public abstract void loadConfigurations();
}
