package sagex.phoenix.task;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import test.InitPhoenix;

public class TestTaskManager {
    private int called = 0;

    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testTaskManager() {
        final AtomicReference<ScheduledFuture> ref = new AtomicReference<>(null);
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                called++;
                System.out.println("Called " + called);
                if (called >= 5) {
                    // cancel the running task
                    ref.get().cancel(true);
                }
            }
        };
        ref.set(Phoenix.getInstance().getTaskManager().scheduleRepeatingTask(tt, 0, 100));

        System.out.println("Waiting for 2 seconds, then testing the results..");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Should be called exactly 5 times because we cancelled ourself after 5 iterations
        assertEquals("Called more or less times ", 5, called);
    }
}
