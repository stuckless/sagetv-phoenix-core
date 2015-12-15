package sagex.phoenix.progress;

/**
 * Defines a command that will run with a progress monitor.
 *
 * @param <T>
 * @author seans
 */
public interface IRunnableWithProgress<T extends IProgressMonitor> {
    public void run(T monitor);
}
