package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import sagex.phoenix.progress.BasicProgressMonitor;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.progress.ProgressTracker;
import sagex.phoenix.progress.TrackedItem;

public class TestProgressMonitor {
	@Test
	public void testProgressMonitor() {
		IProgressMonitor mon = new BasicProgressMonitor();
		mon.beginTask("Test", 100);
		assertEquals("Test", mon.getTaskName());
		assertEquals(0.0, mon.internalWorked());

		mon.worked(10);
		assertEquals(0.10, mon.internalWorked());

		// worked in cumulative
		mon.worked(10);
		assertEquals(0.20, mon.internalWorked());

		// should push it over the top
		mon.worked(100);
		assertEquals(1.0, mon.internalWorked());
	}

	@Test
	public void testProgressTracker() {
		ProgressTracker<String> track = new ProgressTracker<String>(new BasicProgressMonitor());
		track.addSuccess("Test");
		track.addFailed("TestFailed", "Failed");
		assertEquals(1, track.getSuccessfulItems().size());
		assertEquals(1, track.getFailedItems().size());

		TrackedItem<String> item = track.getFailedItems().peek();
		assertEquals("TestFailed", item.getItem());
	}

	@Test
	public void testProgressTotalWorkCantBeUpdated() {
		ProgressTracker<String> track = new ProgressTracker<String>(new BasicProgressMonitor());
		track.beginTask("Test", 10);
		assertEquals(10, track.getTotalWork());

		track.beginTask("Another Test", 50);
		assertEquals("Total work was updated, and it should never update with multiple calls to beginTask()", 10,
				track.getTotalWork());
	}

	@Test
	public void testSimpleMonitorWithUnknown() {
		IProgressMonitor mon = new BasicProgressMonitor();
		mon.beginTask("Test", IProgressMonitor.UNKNOWN);
		assertEquals("Test", mon.getTaskName());
		assertEquals(0.0, mon.internalWorked());

		mon.worked(100);
		double work1 = mon.internalWorked();
		assertEquals("work1 should be 0.1", 0.1, work1);
		assertTrue("Work1: " + work1 + " should be greater than 0" + work1, work1 > 0);
		assertTrue("work1: " + work1 + " should never get greater that 1.0", work1 < 1.0);

		mon.worked(700);
		double work2 = mon.internalWorked();
		assertEquals("work2 should be 0.8", 0.8, work2);
		assertTrue("Work2: " + work2 + " should be greater than Work1: " + work1, work2 > work1);
		assertTrue("work2: " + work2 + " should never get greater that 1.0", work2 < 1.0);

		mon.done();
		double work3 = mon.internalWorked();
		assertTrue("Work3:" + work3 + " be 1.0: ", work3 == 1.0);
	}
}
