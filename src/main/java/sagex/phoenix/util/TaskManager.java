package sagex.phoenix.util;

import org.apache.log4j.Logger;

import java.util.*;

public class TaskManager {
    private Logger log = Logger.getLogger(TaskManager.class);

    private Timer timer = new Timer(true);
    private Map<String, TimerTask> tasks = new HashMap<String, TimerTask>();

    public TaskManager() {
    }

    /**
     * Schedules a repeating task, starting at the given time, and repeat every
     * repeat ms
     *
     * @param id
     * @param task
     * @param start  date/time to start, or null, for right now
     * @param repeat #ms between repeats, or 0 for a 1 time only
     */
    public void scheduleTask(String id, TimerTask task, Date start, long repeat) {
        if (start == null)
            start = Calendar.getInstance().getTime();

        TimerTask tt = tasks.get(id);
        if (tt != null) {
            log.info("Cancelling scheduled task since it's being updated for: " + id);
            tt.cancel();
        }
        log.info("Adding Scheduled Task: " + id + "; Date: " + start + "; Repeat: " + repeat);
        if (repeat <= 0) {
            timer.schedule(task, start);
        } else {
            timer.schedule(task, start, repeat);
        }
        tasks.put(id, task);
    }
}
