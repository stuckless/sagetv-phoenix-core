package sagex.phoenix.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sage.SageTVEventListener;

/**
 * Phoenix Event Bus is a proxy for the SageTV event bus, or in the event that
 * phoenix is not loaded as a plugin, then a {@link SimpleEventBus} will be
 * used.
 *
 * @author sean
 */
public class EventBus implements IEventBus {
    private Logger log = Logger.getLogger(EventBus.class);

    private IEventBus bus = null;
    private Map<String, List<SageTVEventListener>> listeners = new HashMap<String, List<SageTVEventListener>>();

    public EventBus() {
        bus = new SimpleEventBus();
    }

    public synchronized void setEventBus(IEventBus newbus) {
        log.info("Setting the Event bus to " + newbus);
        IEventBus oldBus = bus;
        bus = newbus;
        for (String s : listeners.keySet()) {
            List<SageTVEventListener> lnrs = listeners.get(s);
            for (SageTVEventListener l : lnrs) {
                oldBus.removeListener(s, l);
                newbus.addListener(s, l);
                log.info("Transfered Listener " + s + "; " + l + "; to new Event bus");
            }
        }
    }

    /**
     * Adds listeners for all methods that have {@link PhoenixEvent} annotations
     *
     * @param listener Must have at least one method that uses {@link PhoenixEvent}
     * @throws Exception if no methods have a {@link PhoenixEvent} annotation
     */
    public void addListener(final Object listener) throws Exception {
        if (listener == null)
            throw new Exception("Can't add null listener");

        for (Method m : listener.getClass().getDeclaredMethods()) {
            PhoenixEvent pe = m.getAnnotation(PhoenixEvent.class);
            if (pe != null) {
                addListener(pe.value(), new ReflectionEventListener(listener, m));
            }
        }
    }

    @Override
    public void addListener(String type, SageTVEventListener l) {
        bus.addListener(type, l);

        List<SageTVEventListener> handlers = listeners.get(type);
        if (handlers == null) {
            handlers = new ArrayList<SageTVEventListener>();
            listeners.put(type, handlers);
        }
        handlers.add(l);
    }

    @Override
    public void fireEvent(String event, Map eventArgs, boolean wait) {
        bus.fireEvent(event, eventArgs, wait);
    }

    @Override
    public void removeListener(String type, SageTVEventListener l) {
        bus.removeListener(type, l);
        List<SageTVEventListener> handlers = listeners.get(type);
        if (handlers != null) {
            handlers.remove(l);
        }
    }
}
