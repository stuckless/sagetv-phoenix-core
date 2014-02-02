package sagex.phoenix.progress;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * The progress tracker manager will take a {@link IRunnableWithProgress}
 * command run it in a separate thread. It will return back a unique id for the
 * operation and allow you to track it's progress using that id.
 * 
 * @author seans
 * 
 */
public class ProgressTrackerManager {
	private static final Logger log = Logger.getLogger(ProgressTrackerManager.class);

	private static class Trackable {
		private Thread thread;
		private String id;
		private IProgressMonitor monitor;

		public String toString() {
			return "Tracking:[id:, " + id + ", thread:," + thread + "]";
		}
	}

	private Map<String, Trackable> running = new HashMap<String, Trackable>();
	private AtomicInteger ids = new AtomicInteger(10000);

	public ProgressTrackerManager() {
	}

	public <T extends IProgressMonitor> String runWithProgress(final IRunnableWithProgress<T> runnable, final T progress) {
		String id = String.valueOf(ids.incrementAndGet());

		final Trackable tracker = new Trackable();
		tracker.id = id;
		tracker.monitor = progress;
		tracker.thread = new Thread() {
			@Override
			public void run() {
				try {
					log.debug("Starting a new runnable progress: " + tracker);
					runnable.run(progress);
				} catch (Throwable t) {
					log.error("Runnable Progress failed: " + tracker, t);
					// progress.setCancelled(true);
					progress.setTaskName("Failed With Error: " + t.getMessage());
				} finally {
					progress.done();
				}
			}
		};

		running.put(id, tracker);
		tracker.thread.setDaemon(true);
		tracker.thread.start();
		log.debug("Runnable Progress has been started and managed: " + tracker);
		return id;
	}

	private Trackable getTrackable(String id) {
		Trackable trackable = running.get(id);
		if (trackable == null) {
			log.error("Nothing Known about Progress Monitor: " + id);
		}
		return trackable;
	}

	public IProgressMonitor getProgress(String id) {
		Trackable t = getTrackable(id);
		if (t != null)
			return t.monitor;
		return null;
	}

	public void cancelProgress(String id) {
		Trackable t = getTrackable(id);
		if (t != null)
			t.monitor.setCancelled(true);
	}

	public void removeProgress(String id) {
		running.remove(id);
	}

	public Set<String> getProgressIds() {
		return running.keySet();
	}

	public void cancelAll() {
		for (String k : getProgressIds()) {
			Trackable t = getTrackable(k);
			if (t != null) {
				t.monitor.setCancelled(true);
			}
		}
	}

	public void removeCompletedTasks() {
		for (Iterator<Map.Entry<String, Trackable>> i = running.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, Trackable> t = i.next();
			if (t.getValue().monitor.isCancelled() || t.getValue().monitor.isDone()) {
				i.remove();
			}
		}
	}

	public boolean hasRunningTasks() {
		for (Iterator<Map.Entry<String, Trackable>> i = running.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, Trackable> t = i.next();
			if (!t.getValue().monitor.isCancelled() || !t.getValue().monitor.isDone()) {
				return true;
			}
		}
		return false;
	}
}
