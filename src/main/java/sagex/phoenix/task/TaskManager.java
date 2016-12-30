package sagex.phoenix.task;

import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TaskManager implements SchedulerSupplier {
    private Logger log = Logger.getLogger(TaskManager.class);

    ScheduledExecutorService scheduler = null;
    RetryTaskManager retryTaskManager = null;

    public TaskManager() {
        init();
    }

    public void init() {
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(20);
            retryTaskManager = new RetryTaskManager(5, 30*60*1000, this);
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
        scheduler = null;
        retryTaskManager = null;
    }


    /**
     * Schedules a repeating task, starting at the given time, and repeat every
     * repeat ms
     *
     * @param task
     * @param delay  initial dely in MS
     * @param repeat ms between repeats
     */
    public ScheduledFuture scheduleRepeatingTask(Runnable task, long delay, long repeat) {
        log.debug("Adding Scheduled Task: " + task + "; Delay: " + delay + "; Repeat: " + repeat);
        return scheduler.scheduleAtFixedRate(task, delay, repeat, TimeUnit.MILLISECONDS);
    }

    /**
     * Runs the task after the specified delay in ms
     * @param runnable
     * @param delay
     */
    public void runLater(Runnable runnable, long delay) {
        log.debug("Scheduled Runnable Task with delay " + delay + "; Runnable: " + runnable );
        scheduler.schedule(runnable,delay, TimeUnit.MILLISECONDS);
    }

    public void submitTaskWithRetry(TaskItem item) {
        retryTaskManager.submitTask(item);
    }

    public void submit(Runnable item) {
        scheduler.submit(item);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
