package sagex.phoenix.progress;

/**
 * Implements the most basic progress monitor possible.
 * 
 * @author seans
 * 
 */
public class BasicProgressMonitor implements IProgressMonitor {
	private boolean cancelled = false;
	private String task;
	private int totalWork;
	private int worked;
	private int unknownTotal = 1000;
	private boolean done = false;
	private boolean initialized = false;

	public BasicProgressMonitor() {
		totalWork = IProgressMonitor.UNKNOWN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#beginTask(java.lang.String,
	 * int)
	 */
	public void beginTask(String name, int worked) {
		this.task = name;

		// prevents the total work from being updated after it has been set.
		if (!initialized) {
			initialized = true;
			this.totalWork = worked;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#done()
	 */
	public void done() {
		done = true;
		// this ensures that when done is called internalWorked() == 100%
		totalWork = worked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#isCancelled()
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#setCancelled(boolean)
	 */
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#worked(int)
	 */
	public void worked(int worked) {
		this.worked += worked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#internalWorked()
	 */
	public double internalWorked() {
		if (totalWork == UNKNOWN) {
			worked = worked % unknownTotal;
			if (worked == 0 || unknownTotal == 0)
				return 0;
			return (double) worked / (double) unknownTotal;
		} else {
			if (worked == 0 || totalWork == 0)
				return 0;
			if (worked >= totalWork)
				return 1;
			return (double) worked / (double) totalWork;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#getTaskName()
	 */
	public String getTaskName() {
		return task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sagex.phoenix.progress.IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
		this.task = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#isDone()
	 */
	public boolean isDone() {
		return done;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#getTotalWork()
	 */
	public int getTotalWork() {
		return totalWork;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sagex.phoenix.progress.IProgressMonitor#getWorked()
	 */
	public int getWorked() {
		return worked;
	}
}
