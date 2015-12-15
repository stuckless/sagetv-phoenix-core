package sagex.phoenix.task;

import org.apache.log4j.Logger;
import sagex.phoenix.task.TaskItem.State;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskManager is used to manager long running tasks that require rety
 * abilities. If you only need to run simple background task that does not
 * require retries, then use the java {@link Timer} and {@link TimerTask}.
 * <p/>
 * Subclasses will need implement the createTask() method to create tasks for
 * their particular type.
 *
 * @author seans
 */
public class RetryTaskManager {
    public static class Status {
        public int threads = 0;
        public int queueLength = 0;

        @Override
        public String toString() {
            return "Status [threads=" + threads + ", waiting=" + queueLength + "]";
        }
    }

    Logger log = Logger.getLogger(this.getClass());
    private AtomicInteger currentThread = new AtomicInteger(0);
    private List<Timer> threads = new ArrayList<Timer>();
    private Queue<TaskItem> items = new LinkedList<TaskItem>();

    protected int defaultMaxRetries = 5;
    protected long defaultDelay = 1000 * 60 * 5; // 5 minute delays
    protected int defaultMaxThreads = 3;

    public RetryTaskManager() {
    }

    public RetryTaskManager(int maxRetries, int maxThreads, long delay) {
        this.defaultMaxRetries = maxRetries;
        this.defaultMaxThreads = maxThreads;
        this.defaultDelay = delay;
    }

    /**
     * Main task execution point. TaskItem requires {@link ITaskOperation} be
     * set.
     *
     * @param item
     */
    public void performTask(TaskItem item) {
        if (item.getOperation() == null)
            throw new RuntimeException("TaskItem required an operation to perform");
        item.setRetries(0);
        if (item.getMaxReties() == 0) {
            // set default # of retries
            item.setMaxReties(defaultMaxRetries);
        }
        items.add(item);
        reschedule(item);
    }

    /**
     * Main task execution point.
     *
     * @param item
     */
    public void performTask(TaskItem item, ITaskOperation operation) {
        item.setOperation(operation);
        performTask(item);
    }

    protected void fail(TaskItem item, Throwable t) {
        log.warn("Failed to perform task: " + item, t);
        item.setError(t);
        items.remove(item);
        if (item.getHandler() != null) {
            item.getHandler().onError(item);
        }
    }

    protected void started(TaskItem item) {
        log.info("Starting Task " + item);
        item.setState(State.STARTED);
        if (item.getHandler() != null) {
            item.getHandler().onStart(item);
        }
    }

    protected void reschedule(TaskItem item) {
        int retries = item.incrementRetries();
        if (retries > item.getMaxReties()) {
            fail(item, new Exception("Max Retry has been exceeded: " + retries));
            return;
        }

        Timer timer = getNextTimer();
        long delay = 0;
        if (retries > 1) {
            // delay retries by 1 second
            delay = defaultDelay;
        }

        timer.schedule(createTask(item), delay);
        log.info("Scheduled Task: " + item);
    }

    void completed(TaskItem item) {
        items.remove(item);
        item.setState(TaskItem.State.COMPLETE);
        if (item.getHandler() != null) {
            item.getHandler().onComplete(item);
        }
    }

    private synchronized Timer getNextTimer() {
        int pos = currentThread.getAndAdd(1) % defaultMaxThreads;
        if (pos < threads.size()) {
            return threads.get(pos);
        } else {
            Timer timer = new Timer("Task-" + pos, true);
            threads.add(timer);
            return timer;
        }
    }

    /**
     * returns the current status of the download manager. ie, the number of
     * running threads, waiting items, etc.
     *
     * @return
     */
    public Status getStatus() {
        Status stats = new Status();
        stats.threads = threads.size();
        stats.queueLength = items.size();
        return stats;
    }

    /**
     * Create a {@link RunnableTask} specific for your long running operation.
     *
     * @param item
     * @return
     */
    protected RunnableTask createTask(TaskItem item) {
        return new RunnableTask(this, item);
    }
}
