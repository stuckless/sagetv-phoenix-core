package sagex.phoenix.progress;

/**
 * Defines a command that will run with a progress monitor.
 * 
 * @author seans
 * 
 * @param <T>
 */
public interface IRunnableWithProgress<T extends IProgressMonitor> {
	public void run(T monitor);
}
