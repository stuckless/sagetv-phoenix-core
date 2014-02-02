package sagex.phoenix.progress;

/**
 * NullProgressMonitor is a monitor that does not honor, done or cancelled.
 * There is only a single instance that is shared by all places the use a null
 * progress monitor. It's a lazy and cheap way to call a visitor with a monitor
 * when you don't care about the progress.
 * 
 * To get an instance of the NullProgressMonitor, use the INSTANCE field. Keep
 * in mind this single instance does NOT hold any stateful information. ie,
 * after calling beginTask(name), and you call getTaskName() it will be null.
 * 
 * @author seans
 */
public class NullProgressMonitor implements IProgressMonitor {
	public static final NullProgressMonitor INSTANCE = new NullProgressMonitor();

	private NullProgressMonitor() {
	}

	@Override
	public void beginTask(String name, int worked) {
	}

	@Override
	public void done() {
	}

	@Override
	public String getTaskName() {
		return null;
	}

	@Override
	public int getTotalWork() {
		return 0;
	}

	@Override
	public int getWorked() {
		return 0;
	}

	@Override
	public double internalWorked() {
		return 0;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public void setCancelled(boolean cancel) {
	}

	@Override
	public void setTaskName(String name) {
	}

	@Override
	public void worked(int worked) {
	}
}
