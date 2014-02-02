package sagex.phoenix.task;

public interface ITaskOperation {
	public void performAction(TaskItem item) throws Throwable;

	public boolean canRetry(Throwable t);
}
