package sagex.phoenix.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sage.SageTVEventListener;

public class SimpleEventBus implements IEventBus {
    private Logger log = Logger.getLogger(this.getClass());
    
    private Map<String, List<SageTVEventListener>> types = new HashMap<String, List<SageTVEventListener>>();
    
    /* (non-Javadoc)
	 * @see sagex.phoenix.event.IEventBus#addListener(java.lang.String, sage.SageTVEventListener)
	 */
    @Override
	public void addListener(String type, SageTVEventListener l) {
        List<SageTVEventListener> handlers = types.get(type);
        if (handlers==null) {
            handlers = new ArrayList<SageTVEventListener>();
            types.put(type, handlers);
        }
        
        handlers.add(l);
        log.info("Registering Event Handler: " + l.getClass().getName() + " for Event Type: " + type);
    }
    
    /* (non-Javadoc)
	 * @see sagex.phoenix.event.IEventBus#fireEvent(java.lang.String, java.util.Map)
	 */
    @Override
	public void fireEvent(String event, Map eventArgs, boolean wait) {
    	if (wait) {
	        List<SageTVEventListener> handlers = types.get(event);
	        if (handlers!=null) {
	            for (SageTVEventListener h : handlers) {
	                h.sageEvent(event, eventArgs);
	            }
	        } else {
	            log.warn("No Event Handlers configured for type: " + event);
	        }
    	} else {
    		fireEventInBackground(event, eventArgs);
    	}
    }

    private void fireEventInBackground(final String event, final Map eventArgs) {
    	Runnable r = new Runnable() {
			@Override
			public void run() {
		        List<SageTVEventListener> handlers = types.get(event);
		        if (handlers!=null) {
		            for (SageTVEventListener h : handlers) {
		                h.sageEvent(event, eventArgs);
		            }
		        } else {
		            log.warn("No Event Handlers configured for type: " + event);
		        }
			}
		};
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.start();
    }

    /* (non-Javadoc)
	 * @see sagex.phoenix.event.IEventBus#removeListener(java.lang.String, sage.SageTVEventListener)
	 */
    @Override
	public void removeListener(String type, SageTVEventListener l) {
        List<SageTVEventListener> handlers = types.get(type);
        if (handlers!=null) {
        	handlers.remove(l);
        }
    }
}
