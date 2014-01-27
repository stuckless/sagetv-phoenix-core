package sagex.phoenix.task;

import java.util.TimerTask;

import sagex.phoenix.task.TaskItem.State;

public class RunnableTask extends TimerTask {
	private RetryTaskManager mgr = null;
	private TaskItem item = null;
	
	public RunnableTask(RetryTaskManager manager, TaskItem item) {
		this.mgr=manager;
		this.item=item;
		if (item.getOperation()==null) {
			throw new RuntimeException("Task must have an operation");
		}
	}

	@Override
	public void run() {
		try {
			mgr.started(item);
			item.setState(State.STARTED);
			item.getOperation().performAction(item);
			mgr.completed(item);
		} catch (Throwable t) {
			if (item.getOperation().canRetry(t)) {
				mgr.reschedule(item);
			} else {
				mgr.fail(item, t);
			}
		}
	}
}
