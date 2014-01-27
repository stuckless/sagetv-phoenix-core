package sagex.phoenix.progress;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 * A progress tracker is a special kind of {@link IProgressMonitor} that will 
 * track the status of items as success, failed, or skipped.
 * 
 * @author seans
 */
public class ProgressTracker<T> implements IProgressMonitor {
    private Date dateTime = null;
    
    private IProgressMonitor monitor = null;
    private LinkedList<TrackedItem<T>> success = new LinkedList<TrackedItem<T>>();
    private LinkedList<TrackedItem<T>> failed = new LinkedList<TrackedItem<T>>();
    private LinkedList<TrackedItem<T>> skipped = new LinkedList<TrackedItem<T>>();
    
    private String label = null;
    
    public ProgressTracker() {
        this(new BasicProgressMonitor());
    }
    
    public ProgressTracker(IProgressMonitor monitor) {
        this.monitor = monitor;
        updateDateTime();
    }
    
    public void addSuccess(T item) {
        success.add(new TrackedItem<T>(item));
        updateDateTime();
    }

    public void addSkipped(T item, String msg) {
        skipped.add(new TrackedItem<T>(item, msg));
        updateDateTime();
    }
    
    private void updateDateTime() {
        dateTime = Calendar.getInstance().getTime();
    }
    
    public void addFailed(T item, String msg) {
        addFailed(item, msg, null);
        updateDateTime();
    }
    
    public void addFailed(T item, String msg, Throwable t) {
        failed.add(new TrackedItem<T>(item, msg, t));
        updateDateTime();
    }
    
    /**
     * LinkedList is used as the return type because we want to access the list as a List and Queue, depending on the scenario.
     * 
     * @return
     */
    public LinkedList<TrackedItem<T>> getSuccessfulItems() {
        return success;
    }

    public LinkedList<TrackedItem<T>> getSkippedItems() {
        return skipped;
    }
    
    public LinkedList<TrackedItem<T>> getFailedItems() {
        return failed;
    }

    public void beginTask(String name, int worked) {
        monitor.beginTask(name, worked);
        updateDateTime();
    }

    public void done() {
        monitor.done();
    }

    public boolean isCancelled() {
        return monitor.isCancelled();
    }

    public void setCancelled(boolean cancel) {
        monitor.setCancelled(cancel);
    }

    public void worked(int worked) {
        monitor.worked(worked);
    }

    public double internalWorked() {
        return monitor.internalWorked();
    }

    public String getTaskName() {
        return monitor.getTaskName();
    }

    public void setTaskName(String name) {
        monitor.setTaskName(name);
    }

    public boolean isDone() {
        return monitor.isDone();
    }

    public int getTotalWork() {
        return monitor.getTotalWork();
    }

    public int getWorked() {
        return monitor.getWorked();
    }
    
    public Date getLastUpdated() {
        return dateTime;
    }

    /**
     * @return the label assigned to the tracker, if any
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets a label for the tracker.  A label is simply a name and has no other purpose
     * @param label label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
    
    public void clear() {
        success.clear();
        failed.clear();
        skipped.clear();
    }
    
    public int getFailedCount() {
        return failed.size();
    }
    
    public int getSuccessCount() {
        return success.size();
    }
    
    public int getSkippedCount() {
        return skipped.size();
    }
}
