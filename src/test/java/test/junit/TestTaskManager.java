package test.junit;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimerTask;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import test.InitPhoenix;


public class TestTaskManager {
    private String message = null;
    private int called=0;
    
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true,true);
    }

    @Test
    public void testTaskManager() {
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                called++;
                System.out.println("Called " + called);
                if (called>=5) {
                    cancel();
                }
            }
        };
        Phoenix.getInstance().getTaskManager().scheduleTask("mytask", tt, Calendar.getInstance().getTime(), 100);
        
        System.out.println("Waiting for 2 seconds, then testing the results..");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals("Called more or less times ", 5, called);
    }
}
