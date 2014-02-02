package sagex.phoenix.progress;

/**
 * progress monitor is used to track progress of a long running operation. This
 * is modelled off the eclipse progress monitor.
 * 
 * @author seans
 * 
 */
public interface IProgressMonitor {
	public static final int UNKNOWN = 0;

	public abstract void beginTask(String name, int worked);

	public abstract void done();

	public abstract boolean isCancelled();

	public abstract void setCancelled(boolean cancel);

	public abstract void worked(int worked);

	public abstract double internalWorked();

	public abstract String getTaskName();

	public abstract void setTaskName(String name);

	public abstract boolean isDone();

	public abstract int getTotalWork();

	public abstract int getWorked();

}