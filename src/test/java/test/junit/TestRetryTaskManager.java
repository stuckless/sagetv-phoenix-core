package test.junit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.task.ITaskOperation;
import sagex.phoenix.task.ITaskProgressHandler;
import sagex.phoenix.task.RetryTaskManager;
import sagex.phoenix.task.TaskItem;
import test.InitPhoenix;

public class TestRetryTaskManager {
	public static class Counts {
		public int counts=0;
		public int failed=0;
	}
	
	public static class TaskHandler implements ITaskProgressHandler {
		public boolean started, completed,error;
		@Override
		public void onStart(TaskItem item) {
			started=true;
		}

		@Override
		public void onComplete(TaskItem item) {
			completed=true;
		}

		@Override
		public void onError(TaskItem item) {
			error=true;
		}
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InitPhoenix.init(true, true);
	}
	
	@Test
	public void testRetry() throws InterruptedException {
		final Counts counts = new Counts();
		
		ITaskOperation op1 = new ITaskOperation() {
			@Override
			public void performAction(TaskItem item) throws Throwable {
				System.out.println("Running Task...");
				counts.counts++;
				throw new Exception("Failed");
			}
			
			@Override
			public boolean canRetry(Throwable t) {
				System.out.println("Check Retry...");
				counts.failed++;
				// always retry
				return true;
			}
		};
		
		RetryTaskManager rtt = new RetryTaskManager(3, 2, 200);
		TaskHandler th = new TaskHandler();
		TaskItem ti = new TaskItem();
		ti.setHandler(th);
		rtt.performTask(ti, op1);
		Thread.currentThread().sleep(1000);
		assertTrue(th.started);
		assertTrue(th.error);
		assertFalse(th.completed);
		assertEquals(counts.counts, 3);
		assertEquals(counts.failed, 3);
		assertEquals(TaskItem.State.ERROR, ti.getState());
		assertEquals(2, rtt.getStatus().threads); // 2 threads should have been started
		assertEquals(0, rtt.getStatus().queueLength);
	}

	@Test
	public void testSuccess() throws InterruptedException {
		final Counts counts = new Counts();
		
		ITaskOperation op1 = new ITaskOperation() {
			@Override
			public void performAction(TaskItem item) throws Throwable {
				System.out.println("Running Task...");
				counts.counts++;
			}
			
			@Override
			public boolean canRetry(Throwable t) {
				System.out.println("Check Retry...");
				counts.failed++;
				// always retry
				return true;
			}
		};
		
		RetryTaskManager rtt = new RetryTaskManager(3, 2, 200);
		TaskHandler th = new TaskHandler();
		TaskItem ti = new TaskItem();
		ti.setHandler(th);
		rtt.performTask(ti, op1);
		Thread.currentThread().sleep(1000);
		assertTrue(th.started);
		assertFalse(th.error);
		assertTrue(th.completed);
		assertEquals(counts.counts, 1);
		assertEquals(counts.failed, 0);
		assertEquals(TaskItem.State.COMPLETE, ti.getState());
		assertEquals(1, rtt.getStatus().threads);
		assertEquals(0, rtt.getStatus().queueLength);
	}

	
	@Test
	public void testThreadsAndWaiting() throws InterruptedException {
		ITaskOperation op1 = new ITaskOperation() {
			@Override
			public void performAction(TaskItem item) throws Throwable {
				Thread.sleep(1000);
				throw new Exception("Failed");
			}
			
			@Override
			public boolean canRetry(Throwable t) {
				return true;
			}
		};
		
		RetryTaskManager rtt = new RetryTaskManager(3, 2, 200);
		TaskHandler th = new TaskHandler();
		TaskItem ti1 = new TaskItem();
		TaskItem ti2 = new TaskItem();
		TaskItem ti3 = new TaskItem();
		rtt.performTask(ti1, op1);
		rtt.performTask(ti1, op1);
		rtt.performTask(ti1, op1);
		Thread.sleep(400);
		assertEquals(2, rtt.getStatus().threads);
		assertEquals(3, rtt.getStatus().queueLength); // 3 because they are all failing
		Thread.sleep(10000);
		assertEquals(2, rtt.getStatus().threads);
		assertEquals(0, rtt.getStatus().queueLength); // 0 because they are all failed, and we are done
	}
}
