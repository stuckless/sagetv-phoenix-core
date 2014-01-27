package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import sage.SageTVEventListener;
import sagex.phoenix.Phoenix;
import sagex.phoenix.event.PhoenixEvent;
import test.InitPhoenix;


public class TestEvents {
    private String message = null;
    
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true,true);
    }

    @Test
    public void testEvents() {
    	SageTVEventListener l = new SageTVEventListener() {
			@Override
			public void sageEvent(String eventName, Map args) {
				message=eventName;
			}
		};
        
        Phoenix.getInstance().getEventBus().addListener("TestEvent", l);

        // firing the event should set the message to TEST MESSAGE
        Phoenix.getInstance().getEventBus().fireEvent("TestEvent", new HashMap(), true);
        assertEquals(message, "TestEvent");
        
        Phoenix.getInstance().getEventBus().removeListener("TestEvent", l);
        message=null;

        // fince the handler is removed, then firing the event should do nothing
        Phoenix.getInstance().getEventBus().fireEvent("TestEvent", new HashMap(), true);
        assertTrue(message==null);
        
        //create a class with annotated method and test it
        
    }
    
    private static boolean customeEventNoArgsPassed=false;
    private static boolean customeEventRegArgsPassed=false;
    private static boolean customeEventStringArgPassed=false;
    private static boolean customeEventMapArgPassed=false;
    public static class MyEventClass {
    	@PhoenixEvent("customeEventNoArgsPassed")
    	public void customEvent() {
    		customeEventNoArgsPassed=true;
    	}
    	
    	@PhoenixEvent("customeEventRegArgsPassed")
    	public void customEvent(String name, Map args) {
    		customeEventRegArgsPassed=true;
    	}

    	@PhoenixEvent("customeEventStringArgPassed")
    	public void customEvent(String name) {
    		customeEventStringArgPassed=true;
    	}

    	@PhoenixEvent("customeEventMapArgPassed")
    	public void customEvent(Map args) {
    		customeEventMapArgPassed=true;
    	}
    	
    }
    
    @Test
    public void testCustomEventClass() {
        try {
			Phoenix.getInstance().getEventBus().addListener(new MyEventClass());
		} catch (Exception e) {
			fail("Failed to add events from custom class");
		}
		
        Phoenix.getInstance().getEventBus().fireEvent("customeEventNoArgsPassed", null, true);
        Phoenix.getInstance().getEventBus().fireEvent("customeEventRegArgsPassed", null, true);
        Phoenix.getInstance().getEventBus().fireEvent("customeEventStringArgPassed", null, true);
        Phoenix.getInstance().getEventBus().fireEvent("customeEventMapArgPassed", null, true);
        
        assertTrue(customeEventNoArgsPassed);
        assertTrue(customeEventRegArgsPassed);
        assertTrue(customeEventStringArgPassed);
        assertTrue(customeEventMapArgPassed);
    }
}
