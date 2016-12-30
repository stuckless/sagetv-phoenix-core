package sagex.phoenix.task;

import org.apache.log4j.Logger;
import sagex.phoenix.task.TaskItem.State;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private SchedulerSupplier scheduler;
    Logger log = Logger.getLogger(this.getClass());

    protected int defaultMaxRetries = 5;
    protected long defaultDelay = 1000 * 60 * 5; // 5 minute delays

    RetryTaskManager() {
    }

    RetryTaskManager(int maxRetries, long delay, SchedulerSupplier supplier) {
        this.defaultMaxRetries = maxRetries;
        this.defaultDelay = delay;
        this.scheduler = supplier;
    }

    /**
     * Main task execution point. TaskItem requires {@link ITaskOperation} be
     * set.
     *
     * @param item
     */
    public void submitTask(TaskItem item) {
        if (item.getOperation() == null)
            throw new RuntimeException("TaskItem required an operation to perform");
        item.setRetries(0);
        if (item.getMaxReties() == 0) {
            // set default # of retries
            item.setMaxReties(defaultMaxRetries);
        }
        reschedule(item);
    }

    /**
     * Main task execution point.
     *
     * @param item
     */
    public void submitTask(TaskItem item, ITaskOperation operation) {
        item.setOperation(operation);
        submitTask(item);
    }

    protected void fail(TaskItem item, Throwable t) {
        log.warn("Failed to perform task: " + item, t);
        item.setError(t);
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

        ScheduledExecutorService scheduledExecutorService = scheduler.getScheduler();
        long delay = 0;
        if (retries > 1) {
            // delay retries by 1 second
            delay = defaultDelay;
        }

        scheduledExecutorService.schedule(createTask(item), delay, TimeUnit.MILLISECONDS);
        log.info("Scheduled Task: " + item);
    }

    void completed(TaskItem item) {
        item.setState(TaskItem.State.COMPLETE);
        if (item.getHandler() != null) {
            item.getHandler().onComplete(item);
        }
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
