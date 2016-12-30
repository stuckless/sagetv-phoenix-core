package sagex.phoenix.progress;

import org.apache.log4j.Logger;

/**
 * Created by seans on 24/12/16.
 */
public class LogProgressMonitor extends BasicProgressMonitor {
    private final Logger log;

    public LogProgressMonitor(Logger log) {
        this.log=log;
    }
    @Override
    public void beginTask(String name, int worked) {
        super.beginTask(name, worked);
        log.debug("Progress: beginTask: " + name + " with work " + worked);
    }

    @Override
    public void setCancelled(boolean cancel) {
        super.setCancelled(cancel);
        log.debug("Progress: cancelled: " + getTaskName());
    }

    @Override
    public void worked(int worked) {
        super.worked(worked);
        log.debug("Progress: worked: " + getTaskName() + " incremented by " + worked);
    }

    @Override
    public void setTaskName(String name) {
        super.setTaskName(name);
        log.debug("Progress: new task: " + name);
    }

    @Override
    public void done() {
        super.done();
        log.debug("Progress: done: " + getTaskName());
    }
}
