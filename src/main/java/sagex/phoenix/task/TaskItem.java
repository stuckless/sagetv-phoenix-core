package sagex.phoenix.task;

import java.util.HashMap;
import java.util.Map;


public class TaskItem {
	public enum State {WAITING, STARTED, COMPLETE, ERROR}
	private long id = System.nanoTime();
	private State state = State.WAITING;
	private ITaskProgressHandler handler;
	
	private int retries = 0;
	private int maxReties = 0;
	private Throwable error;
	
	private Map<String, Object> userData = new HashMap<String, Object>();
	
	private ITaskOperation operation;
	
	public TaskItem() {
	}
	
	public long getId() {
		return id;
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public ITaskProgressHandler getHandler() {
		return handler;
	}
	
	public void setHandler(ITaskProgressHandler handler) {
		this.handler = handler;
	}

	@Override
	public String toString() {
		return "TaskItem [id=" + id + ", state=" + state + ", handler="
				+ handler + ", retries=" + retries + ", maxReties=" + maxReties
				+ ", error=" + error + ", userData=" + userData + "]";
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public int getMaxReties() {
		return maxReties;
	}

	public void setMaxReties(int maxReties) {
		this.maxReties = maxReties;
	}

	public void setError(Throwable error) {
		setState(State.ERROR);
		this.error = error;
	}

	public Throwable getError() {
		return error;
	}

	public int incrementRetries() {
		return ++retries;
	}
	
	public Map<String, Object> getUserData() {
		return userData;
	}

	public void setOperation(ITaskOperation operation) {
		this.operation = operation;
	}

	public ITaskOperation getOperation() {
		return operation;
	}
}
