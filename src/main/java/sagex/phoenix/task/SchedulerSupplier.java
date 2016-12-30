package sagex.phoenix.task;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by seans on 26/12/16.
 */
public interface SchedulerSupplier {
    public ScheduledExecutorService getScheduler();
}
