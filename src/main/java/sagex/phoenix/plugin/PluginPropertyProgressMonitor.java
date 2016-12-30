package sagex.phoenix.plugin;

import sagex.phoenix.progress.BasicProgressMonitor;
import sagex.phoenix.util.Loggers;
import sagex.plugin.PluginProperty;

/**
 * Progress Monitor that updates the Help Text for a Plugin based on the Progress
 */
public class PluginPropertyProgressMonitor extends BasicProgressMonitor {
    PluginProperty prop = null;
    String text = null;

    public PluginPropertyProgressMonitor(PluginProperty propInst) {
        this.prop = propInst;
        text = propInst.getHelp();
    }

    @Override
    public void beginTask(String name, int worked) {
        super.beginTask(name, worked);
        prop.setHelp(name);
    }

    @Override
    public void done() {
        super.done();
        prop.setHelp(text);
        Loggers.LOG.info("PROGRESS: DONE.");
    }

    @Override
    public void setTaskName(String name) {
        super.setTaskName(name);
        prop.setHelp(name);
        Loggers.LOG.info("PROGRESS: " + name);
    }
}
