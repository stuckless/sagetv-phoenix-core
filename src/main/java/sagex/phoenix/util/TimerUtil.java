package sagex.phoenix.util;

import java.util.Timer;
import java.util.TimerTask;

public class TimerUtil {
	/**
	 * Convenience method that will run a given task repeatedly
	 * 
	 * @param delay
	 * @param period
	 * @param task
	 */
	public static java.util.Timer scheduleRepeating(long delay, long period, TimerTask task) {
		java.util.Timer timer = new java.util.Timer();
		timer.schedule(task, delay, period);
		return timer;
	}
	
	/**
	 * Schedules a task to run once after a given delay
	 * @param delay
	 * @param task
	 * @return
	 */
	public static Timer runOnce(long delay, TimerTask task) {
		java.util.Timer timer = new java.util.Timer();
		timer.schedule(task, delay);
		return timer;
	}
	
	/**
	 * performs sleep on the current thread
	 * 
	 * @param delay
	 */
	public static void sleep(long delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}
}
