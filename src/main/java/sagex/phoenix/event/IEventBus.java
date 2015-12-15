package sagex.phoenix.event;

import java.util.Map;

import sage.SageTVEventListener;

/**
 * Simple INterface for an EventBus
 *
 * @author sean
 */
public interface IEventBus {
    public abstract void addListener(String type, SageTVEventListener l);

    public abstract void fireEvent(String event, Map eventArgs, boolean wait);

    public abstract void removeListener(String type, SageTVEventListener l);
}
