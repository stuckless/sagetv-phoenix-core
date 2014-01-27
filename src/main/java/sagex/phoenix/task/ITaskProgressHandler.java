package sagex.phoenix.task;

public interface ITaskProgressHandler {
	public void onStart(TaskItem item);
	public void onComplete(TaskItem item);
	public void onError(TaskItem item);
}
